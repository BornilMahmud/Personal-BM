package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.LoanPayment
import com.example.data.model.LoanRecord
import com.example.data.model.NavSection
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsContribution
import com.example.data.model.SavingsGoal
import com.example.data.model.UserSettings
import com.example.data.repository.PersonalBmRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionFilterState(
    val searchQuery: String = "",
    val typeFilter: String = "ALL", // "ALL", "INCOME", "EXPENSE", "TRANSFER"
    val categoryIdFilter: String = "ALL",
    val accountIdFilter: String = "ALL",
    val periodFilter: String = "ALL", // "ALL", "TODAY", "THIS_WEEK", "THIS_MONTH", "THIS_YEAR"
    val sortBy: String = "DATE_DESC", // "DATE_DESC", "DATE_ASC", "AMOUNT_DESC", "AMOUNT_ASC"
    val currentPage: Int = 1,
    val pageSize: Int = 15
)

data class PersonalBmUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val currentSection: NavSection = NavSection.OVERVIEW,
    val settings: UserSettings = UserSettings(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<FinTransaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val savingsContributions: List<SavingsContribution> = emptyList(),
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val loans: List<LoanRecord> = emptyList(),
    val loanPayments: List<LoanPayment> = emptyList(),
    val txFilter: TransactionFilterState = TransactionFilterState(),
    val selectedMonthKey: String = DhakaDateUtils.currentMonthKey(),
    val selectedCalendarEpochDay: Long = DhakaDateUtils.todayEpochDay(),
    val analyticsPeriod: String = "MONTHLY", // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val analyticsChartType: String = "BAR", // "BAR", "LINE", "DONUT"
    val snackbarMessage: String? = null,
    val errorMessage: String? = null
)

