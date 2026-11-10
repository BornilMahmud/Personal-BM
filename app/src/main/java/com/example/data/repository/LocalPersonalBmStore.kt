package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DefaultSeedData
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.LoanPayment
import com.example.data.model.LoanRecord
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsContribution
import com.example.data.model.SavingsGoal
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Instant, zero-latency Local Storage persistence engine for Personal BM.
 * Allows Default User login without requiring cloud authentication while preserving
 * 100% of financial calculations, atomic account balance reconciliation, and persistence across restarts.
 */
class LocalPersonalBmStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val settingsFlowMap = mutableMapOf<String, MutableStateFlow<UserSettings>>()
    private val photoUrlFlowMap = mutableMapOf<String, MutableStateFlow<String>>()
    private val accountsFlowMap = mutableMapOf<String, MutableStateFlow<List<Account>>>()
    private val categoriesFlowMap = mutableMapOf<String, MutableStateFlow<List<Category>>>()
    private val transactionsFlowMap = mutableMapOf<String, MutableStateFlow<List<FinTransaction>>>()
    private val budgetsFlowMap = mutableMapOf<String, MutableStateFlow<List<Budget>>>()
    private val goalsFlowMap = mutableMapOf<String, MutableStateFlow<List<SavingsGoal>>>()
    private val contributionsFlowMap = mutableMapOf<String, MutableStateFlow<List<SavingsContribution>>>()
    private val recurringFlowMap = mutableMapOf<String, MutableStateFlow<List<RecurringTransaction>>>()
    private val loansFlowMap = mutableMapOf<String, MutableStateFlow<List<LoanRecord>>>()
    private val loanPaymentsFlowMap = mutableMapOf<String, MutableStateFlow<List<LoanPayment>>>()

    @Synchronized
    fun ensureSeeded(
        uid: String,
        displayName: String = "Guest",
        email: String = "",
        photoUrl: String = ""
    ) {
        var currentSettings = loadSettingsFromPrefs(uid)
        val currentAccounts = loadAccountsFromPrefs(uid).toMutableList()
        val currentCategories = loadCategoriesFromPrefs(uid).toMutableList()

        var changed = false
        if (currentAccounts.isEmpty()) {
            currentAccounts.addAll(DefaultSeedData.defaultAccounts(uid).map { it.second })
            saveAccountsToPrefs(uid, currentAccounts)
            changed = true
        }
        if (currentCategories.isEmpty()) {
            currentCategories.addAll(DefaultSeedData.defaultCategories(uid).map { it.second })
            saveCategoriesToPrefs(uid, currentCategories)
            changed = true
        } else {
            var addedCat = false
            for ((_, defaultCat) in DefaultSeedData.defaultCategories(uid)) {
                if (currentCategories.none { it.id == defaultCat.id }) {
                    currentCategories.add(defaultCat)
                    addedCat = true
                }
            }
            if (addedCat) {
                saveCategoriesToPrefs(uid, currentCategories)
                changed = true
            }
        }

        // One-time migration to ensure white ("light") theme is the default if previously auto-seeded as "system"
        val migratedKey = key(uid, "migrated_white_default_v1")
        if (!prefs.getBoolean(migratedKey, false)) {
            prefs.edit().putBoolean(migratedKey, true).apply()
            if (currentSettings != null && currentSettings.themeMode == "system") {
                currentSettings = currentSettings.copy(themeMode = "light")
                saveSettingsToPrefs(uid, currentSettings)
                changed = true
            }
        }

        // One-time migration for local default user to be "Guest" by default unless custom-named
        if (uid == DEFAULT_LOCAL_USER_ID) {
            val migratedGuestKey = key(uid, "migrated_guest_default_v1")
            if (!prefs.getBoolean(migratedGuestKey, false)) {
                prefs.edit().putBoolean(migratedGuestKey, true).apply()
                val hasCustomName = prefs.getBoolean(key(uid, "custom_display_name"), false)
                if (!hasCustomName && currentSettings != null &&
                    (currentSettings.displayName == "Bornil Mahmud" || currentSettings.displayName == "Personal BM User")
                ) {
                    currentSettings = currentSettings.copy(
                        displayName = "Guest",
                        email = "guest@personalbm.app"
                    )
                    saveSettingsToPrefs(uid, currentSettings)
                    changed = true
                }
            }
        }

        if (currentSettings == null) {
            val defaultSettings = UserSettings(
                id = "preferences",
                userId = uid,
                displayName = displayName.ifBlank { "Guest" }.take(120),
                email = email.ifBlank { if (uid == DEFAULT_LOCAL_USER_ID) "guest@personalbm.app" else "" }.take(254),
                currencyCode = "BDT",
                currencySymbol = "৳",
                timezone = "Asia/Dhaka",
                language = "en",
                themeMode = "light",
                defaultAccountId = "acc_cash",
                weekStartDay = "SATURDAY",
                seededDefaults = true
            )
            saveSettingsToPrefs(uid, defaultSettings)
            changed = true
        } else {
            // If Google Auth provides a real name/email and current settings still have generic placeholders, update them
            val hasCustomName = prefs.getBoolean(key(uid, "custom_display_name"), false)
            val shouldUpdateName = !hasCustomName &&
                displayName.isNotBlank() &&
                (currentSettings.displayName.isBlank() ||
                    currentSettings.displayName == "Guest" ||
                    currentSettings.displayName == "Personal BM User" ||
                    currentSettings.displayName == "Bornil Mahmud")
            val shouldUpdateEmail = email.isNotBlank() && currentSettings.email != email
            if (shouldUpdateName || shouldUpdateEmail) {
                currentSettings = currentSettings.copy(
                    displayName = if (shouldUpdateName) displayName.take(120) else currentSettings.displayName,
                    email = if (shouldUpdateEmail) email.take(254) else currentSettings.email
                )
                saveSettingsToPrefs(uid, currentSettings)
                changed = true
            }
        }

        if (photoUrl.isNotBlank()) {
            val existingPhoto = getPhotoUrl(uid)
            val hasCustomPhoto = prefs.getBoolean(key(uid, "custom_photo_url"), false)
            if (existingPhoto.isBlank() || !hasCustomPhoto) {
                prefs.edit().putString(key(uid, "photo_url"), photoUrl).apply()
                changed = true
            }
        }

        if (changed) {
            refreshFlows(uid)
        }
    }

    @Synchronized
    private fun refreshFlows(uid: String) {
        settingsFlowMap[uid]?.value = loadSettingsFromPrefs(uid) ?: UserSettings(userId = uid, themeMode = "light")
        photoUrlFlowMap[uid]?.value = getPhotoUrl(uid)
        accountsFlowMap[uid]?.value = sortAccounts(loadAccountsFromPrefs(uid))
        categoriesFlowMap[uid]?.value = sortCategories(loadCategoriesFromPrefs(uid))
        transactionsFlowMap[uid]?.value = sortTransactions(loadTransactionsFromPrefs(uid))
        budgetsFlowMap[uid]?.value = loadBudgetsFromPrefs(uid)
        goalsFlowMap[uid]?.value = loadGoalsFromPrefs(uid).sortedBy { it.targetDateEpochDay }
        contributionsFlowMap[uid]?.value = loadContributionsFromPrefs(uid).sortedByDescending { it.dateEpochDay }
        recurringFlowMap[uid]?.value = loadRecurringFromPrefs(uid).sortedBy { it.nextDueDateEpochDay }
        loansFlowMap[uid]?.value = sortLoans(loadLoansFromPrefs(uid))
        loanPaymentsFlowMap[uid]?.value = loadLoanPaymentsFromPrefs(uid).sortedByDescending { it.dateEpochDay }
    }

    // ========================================================================
    // OBSERVATION STREAMS
    // ========================================================================

    @Synchronized
    fun observeSettings(uid: String): Flow<UserSettings?> {
        ensureSeeded(uid)
        return settingsFlowMap.getOrPut(uid) {
            MutableStateFlow(loadSettingsFromPrefs(uid) ?: UserSettings(userId = uid, themeMode = "light"))
        }.map { it }
    }

    @Synchronized
    fun observePhotoUrl(uid: String): Flow<String> {
        ensureSeeded(uid)
        return photoUrlFlowMap.getOrPut(uid) {
            MutableStateFlow(getPhotoUrl(uid))
        }
    }

    @Synchronized
    fun getPhotoUrl(uid: String): String {
        return prefs.getString(key(uid, "photo_url"), "") ?: ""
    }

    @Synchronized
    fun setPhotoUrl(uid: String, photoUrl: String, isCustom: Boolean = true) {
        prefs.edit()
            .putString(key(uid, "photo_url"), photoUrl.trim())
            .putBoolean(key(uid, "custom_photo_url"), isCustom && photoUrl.isNotBlank())
            .apply()
        photoUrlFlowMap[uid]?.value = photoUrl.trim()
    }

    @Synchronized
    fun observeAccounts(uid: String): Flow<List<Account>> {
        ensureSeeded(uid)
        return accountsFlowMap.getOrPut(uid) {
            MutableStateFlow(sortAccounts(loadAccountsFromPrefs(uid)))
        }
    }

    @Synchronized
    fun observeCategories(uid: String): Flow<List<Category>> {
        ensureSeeded(uid)
        return categoriesFlowMap.getOrPut(uid) {
            MutableStateFlow(sortCategories(loadCategoriesFromPrefs(uid)))
        }
    }

    @Synchronized
    fun observeTransactions(uid: String): Flow<List<FinTransaction>> {
        ensureSeeded(uid)
        return transactionsFlowMap.getOrPut(uid) {
            MutableStateFlow(sortTransactions(loadTransactionsFromPrefs(uid)))
        }
    }

    @Synchronized
    fun observeBudgets(uid: String): Flow<List<Budget>> {
        ensureSeeded(uid)
        return budgetsFlowMap.getOrPut(uid) {
            MutableStateFlow(loadBudgetsFromPrefs(uid))
        }
    }

    @Synchronized
    fun observeSavingsGoals(uid: String): Flow<List<SavingsGoal>> {
        ensureSeeded(uid)
        return goalsFlowMap.getOrPut(uid) {
            MutableStateFlow(loadGoalsFromPrefs(uid).sortedBy { it.targetDateEpochDay })
        }
    }

    @Synchronized
    fun observeSavingsContributions(uid: String): Flow<List<SavingsContribution>> {
        ensureSeeded(uid)
        return contributionsFlowMap.getOrPut(uid) {
            MutableStateFlow(loadContributionsFromPrefs(uid).sortedByDescending { it.dateEpochDay })
        }
    }

    @Synchronized
    fun observeRecurringTransactions(uid: String): Flow<List<RecurringTransaction>> {
        ensureSeeded(uid)
        return recurringFlowMap.getOrPut(uid) {
            MutableStateFlow(loadRecurringFromPrefs(uid).sortedBy { it.nextDueDateEpochDay })
        }
    }

    @Synchronized
    fun observeLoans(uid: String): Flow<List<LoanRecord>> {
        ensureSeeded(uid)
        return loansFlowMap.getOrPut(uid) {
            MutableStateFlow(sortLoans(loadLoansFromPrefs(uid)))
        }
    }

    @Synchronized
    fun observeLoanPayments(uid: String): Flow<List<LoanPayment>> {
        ensureSeeded(uid)
        return loanPaymentsFlowMap.getOrPut(uid) {
            MutableStateFlow(loadLoanPaymentsFromPrefs(uid).sortedByDescending { it.dateEpochDay })
        }
    }

    // ========================================================================
    // MUTATIONS WITH ATOMIC ACCOUNT BALANCE RECONCILIATION
    // ========================================================================

    @Synchronized
    fun updateSettings(uid: String, updated: UserSettings, markCustomName: Boolean = false) {
        val model = updated.copy(
            id = updated.id.ifBlank { "preferences" },
            userId = uid
        )
        if (markCustomName && updated.displayName.isNotBlank()) {
            prefs.edit().putBoolean(key(uid, "custom_display_name"), true).apply()
        }
        saveSettingsToPrefs(uid, model)
        refreshFlows(uid)
    }

    /**
     * When a user signs in with Google Auth after using the local default profile,
     * migrate any local transactions/goals/budgets/accounts into their Google cloud profile
     * so zero data is lost and everything gets saved to Firebase Firestore.
     */
    @Synchronized
    fun migrateDefaultUserToCloudUser(
        cloudUid: String,
        googleDisplayName: String,
        googleEmail: String,
        googlePhotoUrl: String
    ) {
        if (cloudUid == DEFAULT_LOCAL_USER_ID || cloudUid.isBlank()) return
        ensureSeeded(cloudUid, googleDisplayName, googleEmail, googlePhotoUrl)

        val localTxs = loadTransactionsFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localGoals = loadGoalsFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localContribs = loadContributionsFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localBudgets = loadBudgetsFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localRecurring = loadRecurringFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localLoans = loadLoansFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localLoanPayments = loadLoanPaymentsFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localAccounts = loadAccountsFromPrefs(DEFAULT_LOCAL_USER_ID)
        val localCategories = loadCategoriesFromPrefs(DEFAULT_LOCAL_USER_ID)

        val cloudTxs = loadTransactionsFromPrefs(cloudUid).toMutableList()
        var mergedAny = false
        for (tx in localTxs) {
            if (cloudTxs.none { it.id == tx.id }) {
                cloudTxs.add(tx.copy(userId = cloudUid))
                mergedAny = true
            }
        }
        if (mergedAny) {
            saveTransactionsToPrefs(cloudUid, cloudTxs)
        }

        // Merge custom accounts and reconcile balances
        val cloudAccounts = loadAccountsFromPrefs(cloudUid).toMutableList()
        for (acc in localAccounts) {
            val idx = cloudAccounts.indexOfFirst { it.id == acc.id }
            if (idx < 0) {
                cloudAccounts.add(acc.copy(userId = cloudUid))
            } else if (cloudAccounts[idx].currentBalancePaisa == 0L && acc.currentBalancePaisa != 0L) {
                cloudAccounts[idx] = acc.copy(userId = cloudUid)
            }
        }
        saveAccountsToPrefs(cloudUid, cloudAccounts)

        val cloudCategories = loadCategoriesFromPrefs(cloudUid).toMutableList()
        for (cat in localCategories) {
            if (cloudCategories.none { it.id == cat.id }) {
                cloudCategories.add(cat.copy(userId = cloudUid))
            }
        }
        saveCategoriesToPrefs(cloudUid, cloudCategories)

        val cloudBudgets = loadBudgetsFromPrefs(cloudUid).toMutableList()
        for (b in localBudgets) {
            if (cloudBudgets.none { it.id == b.id }) {
                cloudBudgets.add(b.copy(userId = cloudUid))
            }
        }
        saveBudgetsToPrefs(cloudUid, cloudBudgets)

        val cloudGoals = loadGoalsFromPrefs(cloudUid).toMutableList()
        for (g in localGoals) {
            if (cloudGoals.none { it.id == g.id }) {
                cloudGoals.add(g.copy(userId = cloudUid))
            }
        }
        saveGoalsToPrefs(cloudUid, cloudGoals)

        val cloudContribs = loadContributionsFromPrefs(cloudUid).toMutableList()
        for (c in localContribs) {
            if (cloudContribs.none { it.id == c.id }) {
                cloudContribs.add(c.copy(userId = cloudUid))
            }
        }
        saveContributionsToPrefs(cloudUid, cloudContribs)

        val cloudRecurring = loadRecurringFromPrefs(cloudUid).toMutableList()
        for (r in localRecurring) {
            if (cloudRecurring.none { it.id == r.id }) {
                cloudRecurring.add(r.copy(userId = cloudUid))
            }
        }
        saveRecurringToPrefs(cloudUid, cloudRecurring)

        val cloudLoans = loadLoansFromPrefs(cloudUid).toMutableList()
        for (l in localLoans) {
            if (cloudLoans.none { it.id == l.id }) {
                cloudLoans.add(l.copy(userId = cloudUid))
            }
        }
        saveLoansToPrefs(cloudUid, cloudLoans)

        val cloudLoanPayments = loadLoanPaymentsFromPrefs(cloudUid).toMutableList()
        for (lp in localLoanPayments) {
            if (cloudLoanPayments.none { it.id == lp.id }) {
                cloudLoanPayments.add(lp.copy(userId = cloudUid))
            }
        }
        saveLoanPaymentsToPrefs(cloudUid, cloudLoanPayments)

        refreshFlows(cloudUid)
    }

    @Synchronized
    fun mergeFromCloud(
        uid: String,
        cloudSettings: UserSettings?,
        cloudAccounts: List<Account>,
        cloudCategories: List<Category>,
        cloudTransactions: List<FinTransaction>,
        cloudBudgets: List<Budget>,
        cloudGoals: List<SavingsGoal>,
        cloudContribs: List<SavingsContribution>,
        cloudRecurring: List<RecurringTransaction>,
        cloudLoans: List<LoanRecord>,
        cloudLoanPayments: List<LoanPayment>
    ) {
        if (cloudSettings != null) {
            val localS = loadSettingsFromPrefs(uid)
            val hasCustomName = prefs.getBoolean(key(uid, "custom_display_name"), false)
            val merged = cloudSettings.copy(
                id = "preferences",
                userId = uid,
                displayName = if (hasCustomName && localS != null && localS.displayName.isNotBlank()) localS.displayName else cloudSettings.displayName
            )
            saveSettingsToPrefs(uid, merged)
        }
        if (cloudAccounts.isNotEmpty()) {
            val merged = loadAccountsFromPrefs(uid).toMutableList()
            for (item in cloudAccounts) {
                val idx = merged.indexOfFirst { it.id == item.id }
                if (idx >= 0) {
                    if (merged[idx].currentBalancePaisa == 0L && item.currentBalancePaisa != 0L) {
                        merged[idx] = item.copy(userId = uid)
                    }
                } else {
                    merged.add(item.copy(userId = uid))
                }
            }
            saveAccountsToPrefs(uid, merged)
        }
        if (cloudCategories.isNotEmpty()) {
            val merged = loadCategoriesFromPrefs(uid).toMutableList()
            for (item in cloudCategories) {
                if (merged.none { it.id == item.id }) merged.add(item.copy(userId = uid))
            }
            saveCategoriesToPrefs(uid, merged)
        }
        if (cloudTransactions.isNotEmpty()) {
            val merged = loadTransactionsFromPrefs(uid).toMutableList()
            for (item in cloudTransactions) {
                val idx = merged.indexOfFirst { it.id == item.id }
                if (idx >= 0) merged[idx] = item.copy(userId = uid) else merged.add(item.copy(userId = uid))
            }
            saveTransactionsToPrefs(uid, merged)
        }
        if (cloudBudgets.isNotEmpty()) {
            val merged = loadBudgetsFromPrefs(uid).toMutableList()
            for (item in cloudBudgets) {
                val idx = merged.indexOfFirst { it.id == item.id }
                if (idx >= 0) merged[idx] = item.copy(userId = uid) else merged.add(item.copy(userId = uid))
            }
            saveBudgetsToPrefs(uid, merged)
        }
        if (cloudGoals.isNotEmpty()) {
            val merged = loadGoalsFromPrefs(uid).toMutableList()
            for (item in cloudGoals) {
                val idx = merged.indexOfFirst { it.id == item.id }
                if (idx >= 0) merged[idx] = item.copy(userId = uid) else merged.add(item.copy(userId = uid))
            }
            saveGoalsToPrefs(uid, merged)
        }
        if (cloudContribs.isNotEmpty()) {
            val merged = loadContributionsFromPrefs(uid).toMutableList()
            for (item in cloudContribs) {
                if (merged.none { it.id == item.id }) merged.add(item.copy(userId = uid))
            }
            saveContributionsToPrefs(uid, merged)
        }
        if (cloudRecurring.isNotEmpty()) {
            val merged = loadRecurringFromPrefs(uid).toMutableList()
            for (item in cloudRecurring) {
                val idx = merged.indexOfFirst { it.id == item.id }
                if (idx >= 0) merged[idx] = item.copy(userId = uid) else merged.add(item.copy(userId = uid))
            }
            saveRecurringToPrefs(uid, merged)
        }
        if (cloudLoans.isNotEmpty()) {
            val merged = loadLoansFromPrefs(uid).toMutableList()
            for (item in cloudLoans) {
                val idx = merged.indexOfFirst { it.id == item.id }
                if (idx >= 0) merged[idx] = item.copy(userId = uid) else merged.add(item.copy(userId = uid))
            }
            saveLoansToPrefs(uid, merged)
        }
        if (cloudLoanPayments.isNotEmpty()) {
            val merged = loadLoanPaymentsFromPrefs(uid).toMutableList()
            for (item in cloudLoanPayments) {
                if (merged.none { it.id == item.id }) merged.add(item.copy(userId = uid))
            }
            saveLoanPaymentsToPrefs(uid, merged)
        }
        refreshFlows(uid)
    }

    @Synchronized
    fun getSettingsSnapshot(uid: String): UserSettings =
        loadSettingsFromPrefs(uid) ?: UserSettings(id = "preferences", userId = uid, themeMode = "light")

    @Synchronized
    fun getAccountsSnapshot(uid: String): List<Account> = loadAccountsFromPrefs(uid)

    @Synchronized
    fun getCategoriesSnapshot(uid: String): List<Category> = loadCategoriesFromPrefs(uid)

    @Synchronized
    fun getTransactionsSnapshot(uid: String): List<FinTransaction> = loadTransactionsFromPrefs(uid)

    @Synchronized
    fun getBudgetsSnapshot(uid: String): List<Budget> = loadBudgetsFromPrefs(uid)

    @Synchronized
    fun getGoalsSnapshot(uid: String): List<SavingsGoal> = loadGoalsFromPrefs(uid)

    @Synchronized
    fun getContributionsSnapshot(uid: String): List<SavingsContribution> = loadContributionsFromPrefs(uid)

    @Synchronized
    fun getRecurringSnapshot(uid: String): List<RecurringTransaction> = loadRecurringFromPrefs(uid)

    @Synchronized
    fun getLoansSnapshot(uid: String): List<LoanRecord> = loadLoansFromPrefs(uid)

    @Synchronized
    fun getLoanPaymentsSnapshot(uid: String): List<LoanPayment> = loadLoanPaymentsFromPrefs(uid)

    @Synchronized
    fun saveAccount(uid: String, account: Account): String {
        val list = loadAccountsFromPrefs(uid).toMutableList()
        val isNew = account.id.isBlank()
        val docId = if (isNew) "acc_${UUID.randomUUID().toString().take(8)}" else account.id
        val existingIdx = list.indexOfFirst { it.id == docId }
        if (existingIdx >= 0) {
            val existing = list[existingIdx]
            val initialDiff = account.initialBalancePaisa - existing.initialBalancePaisa
            val newCurrent = existing.currentBalancePaisa + initialDiff
            list[existingIdx] = account.copy(
                id = docId,
                userId = uid,
                currentBalancePaisa = newCurrent
            )
        } else {
            list.add(
                account.copy(
                    id = docId,
                    userId = uid,
                    currentBalancePaisa = account.initialBalancePaisa
                )
            )
        }
        saveAccountsToPrefs(uid, list)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun deleteAccount(uid: String, accountId: String) {
        val txs = loadTransactionsFromPrefs(uid)
        val hasLinkedTx = txs.any { it.accountId == accountId || it.toAccountId == accountId }
        if (hasLinkedTx) {
            throw IllegalStateException("Cannot delete an account that has existing transactions. Delete or reassign its transactions first.")
        }
        val list = loadAccountsFromPrefs(uid).filterNot { it.id == accountId }
        saveAccountsToPrefs(uid, list)
        refreshFlows(uid)
    }

    @Synchronized
    fun saveCategory(uid: String, category: Category): String {
        val list = loadCategoriesFromPrefs(uid).toMutableList()
        val isNew = category.id.isBlank()
        val docId = if (isNew) "cat_${UUID.randomUUID().toString().take(8)}" else category.id
        val idx = list.indexOfFirst { it.id == docId }
        val model = category.copy(id = docId, userId = uid)
        if (idx >= 0) list[idx] = model else list.add(model)
        saveCategoriesToPrefs(uid, list)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun deleteCategory(uid: String, categoryId: String) {
        val list = loadCategoriesFromPrefs(uid).filterNot { it.id == categoryId }
        saveCategoriesToPrefs(uid, list)
        refreshFlows(uid)
    }

    private fun computeBalanceDeltas(tx: FinTransaction, multiplier: Long): Map<String, Long> {
        val deltas = mutableMapOf<String, Long>()
        when (tx.type) {
            "INCOME" -> {
                if (tx.accountId.isNotBlank()) {
                    deltas[tx.accountId] = (deltas[tx.accountId] ?: 0L) + (tx.amountPaisa * multiplier)
                }
            }
            "EXPENSE" -> {
                if (tx.accountId.isNotBlank()) {
                    deltas[tx.accountId] = (deltas[tx.accountId] ?: 0L) - (tx.amountPaisa * multiplier)
                }
            }
            "TRANSFER" -> {
                if (tx.accountId.isNotBlank()) {
                    deltas[tx.accountId] = (deltas[tx.accountId] ?: 0L) - (tx.amountPaisa * multiplier)
                }
                if (tx.toAccountId.isNotBlank()) {
                    deltas[tx.toAccountId] = (deltas[tx.toAccountId] ?: 0L) + (tx.amountPaisa * multiplier)
                }
            }
        }
        return deltas
    }

    @Synchronized
    fun addTransaction(uid: String, tx: FinTransaction): String {
        require(tx.amountPaisa > 0L) { "Transaction amount must be greater than zero." }
        if (tx.type == "TRANSFER") {
            require(tx.accountId.isNotBlank() && tx.toAccountId.isNotBlank() && tx.accountId != tx.toAccountId) {
                "Transfer requires two distinct accounts."
            }
        }
        ensureSeeded(uid)
        val docId = tx.id.ifBlank { "tx_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val txs = loadTransactionsFromPrefs(uid).toMutableList()
        val accounts = loadAccountsFromPrefs(uid).toMutableList()

        val savedTx = tx.copy(id = docId, userId = uid)
        val existingIdx = txs.indexOfFirst { it.id == docId }
        if (existingIdx >= 0) {
            txs[existingIdx] = savedTx
        } else {
            txs.add(savedTx)
        }

        val deltas = computeBalanceDeltas(savedTx, 1L)
        applyAccountDeltas(uid, accounts, deltas)

        saveTransactionsToPrefs(uid, txs)
        saveAccountsToPrefs(uid, accounts)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun updateTransaction(uid: String, updatedTx: FinTransaction) {
        require(updatedTx.id.isNotBlank()) { "Transaction ID required for update." }
        require(updatedTx.amountPaisa > 0L) { "Transaction amount must be greater than zero." }
        ensureSeeded(uid)
        val txs = loadTransactionsFromPrefs(uid).toMutableList()
        val accounts = loadAccountsFromPrefs(uid).toMutableList()
        val idx = txs.indexOfFirst { it.id == updatedTx.id }
        if (idx < 0) {
            addTransaction(uid, updatedTx)
            return
        }
        val oldTx = txs[idx]
        val combinedDeltas = mutableMapOf<String, Long>()
        for ((accId, d) in computeBalanceDeltas(oldTx, -1L)) {
            combinedDeltas[accId] = (combinedDeltas[accId] ?: 0L) + d
        }
        for ((accId, d) in computeBalanceDeltas(updatedTx, 1L)) {
            combinedDeltas[accId] = (combinedDeltas[accId] ?: 0L) + d
        }
        txs[idx] = updatedTx.copy(userId = uid)
        applyAccountDeltas(uid, accounts, combinedDeltas)

        saveTransactionsToPrefs(uid, txs)
        saveAccountsToPrefs(uid, accounts)
        refreshFlows(uid)
    }

    @Synchronized
    fun deleteTransaction(uid: String, txId: String) {
        ensureSeeded(uid)
        val txs = loadTransactionsFromPrefs(uid).toMutableList()
        val accounts = loadAccountsFromPrefs(uid).toMutableList()
        val oldTx = txs.firstOrNull { it.id == txId } ?: return
        txs.removeAll { it.id == txId }

        val reversalDeltas = computeBalanceDeltas(oldTx, -1L)
        applyAccountDeltas(uid, accounts, reversalDeltas)

        saveTransactionsToPrefs(uid, txs)
        saveAccountsToPrefs(uid, accounts)
        refreshFlows(uid)
    }

    private fun applyAccountDeltas(uid: String, accounts: MutableList<Account>, deltas: Map<String, Long>) {
        for ((accId, delta) in deltas) {
            if (delta == 0L) continue
            val accIdx = accounts.indexOfFirst { it.id == accId }
            if (accIdx >= 0) {
                val acc = accounts[accIdx]
                accounts[accIdx] = acc.copy(currentBalancePaisa = acc.currentBalancePaisa + delta)
            } else {
                // Fallback if account id wasn't found: create or update default Cash account
                val cashIdx = accounts.indexOfFirst { it.id == "acc_cash" }
                if (cashIdx >= 0) {
                    val acc = accounts[cashIdx]
                    accounts[cashIdx] = acc.copy(currentBalancePaisa = acc.currentBalancePaisa + delta)
                } else {
                    accounts.add(
                        Account(
                            id = accId.ifBlank { "acc_cash" },
                            userId = uid,
                            name = "Cash",
                            type = "CASH",
                            initialBalancePaisa = 0L,
                            currentBalancePaisa = delta,
                            colorHex = "#10B981",
                            iconName = "payments",
                            isDefault = true
                        )
                    )
                }
            }
        }
    }

    @Synchronized
    fun saveBudget(uid: String, budget: Budget): String {
        require(budget.limitPaisa > 0L) { "Budget limit must be positive." }
        val list = loadBudgetsFromPrefs(uid).toMutableList()
        val docId = budget.id.ifBlank { "bgt_${budget.monthKey}_${budget.categoryId}" }
        val idx = list.indexOfFirst { it.id == docId || (it.monthKey == budget.monthKey && it.categoryId == budget.categoryId) }
        val model = budget.copy(id = docId, userId = uid)
        if (idx >= 0) list[idx] = model else list.add(model)
        saveBudgetsToPrefs(uid, list)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun deleteBudget(uid: String, budgetId: String) {
        val list = loadBudgetsFromPrefs(uid).filterNot { it.id == budgetId }
        saveBudgetsToPrefs(uid, list)
        refreshFlows(uid)
    }

    @Synchronized
    fun saveSavingsGoal(uid: String, goal: SavingsGoal): String {
        require(goal.targetAmountPaisa > 0L) { "Target amount must be greater than zero." }
        val list = loadGoalsFromPrefs(uid).toMutableList()
        val isNew = goal.id.isBlank()
        val docId = if (isNew) "goal_${UUID.randomUUID().toString().take(8)}" else goal.id
        val idx = list.indexOfFirst { it.id == docId }
        val model = goal.copy(id = docId, userId = uid)
        if (idx >= 0) list[idx] = model else list.add(model)
        saveGoalsToPrefs(uid, list)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun addSavingsContribution(
        uid: String,
        goalId: String,
        amountPaisa: Long,
        sourceAccountId: String,
        sourceAccountName: String,
        notes: String
    ) {
        require(amountPaisa > 0L) { "Contribution amount must be positive." }
        ensureSeeded(uid)
        val goals = loadGoalsFromPrefs(uid).toMutableList()
        val goalIdx = goals.indexOfFirst { it.id == goalId }
        require(goalIdx >= 0) { "Savings goal not found." }
        val goal = goals[goalIdx]
        goals[goalIdx] = goal.copy(currentSavedPaisa = goal.currentSavedPaisa + amountPaisa)

        val contribs = loadContributionsFromPrefs(uid).toMutableList()
        val contribId = "sc_${UUID.randomUUID().toString().take(10)}"
        contribs.add(
            SavingsContribution(
                id = contribId,
                userId = uid,
                goalId = goalId,
                goalName = goal.name,
                amountPaisa = amountPaisa,
                sourceAccountId = sourceAccountId,
                sourceAccountName = sourceAccountName,
                dateEpochDay = DhakaDateUtils.todayEpochDay(),
                notes = notes.take(500)
            )
        )

        if (sourceAccountId.isNotBlank()) {
            val accounts = loadAccountsFromPrefs(uid).toMutableList()
            applyAccountDeltas(uid, accounts, mapOf(sourceAccountId to -amountPaisa))
            saveAccountsToPrefs(uid, accounts)
        }

        saveGoalsToPrefs(uid, goals)
        saveContributionsToPrefs(uid, contribs)
        refreshFlows(uid)
    }

    @Synchronized
    fun deleteSavingsGoal(uid: String, goalId: String) {
        val list = loadGoalsFromPrefs(uid).filterNot { it.id == goalId }
        saveGoalsToPrefs(uid, list)
        refreshFlows(uid)
    }

    @Synchronized
    fun saveRecurringRule(uid: String, rule: RecurringTransaction): String {
        require(rule.amountPaisa > 0L) { "Recurring amount must be positive." }
        val list = loadRecurringFromPrefs(uid).toMutableList()
        val isNew = rule.id.isBlank()
        val docId = if (isNew) "rec_${UUID.randomUUID().toString().take(8)}" else rule.id
        val idx = list.indexOfFirst { it.id == docId }
        val model = rule.copy(id = docId, userId = uid)
        if (idx >= 0) list[idx] = model else list.add(model)
        saveRecurringToPrefs(uid, list)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun deleteRecurringRule(uid: String, ruleId: String) {
        val list = loadRecurringFromPrefs(uid).filterNot { it.id == ruleId }
        saveRecurringToPrefs(uid, list)
        refreshFlows(uid)
    }

    @Synchronized
    fun saveLoan(uid: String, loan: LoanRecord): String {
        require(loan.principalAmountPaisa > 0L) { "Loan principal must be greater than zero." }
        val list = loadLoansFromPrefs(uid).toMutableList()
        val isNew = loan.id.isBlank()
        val docId = if (isNew) "loan_${UUID.randomUUID().toString().take(8)}" else loan.id
        val status = if (loan.outstandingAmountPaisa <= 0L) "PAID" else "ACTIVE"
        val idx = list.indexOfFirst { it.id == docId }
        val model = loan.copy(id = docId, userId = uid, status = status)
        if (idx >= 0) list[idx] = model else list.add(model)
        saveLoansToPrefs(uid, list)
        refreshFlows(uid)
        return docId
    }

    @Synchronized
    fun addLoanPayment(
        uid: String,
        loan: LoanRecord,
        paymentAmountPaisa: Long,
        accountId: String,
        accountName: String,
        notes: String
    ) {
        require(paymentAmountPaisa > 0L) { "Payment amount must be positive." }
        ensureSeeded(uid)
        val loans = loadLoansFromPrefs(uid).toMutableList()
        val idx = loans.indexOfFirst { it.id == loan.id }
        require(idx >= 0) { "Loan record not found." }
        val existingLoan = loans[idx]
        val newOutstanding = (existingLoan.outstandingAmountPaisa - paymentAmountPaisa).coerceAtLeast(0L)
        val newStatus = if (newOutstanding == 0L) "PAID" else "ACTIVE"
        loans[idx] = existingLoan.copy(
            outstandingAmountPaisa = newOutstanding,
            status = newStatus
        )

        val payments = loadLoanPaymentsFromPrefs(uid).toMutableList()
        val paymentId = "lp_${UUID.randomUUID().toString().take(10)}"
        payments.add(
            LoanPayment(
                id = paymentId,
                userId = uid,
                loanId = existingLoan.id,
                personOrOrg = existingLoan.personOrOrg,
                amountPaisa = paymentAmountPaisa,
                accountId = accountId,
                accountName = accountName,
                dateEpochDay = DhakaDateUtils.todayEpochDay(),
                notes = notes.take(500)
            )
        )

        if (accountId.isNotBlank()) {
            val accounts = loadAccountsFromPrefs(uid).toMutableList()
            val delta = if (existingLoan.type == "BORROWED") -paymentAmountPaisa else paymentAmountPaisa
            applyAccountDeltas(uid, accounts, mapOf(accountId to delta))
            saveAccountsToPrefs(uid, accounts)
        }

        saveLoansToPrefs(uid, loans)
        saveLoanPaymentsToPrefs(uid, payments)
        refreshFlows(uid)
    }

    @Synchronized
    fun deleteLoan(uid: String, loanId: String) {
        val list = loadLoansFromPrefs(uid).filterNot { it.id == loanId }
        saveLoansToPrefs(uid, list)
        refreshFlows(uid)
    }

    // ========================================================================
    // SORTING HELPERS
    // ========================================================================

    private fun sortAccounts(list: List<Account>): List<Account> =
        list.sortedWith(compareByDescending<Account> { it.isDefault }.thenBy { it.name })

    private fun sortCategories(list: List<Category>): List<Category> =
        list.sortedWith(compareBy<Category> { it.isCustom }.thenBy { it.name })

    private fun sortTransactions(list: List<FinTransaction>): List<FinTransaction> =
        list.sortedWith(compareByDescending<FinTransaction> { it.dateEpochDay }.thenByDescending { it.timestampMillis })

    private fun sortLoans(list: List<LoanRecord>): List<LoanRecord> =
        list.sortedWith(compareBy<LoanRecord> { it.status == "PAID" }.thenBy { it.dueDateEpochDay })

    // ========================================================================
    // JSON SERIALIZATION TO SHAREDPREFERENCES
    // ========================================================================

    private fun key(uid: String, collection: String) = "${uid}_$collection"

    private fun loadSettingsFromPrefs(uid: String): UserSettings? {
        val raw = prefs.getString(key(uid, "settings"), null) ?: return null
        return try {
            val o = JSONObject(raw)
            UserSettings(
                id = o.optString("id", "preferences"),
                userId = o.optString("userId", uid),
                displayName = o.optString("displayName", "Personal BM User"),
                email = o.optString("email", ""),
                currencyCode = o.optString("currencyCode", "BDT"),
                currencySymbol = o.optString("currencySymbol", "৳"),
                timezone = o.optString("timezone", "Asia/Dhaka"),
                language = o.optString("language", "en"),
                themeMode = o.optString("themeMode", "light"),
                defaultAccountId = o.optString("defaultAccountId", "acc_cash"),
                weekStartDay = o.optString("weekStartDay", "SATURDAY"),
                seededDefaults = o.optBoolean("seededDefaults", true)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun saveSettingsToPrefs(uid: String, s: UserSettings) {
        val o = JSONObject().apply {
            put("id", s.id.ifBlank { "preferences" })
            put("userId", uid)
            put("displayName", s.displayName)
            put("email", s.email)
            put("currencyCode", s.currencyCode)
            put("currencySymbol", s.currencySymbol)
            put("timezone", s.timezone)
            put("language", s.language)
            put("themeMode", s.themeMode)
            put("defaultAccountId", s.defaultAccountId)
            put("weekStartDay", s.weekStartDay)
            put("seededDefaults", s.seededDefaults)
        }
        prefs.edit().putString(key(uid, "settings"), o.toString()).apply()
    }

    private fun loadAccountsFromPrefs(uid: String): List<Account> {
        val raw = prefs.getString(key(uid, "accounts"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Account(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    name = o.optString("name"),
                    type = o.optString("type", "CASH"),
                    initialBalancePaisa = o.optLong("initialBalancePaisa", 0L),
                    currentBalancePaisa = o.optLong("currentBalancePaisa", 0L),
                    colorHex = o.optString("colorHex", "#2563EB"),
                    iconName = o.optString("iconName", "wallet"),
                    isDefault = o.optBoolean("isDefault", false)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveAccountsToPrefs(uid: String, list: List<Account>) {
        val arr = JSONArray()
        list.forEach { a ->
            arr.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("userId", uid)
                    put("name", a.name)
                    put("type", a.type)
                    put("initialBalancePaisa", a.initialBalancePaisa)
                    put("currentBalancePaisa", a.currentBalancePaisa)
                    put("colorHex", a.colorHex)
                    put("iconName", a.iconName)
                    put("isDefault", a.isDefault)
                }
            )
        }
        prefs.edit().putString(key(uid, "accounts"), arr.toString()).apply()
    }

    private fun loadCategoriesFromPrefs(uid: String): List<Category> {
        val raw = prefs.getString(key(uid, "categories"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Category(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    name = o.optString("name"),
                    nameBn = o.optString("nameBn"),
                    type = o.optString("type", "EXPENSE"),
                    iconName = o.optString("iconName", "category"),
                    colorHex = o.optString("colorHex", "#2563EB"),
                    isCustom = o.optBoolean("isCustom", false)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCategoriesToPrefs(uid: String, list: List<Category>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("userId", uid)
                    put("name", c.name)
                    put("nameBn", c.nameBn)
                    put("type", c.type)
                    put("iconName", c.iconName)
                    put("colorHex", c.colorHex)
                    put("isCustom", c.isCustom)
                }
            )
        }
        prefs.edit().putString(key(uid, "categories"), arr.toString()).apply()
    }

    private fun loadTransactionsFromPrefs(uid: String): List<FinTransaction> {
        val raw = prefs.getString(key(uid, "transactions"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                FinTransaction(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    title = o.optString("title"),
                    amountPaisa = o.optLong("amountPaisa", 0L),
                    type = o.optString("type", "EXPENSE"),
                    categoryId = o.optString("categoryId"),
                    categoryName = o.optString("categoryName"),
                    accountId = o.optString("accountId"),
                    accountName = o.optString("accountName"),
                    toAccountId = o.optString("toAccountId"),
                    toAccountName = o.optString("toAccountName"),
                    dateEpochDay = o.optLong("dateEpochDay", DhakaDateUtils.todayEpochDay()),
                    timeFormatted = o.optString("timeFormatted", "12:00"),
                    timestampMillis = o.optLong("timestampMillis", 0L),
                    paymentMethod = o.optString("paymentMethod", "Cash"),
                    notes = o.optString("notes"),
                    attachmentUri = o.optString("attachmentUri"),
                    recurringRuleId = o.optString("recurringRuleId")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveTransactionsToPrefs(uid: String, list: List<FinTransaction>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(
                JSONObject().apply {
                    put("id", t.id)
                    put("userId", uid)
                    put("title", t.title)
                    put("amountPaisa", t.amountPaisa)
                    put("type", t.type)
                    put("categoryId", t.categoryId)
                    put("categoryName", t.categoryName)
                    put("accountId", t.accountId)
                    put("accountName", t.accountName)
                    put("toAccountId", t.toAccountId)
                    put("toAccountName", t.toAccountName)
                    put("dateEpochDay", t.dateEpochDay)
                    put("timeFormatted", t.timeFormatted)
                    put("timestampMillis", t.timestampMillis)
                    put("paymentMethod", t.paymentMethod)
                    put("notes", t.notes)
                    put("attachmentUri", t.attachmentUri)
                    put("recurringRuleId", t.recurringRuleId)
                }
            )
        }
        prefs.edit().putString(key(uid, "transactions"), arr.toString()).apply()
    }

    private fun loadBudgetsFromPrefs(uid: String): List<Budget> {
        val raw = prefs.getString(key(uid, "budgets"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Budget(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    monthKey = o.optString("monthKey"),
                    categoryId = o.optString("categoryId"),
                    categoryName = o.optString("categoryName"),
                    limitPaisa = o.optLong("limitPaisa", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveBudgetsToPrefs(uid: String, list: List<Budget>) {
        val arr = JSONArray()
        list.forEach { b ->
            arr.put(
                JSONObject().apply {
                    put("id", b.id)
                    put("userId", uid)
                    put("monthKey", b.monthKey)
                    put("categoryId", b.categoryId)
                    put("categoryName", b.categoryName)
                    put("limitPaisa", b.limitPaisa)
                }
            )
        }
        prefs.edit().putString(key(uid, "budgets"), arr.toString()).apply()
    }

    private fun loadGoalsFromPrefs(uid: String): List<SavingsGoal> {
        val raw = prefs.getString(key(uid, "savings_goals"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavingsGoal(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    name = o.optString("name"),
                    targetAmountPaisa = o.optLong("targetAmountPaisa", 0L),
                    currentSavedPaisa = o.optLong("currentSavedPaisa", 0L),
                    targetDateEpochDay = o.optLong("targetDateEpochDay", 0L),
                    description = o.optString("description"),
                    colorHex = o.optString("colorHex", "#10B981"),
                    iconName = o.optString("iconName", "savings")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveGoalsToPrefs(uid: String, list: List<SavingsGoal>) {
        val arr = JSONArray()
        list.forEach { g ->
            arr.put(
                JSONObject().apply {
                    put("id", g.id)
                    put("userId", uid)
                    put("name", g.name)
                    put("targetAmountPaisa", g.targetAmountPaisa)
                    put("currentSavedPaisa", g.currentSavedPaisa)
                    put("targetDateEpochDay", g.targetDateEpochDay)
                    put("description", g.description)
                    put("colorHex", g.colorHex)
                    put("iconName", g.iconName)
                }
            )
        }
        prefs.edit().putString(key(uid, "savings_goals"), arr.toString()).apply()
    }

    private fun loadContributionsFromPrefs(uid: String): List<SavingsContribution> {
        val raw = prefs.getString(key(uid, "savings_contributions"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavingsContribution(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    goalId = o.optString("goalId"),
                    goalName = o.optString("goalName"),
                    amountPaisa = o.optLong("amountPaisa", 0L),
                    sourceAccountId = o.optString("sourceAccountId"),
                    sourceAccountName = o.optString("sourceAccountName"),
                    dateEpochDay = o.optLong("dateEpochDay", 0L),
                    notes = o.optString("notes")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveContributionsToPrefs(uid: String, list: List<SavingsContribution>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("userId", uid)
                    put("goalId", c.goalId)
                    put("goalName", c.goalName)
                    put("amountPaisa", c.amountPaisa)
                    put("sourceAccountId", c.sourceAccountId)
                    put("sourceAccountName", c.sourceAccountName)
                    put("dateEpochDay", c.dateEpochDay)
                    put("notes", c.notes)
                }
            )
        }
        prefs.edit().putString(key(uid, "savings_contributions"), arr.toString()).apply()
    }

    private fun loadRecurringFromPrefs(uid: String): List<RecurringTransaction> {
        val raw = prefs.getString(key(uid, "recurring_transactions"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                RecurringTransaction(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    title = o.optString("title"),
                    amountPaisa = o.optLong("amountPaisa", 0L),
                    type = o.optString("type", "EXPENSE"),
                    categoryId = o.optString("categoryId"),
                    categoryName = o.optString("categoryName"),
                    accountId = o.optString("accountId"),
                    accountName = o.optString("accountName"),
                    frequency = o.optString("frequency", "MONTHLY"),
                    nextDueDateEpochDay = o.optLong("nextDueDateEpochDay", 0L),
                    lastProcessedDateEpochDay = o.optLong("lastProcessedDateEpochDay", 0L),
                    isActive = o.optBoolean("isActive", true),
                    reminderEnabled = o.optBoolean("reminderEnabled", true),
                    notes = o.optString("notes")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveRecurringToPrefs(uid: String, list: List<RecurringTransaction>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("userId", uid)
                    put("title", r.title)
                    put("amountPaisa", r.amountPaisa)
                    put("type", r.type)
                    put("categoryId", r.categoryId)
                    put("categoryName", r.categoryName)
                    put("accountId", r.accountId)
                    put("accountName", r.accountName)
                    put("frequency", r.frequency)
                    put("nextDueDateEpochDay", r.nextDueDateEpochDay)
                    put("lastProcessedDateEpochDay", r.lastProcessedDateEpochDay)
                    put("isActive", r.isActive)
                    put("reminderEnabled", r.reminderEnabled)
                    put("notes", r.notes)
                }
            )
        }
        prefs.edit().putString(key(uid, "recurring_transactions"), arr.toString()).apply()
    }

    private fun loadLoansFromPrefs(uid: String): List<LoanRecord> {
        val raw = prefs.getString(key(uid, "loans"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LoanRecord(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    personOrOrg = o.optString("personOrOrg"),
                    type = o.optString("type", "BORROWED"),
                    principalAmountPaisa = o.optLong("principalAmountPaisa", 0L),
                    outstandingAmountPaisa = o.optLong("outstandingAmountPaisa", 0L),
                    startDateEpochDay = o.optLong("startDateEpochDay", 0L),
                    dueDateEpochDay = o.optLong("dueDateEpochDay", 0L),
                    accountId = o.optString("accountId"),
                    accountName = o.optString("accountName"),
                    status = o.optString("status", "ACTIVE"),
                    notes = o.optString("notes")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveLoansToPrefs(uid: String, list: List<LoanRecord>) {
        val arr = JSONArray()
        list.forEach { l ->
            arr.put(
                JSONObject().apply {
                    put("id", l.id)
                    put("userId", uid)
                    put("personOrOrg", l.personOrOrg)
                    put("type", l.type)
                    put("principalAmountPaisa", l.principalAmountPaisa)
                    put("outstandingAmountPaisa", l.outstandingAmountPaisa)
                    put("startDateEpochDay", l.startDateEpochDay)
                    put("dueDateEpochDay", l.dueDateEpochDay)
                    put("accountId", l.accountId)
                    put("accountName", l.accountName)
                    put("status", l.status)
                    put("notes", l.notes)
                }
            )
        }
        prefs.edit().putString(key(uid, "loans"), arr.toString()).apply()
    }

    private fun loadLoanPaymentsFromPrefs(uid: String): List<LoanPayment> {
        val raw = prefs.getString(key(uid, "loan_payments"), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LoanPayment(
                    id = o.optString("id"),
                    userId = o.optString("userId", uid),
                    loanId = o.optString("loanId"),
                    personOrOrg = o.optString("personOrOrg"),
                    amountPaisa = o.optLong("amountPaisa", 0L),
                    accountId = o.optString("accountId"),
                    accountName = o.optString("accountName"),
                    dateEpochDay = o.optLong("dateEpochDay", 0L),
                    notes = o.optString("notes")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveLoanPaymentsToPrefs(uid: String, list: List<LoanPayment>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("userId", uid)
                    put("loanId", p.loanId)
                    put("personOrOrg", p.personOrOrg)
                    put("amountPaisa", p.amountPaisa)
                    put("accountId", p.accountId)
                    put("accountName", p.accountName)
                    put("dateEpochDay", p.dateEpochDay)
                    put("notes", p.notes)
                }
            )
        }
        prefs.edit().putString(key(uid, "loan_payments"), arr.toString()).apply()
    }

    companion object {
        const val DEFAULT_LOCAL_USER_ID = "local_default_user"
        private const val PREFS_NAME = "personal_bm_local_store"
    }
}