class PersonalBmViewModel(
    private val repository: PersonalBmRepository,
    private val currentUserId: String,
    private val initialDisplayName: String = "",
    private val initialEmail: String = ""
) : ViewModel() {

    private val _localState = MutableStateFlow(PersonalBmUiState())
    private var recurringCheckCompleted = false

    init {
        // Seed initial BDT accounts (bKash, Nagad, Rocket, Upay, Cash, Bank) and categories on first login
        viewModelScope.launch {
            try {
                repository.ensureDefaultsSeeded(initialDisplayName, initialEmail)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to seed defaults: ${e.message}")
            }
        }
    }

    private val coreDataFlow = combine(
        repository.observeSettings(currentUserId).catch { emit(UserSettings(userId = currentUserId)) },
        repository.observeAccounts(currentUserId).catch { emit(emptyList()) },
        repository.observeCategories(currentUserId).catch { emit(emptyList()) },
        repository.observeTransactions(currentUserId).catch { emit(emptyList()) },
        repository.observeBudgets(currentUserId).catch { emit(emptyList()) }
    ) { settings, accounts, categories, transactions, budgets ->
        CoreDataBundle(
            settings = settings ?: UserSettings(
                userId = currentUserId,
                displayName = initialDisplayName,
                email = initialEmail
            ),
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            budgets = budgets
        )
    }

    private val planningDataFlow = combine(
        repository.observeSavingsGoals(currentUserId).catch { emit(emptyList()) },
        repository.observeSavingsContributions(currentUserId).catch { emit(emptyList()) },
        repository.observeRecurringTransactions(currentUserId).catch { emit(emptyList()) },
        repository.observeLoans(currentUserId).catch { emit(emptyList()) },
        repository.observeLoanPayments(currentUserId).catch { emit(emptyList()) }
    ) { goals, contribs, recurring, loans, loanPayments ->
        PlanningDataBundle(
            goals = goals,
            contributions = contribs,
            recurring = recurring,
            loans = loans,
            loanPayments = loanPayments
        )
    }

    val uiState: StateFlow<PersonalBmUiState> = combine(
        coreDataFlow,
        planningDataFlow,
        _localState
    ) { core, planning, local ->
        // Trigger one-time idempotent recurring auto-post check once recurring rules arrive
        if (!recurringCheckCompleted && planning.recurring.isNotEmpty()) {
            recurringCheckCompleted = true
            triggerRecurringAutoPost(planning.recurring)
        }
        local.copy(
            isLoading = false,
            settings = core.settings,
            accounts = core.accounts,
            categories = core.categories,
            transactions = core.transactions,
            budgets = core.budgets,
            savingsGoals = planning.goals,
            savingsContributions = planning.contributions,
            recurringTransactions = planning.recurring,
            loans = planning.loans,
            loanPayments = planning.loanPayments
        )
    }.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Stream error in PersonalBmViewModel", rootError)
        emit(_localState.value.copy(isLoading = false, errorMessage = rootError.message ?: "Failed to load data"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PersonalBmUiState(isLoading = true)
    )

    private fun triggerRecurringAutoPost(rules: List<RecurringTransaction>) {
        viewModelScope.launch {
            try {
                val postedCount = repository.processDueRecurringRules(rules)
                if (postedCount > 0) {
                    showSnackbar("Auto-posted $postedCount due recurring transaction(s) to your ledger.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Recurring auto-post check failed: ${e.message}")
            }
        }
    }

    // ========================================================================
    // NAVIGATION & UI STATE UPDATERS
    // ========================================================================

    fun navigateTo(section: NavSection) {
        _localState.update { it.copy(currentSection = section) }
    }

    fun updateTransactionFilter(transform: (TransactionFilterState) -> TransactionFilterState) {
        _localState.update { state ->
            state.copy(txFilter = transform(state.txFilter))
        }
    }

    fun setSelectedMonthKey(monthKey: String) {
        _localState.update { it.copy(selectedMonthKey = monthKey) }
    }

    fun setSelectedCalendarDay(epochDay: Long) {
        _localState.update { it.copy(selectedCalendarEpochDay = epochDay) }
    }

    fun setAnalyticsPeriod(period: String) {
        _localState.update { it.copy(analyticsPeriod = period) }
    }

    fun setAnalyticsChartType(chartType: String) {
        _localState.update { it.copy(analyticsChartType = chartType) }
    }

    fun clearSnackbar() {
        _localState.update { it.copy(snackbarMessage = null, errorMessage = null) }
    }

    private fun showSnackbar(message: String) {
        _localState.update { it.copy(snackbarMessage = message) }
    }

    private fun showError(message: String) {
        _localState.update { it.copy(errorMessage = message) }
    }

    // ========================================================================
    // CRUD OPERATIONS WITH DUPLICATE SUBMISSION GUARD
    // ========================================================================

    private fun runMutation(successMsg: String, block: suspend () -> Unit) {
        if (_localState.value.isSubmitting) return
        _localState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                block()
                _localState.update { it.copy(isSubmitting = false, snackbarMessage = successMsg) }
            } catch (e: CancellationException) {
                _localState.update { it.copy(isSubmitting = false) }
                throw e
            } catch (e: Exception) {
                _localState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.localizedMessage ?: "Operation failed"
                    )
                }
            }
        }
    }

    // Transactions
    fun saveTransaction(tx: FinTransaction, isEdit: Boolean) {
        runMutation(if (isEdit) "Transaction updated & balances reconciled" else "Transaction recorded") {
            if (isEdit) {
                repository.updateTransaction(tx)
            } else {
                repository.addTransaction(tx)
            }
        }
    }

    fun duplicateTransaction(tx: FinTransaction) {
        runMutation("Transaction duplicated") {
            val copyTx = tx.copy(
                id = "",
                title = "${tx.title} (Copy)".take(160),
                dateEpochDay = DhakaDateUtils.todayEpochDay(),
                timeFormatted = DhakaDateUtils.currentTimeFormatted(),
                timestampMillis = System.currentTimeMillis(),
                createdAt = null,
                updatedAt = null
            )
            repository.addTransaction(copyTx)
        }
    }

    fun deleteTransaction(txId: String) {
        runMutation("Transaction deleted & balance reversed") {
            repository.deleteTransaction(txId)
        }
    }

    // Accounts
    fun saveAccount(account: Account) {
        val isNew = account.id.isBlank()
        runMutation(if (isNew) "Account created" else "Account updated") {
            repository.saveAccount(account)
        }
    }

    fun deleteAccount(accountId: String) {
        val currentTx = uiState.value.transactions
        runMutation("Account deleted") {
            repository.deleteAccount(accountId, currentTx)
        }
    }

    // Categories
    fun saveCategory(category: Category) {
        val isNew = category.id.isBlank()
        runMutation(if (isNew) "Category added" else "Category updated") {
            repository.saveCategory(category)
        }
    }

    fun deleteCategory(categoryId: String) {
        runMutation("Category removed") {
            repository.deleteCategory(categoryId)
        }
    }

    // Budgets
    fun saveBudget(budget: Budget) {
        runMutation("Monthly budget saved") {
            repository.saveBudget(budget)
        }
    }

    fun deleteBudget(budgetId: String) {
        runMutation("Budget removed") {
            repository.deleteBudget(budgetId)
        }
    }

    // Savings Goals
    fun saveSavingsGoal(goal: SavingsGoal) {
        val isNew = goal.id.isBlank()
        runMutation(if (isNew) "Savings goal created" else "Savings goal updated") {
            repository.saveSavingsGoal(goal)
        }
    }

    fun contributeToSavingsGoal(
        goalId: String,
        amountPaisa: Long,
        sourceAccountId: String,
        sourceAccountName: String,
        notes: String
    ) {
        runMutation("Contribution added to savings goal") {
            repository.addSavingsContribution(
                goalId = goalId,
                amountPaisa = amountPaisa,
                sourceAccountId = sourceAccountId,
                sourceAccountName = sourceAccountName,
                notes = notes
            )
        }
    }

    fun deleteSavingsGoal(goalId: String) {
        runMutation("Savings goal deleted") {
            repository.deleteSavingsGoal(goalId)
        }
    }

    // Recurring Transactions
    fun saveRecurringRule(rule: RecurringTransaction) {
        val isNew = rule.id.isBlank()
        runMutation(if (isNew) "Recurring rule scheduled" else "Recurring rule updated") {
            repository.saveRecurringRule(rule)
        }
    }

    fun toggleRecurringRule(rule: RecurringTransaction) {
        val statusLabel = if (rule.isActive) "paused" else "resumed"
        runMutation("Recurring rule $statusLabel") {
            repository.toggleRecurringRule(rule)
        }
    }

    fun deleteRecurringRule(ruleId: String) {
        runMutation("Recurring rule deleted") {
            repository.deleteRecurringRule(ruleId)
        }
    }

    fun runRecurringCheckNow() {
        val rules = uiState.value.recurringTransactions
        runMutation("Checked due recurring transactions") {
            repository.processDueRecurringRules(rules)
        }
    }

    // Loans & Debts
    fun saveLoan(loan: LoanRecord) {
        val isNew = loan.id.isBlank()
        runMutation(if (isNew) "Loan record added" else "Loan record updated") {
            repository.saveLoan(loan)
        }
    }

    fun recordLoanPayment(
        loan: LoanRecord,
        amountPaisa: Long,
        accountId: String,
        accountName: String,
        notes: String
    ) {
        runMutation("Loan repayment recorded") {
            repository.addLoanPayment(
                loan = loan,
                paymentAmountPaisa = amountPaisa,
                accountId = accountId,
                accountName = accountName,
                notes = notes
            )
        }
    }

    fun deleteLoan(loanId: String) {
        runMutation("Loan record deleted") {
            repository.deleteLoan(loanId)
        }
    }

    // Settings
    fun updateSettings(updated: UserSettings) {
        runMutation("Settings saved") {
            repository.updateSettings(updated)
        }
    }

    private data class CoreDataBundle(
        val settings: UserSettings,
        val accounts: List<Account>,
        val categories: List<Category>,
        val transactions: List<FinTransaction>,
        val budgets: List<Budget>
    )

    private data class PlanningDataBundle(
        val goals: List<SavingsGoal>,
        val contributions: List<SavingsContribution>,
        val recurring: List<RecurringTransaction>,
        val loans: List<LoanRecord>,
        val loanPayments: List<LoanPayment>
    )

    private companion object {
        const val TAG = "PersonalBmVM"
    }
}
