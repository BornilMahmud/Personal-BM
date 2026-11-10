package com.example.data.repository

import android.content.Context
import com.example.R
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PersonalBmRepository(
    private val db: FirebaseFirestore
) {
    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    // ========================================================================
    // REAL-TIME OBSERVATION STREAMS (Scoped with whereEqualTo("userId", uid))
    // ========================================================================

    fun observeSettings(uid: String): Flow<UserSettings?> {
        val path = "users/$uid/settings"
        return userDoc(uid).collection("settings")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.toObject(
                    UserSettings::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeAccounts(uid: String): Flow<List<Account>> {
        val path = "users/$uid/accounts"
        return userDoc(uid).collection("accounts")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(compareByDescending<Account> { it.isDefault }.thenBy { it.name })
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeCategories(uid: String): Flow<List<Category>> {
        val path = "users/$uid/categories"
        return userDoc(uid).collection("categories")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(compareBy<Category> { it.isCustom }.thenBy { it.name })
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeTransactions(uid: String): Flow<List<FinTransaction>> {
        val path = "users/$uid/transactions"
        return userDoc(uid).collection("transactions")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FinTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(
                        compareByDescending<FinTransaction> { it.dateEpochDay }
                            .thenByDescending { it.timestampMillis }
                    )
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeBudgets(uid: String): Flow<List<Budget>> {
        val path = "users/$uid/budgets"
        return userDoc(uid).collection("budgets")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Budget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeSavingsGoals(uid: String): Flow<List<SavingsGoal>> {
        val path = "users/$uid/savings_goals"
        return userDoc(uid).collection("savings_goals")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SavingsGoal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.targetDateEpochDay }
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeSavingsContributions(uid: String): Flow<List<SavingsContribution>> {
        val path = "users/$uid/savings_contributions"
        return userDoc(uid).collection("savings_contributions")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SavingsContribution::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeRecurringTransactions(uid: String): Flow<List<RecurringTransaction>> {
        val path = "users/$uid/recurring_transactions"
        return userDoc(uid).collection("recurring_transactions")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RecurringTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.nextDueDateEpochDay }
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeLoans(uid: String): Flow<List<LoanRecord>> {
        val path = "users/$uid/loans"
        return userDoc(uid).collection("loans")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(LoanRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(compareBy<LoanRecord> { it.status == "PAID" }.thenBy { it.dueDateEpochDay })
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    fun observeLoanPayments(uid: String): Flow<List<LoanPayment>> {
        val path = "users/$uid/loan_payments"
        return userDoc(uid).collection("loan_payments")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(LoanPayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }
            .catch { e -> logIfSignedIn(e, uid, OperationType.LIST, path); throw e }
    }

    private fun logIfSignedIn(error: Throwable, uid: String, op: OperationType, path: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    // ========================================================================
    // INITIAL SEEDING (Pre-loaded BDT Accounts & Categories)
    // ========================================================================

    suspend fun ensureDefaultsSeeded(displayName: String, email: String) {
        val uid = requireUserId()
        val settingsRef = userDoc(uid).collection("settings").document("preferences")
        try {
            val existingSettingsSnap = settingsRef.get().await()
            if (existingSettingsSnap.exists()) {
                val existing = existingSettingsSnap.toObject(
                    UserSettings::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (existing?.seededDefaults == true) return
            }

            val batch = db.batch()
            val settingsModel = UserSettings(
                id = "preferences",
                userId = uid,
                displayName = displayName.take(120),
                email = email.take(254),
                currencyCode = "BDT",
                currencySymbol = "৳",
                timezone = "Asia/Dhaka",
                language = "en",
                themeMode = "light",
                defaultAccountId = "acc_cash",
                weekStartDay = "SATURDAY",
                seededDefaults = true,
                createdAt = null,
                updatedAt = null
            )
            batch.set(settingsRef, settingsModel)

            for ((docId, acc) in DefaultSeedData.defaultAccounts(uid)) {
                val accRef = userDoc(uid).collection("accounts").document(docId)
                batch.set(accRef, acc.copy(createdAt = null, updatedAt = null))
            }

            for ((docId, cat) in DefaultSeedData.defaultCategories(uid)) {
                val catRef = userDoc(uid).collection("categories").document(docId)
                batch.set(catRef, cat.copy(createdAt = null, updatedAt = null))
            }

            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, settingsRef.path)
            throw e
        }
    }

    // ========================================================================
    // USER SETTINGS
    // ========================================================================

    suspend fun updateSettings(updated: UserSettings) {
        val uid = requireUserId()
        val docId = updated.id.ifBlank { "preferences" }
        val docRef = userDoc(uid).collection("settings").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val current = snap.toObject(UserSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val preservedCreatedAt = current?.createdAt ?: updated.createdAt
                docRef.set(
                    updated.copy(
                        id = docId,
                        userId = uid,
                        createdAt = preservedCreatedAt,
                        updatedAt = null
                    )
                ).await()
            } else {
                docRef.set(
                    updated.copy(
                        id = docId,
                        userId = uid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // ========================================================================
    // ACCOUNTS & WALLETS CRUD
    // ========================================================================

    suspend fun saveAccount(account: Account): String {
        val uid = requireUserId()
        val isNew = account.id.isBlank()
        val docId = if (isNew) "acc_${UUID.randomUUID().toString().take(8)}" else account.id
        val docRef = userDoc(uid).collection("accounts").document(docId)
        try {
            if (isNew) {
                val toCreate = account.copy(
                    id = docId,
                    userId = uid,
                    currentBalancePaisa = account.initialBalancePaisa,
                    createdAt = null,
                    updatedAt = null
                )
                docRef.set(toCreate).await()
            } else {
                val snap = docRef.get().await()
                val existing = snap.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val initialDiff = account.initialBalancePaisa - (existing?.initialBalancePaisa ?: account.initialBalancePaisa)
                val newCurrent = (existing?.currentBalancePaisa ?: account.currentBalancePaisa) + initialDiff
                val toUpdate = account.copy(
                    id = docId,
                    userId = uid,
                    currentBalancePaisa = newCurrent,
                    createdAt = existing?.createdAt ?: account.createdAt,
                    updatedAt = null
                )
                docRef.set(toUpdate).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAccount(accountId: String, existingTransactions: List<FinTransaction>) {
        val uid = requireUserId()
        val hasLinkedTx = existingTransactions.any {
            it.accountId == accountId || it.toAccountId == accountId
        }
        if (hasLinkedTx) {
            throw IllegalStateException("Cannot delete an account that has existing transactions. Delete or reassign its transactions first.")
        }
        val docRef = userDoc(uid).collection("accounts").document(accountId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ========================================================================
    // CATEGORIES CRUD
    // ========================================================================

    suspend fun saveCategory(category: Category): String {
        val uid = requireUserId()
        val isNew = category.id.isBlank()
        val docId = if (isNew) "cat_${UUID.randomUUID().toString().take(8)}" else category.id
        val docRef = userDoc(uid).collection("categories").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    category.copy(
                        id = docId,
                        userId = uid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            } else {
                val snap = docRef.get().await()
                val existing = snap.toObject(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                docRef.set(
                    category.copy(
                        id = docId,
                        userId = uid,
                        createdAt = existing?.createdAt ?: category.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("categories").document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ========================================================================
    // TRANSACTIONS CRUD WITH ATOMIC ACCOUNT BALANCE RECONCILIATION
    // Rules 1-6:
    // 1. Expense reduces associated account balance
    // 2. Income increases associated account balance
    // 3. Transfer decreases source account and increases destination account
    // 4. Editing reverses prior effect and applies new effect
    // 5. Deleting reverses financial effect
    // ========================================================================

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

    suspend fun addTransaction(tx: FinTransaction): String {
        val uid = requireUserId()
        require(tx.amountPaisa > 0L) { "Transaction amount must be greater than zero." }
        if (tx.type == "TRANSFER") {
            require(tx.accountId.isNotBlank() && tx.toAccountId.isNotBlank() && tx.accountId != tx.toAccountId) {
                "Transfer requires two distinct accounts."
            }
        }
        val docId = tx.id.ifBlank { "tx_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val txRef = userDoc(uid).collection("transactions").document(docId)
        try {
            val deltas = computeBalanceDeltas(tx, 1L)
            val accountSnaps = deltas.keys.associateWith { accId ->
                userDoc(uid).collection("accounts").document(accId).get().await()
            }

            val batch = db.batch()
            batch.set(
                txRef,
                tx.copy(
                    id = docId,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            )

            for ((accId, delta) in deltas) {
                if (delta == 0L) continue
                val snap = accountSnaps[accId]
                val accObj = snap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (accObj != null) {
                    val accRef = userDoc(uid).collection("accounts").document(accId)
                    batch.set(
                        accRef,
                        accObj.copy(
                            id = accId,
                            userId = uid,
                            currentBalancePaisa = accObj.currentBalancePaisa + delta,
                            updatedAt = null
                        )
                    )
                }
            }
            batch.commit().await()
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, txRef.path)
            throw e
        }
    }

    suspend fun updateTransaction(updatedTx: FinTransaction) {
        val uid = requireUserId()
        require(updatedTx.id.isNotBlank()) { "Transaction ID required for update." }
        require(updatedTx.amountPaisa > 0L) { "Transaction amount must be greater than zero." }
        val txRef = userDoc(uid).collection("transactions").document(updatedTx.id)
        try {
            val existingSnap = txRef.get().await()
            val oldTx = existingSnap.toObject(FinTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Original transaction not found.")

            val combinedDeltas = mutableMapOf<String, Long>()
            // 1. Reverse old transaction effect (-1L)
            for ((accId, d) in computeBalanceDeltas(oldTx, -1L)) {
                combinedDeltas[accId] = (combinedDeltas[accId] ?: 0L) + d
            }
            // 2. Apply updated transaction effect (+1L)
            for ((accId, d) in computeBalanceDeltas(updatedTx, 1L)) {
                combinedDeltas[accId] = (combinedDeltas[accId] ?: 0L) + d
            }

            val accountSnaps = combinedDeltas.keys.associateWith { accId ->
                userDoc(uid).collection("accounts").document(accId).get().await()
            }

            val batch = db.batch()
            batch.set(
                txRef,
                updatedTx.copy(
                    userId = uid,
                    createdAt = oldTx.createdAt,
                    updatedAt = null
                )
            )

            for ((accId, delta) in combinedDeltas) {
                if (delta == 0L) continue
                val snap = accountSnaps[accId]
                val accObj = snap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (accObj != null) {
                    val accRef = userDoc(uid).collection("accounts").document(accId)
                    batch.set(
                        accRef,
                        accObj.copy(
                            id = accId,
                            userId = uid,
                            currentBalancePaisa = accObj.currentBalancePaisa + delta,
                            updatedAt = null
                        )
                    )
                }
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, txRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(txId: String) {
        val uid = requireUserId()
        val txRef = userDoc(uid).collection("transactions").document(txId)
        try {
            val existingSnap = txRef.get().await()
            val oldTx = existingSnap.toObject(FinTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: return

            val reversalDeltas = computeBalanceDeltas(oldTx, -1L)
            val accountSnaps = reversalDeltas.keys.associateWith { accId ->
                userDoc(uid).collection("accounts").document(accId).get().await()
            }

            val batch = db.batch()
            batch.delete(txRef)

            for ((accId, delta) in reversalDeltas) {
                if (delta == 0L) continue
                val snap = accountSnaps[accId]
                val accObj = snap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (accObj != null) {
                    val accRef = userDoc(uid).collection("accounts").document(accId)
                    batch.set(
                        accRef,
                        accObj.copy(
                            id = accId,
                            userId = uid,
                            currentBalancePaisa = accObj.currentBalancePaisa + delta,
                            updatedAt = null
                        )
                    )
                }
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, txRef.path)
            throw e
        }
    }

    // ========================================================================
    // BUDGETS CRUD
    // ========================================================================

    suspend fun saveBudget(budget: Budget): String {
        val uid = requireUserId()
        require(budget.limitPaisa > 0L) { "Budget limit must be positive." }
        val docId = budget.id.ifBlank { "bgt_${budget.monthKey}_${budget.categoryId}" }
        val docRef = userDoc(uid).collection("budgets").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(Budget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                docRef.set(
                    budget.copy(
                        id = docId,
                        userId = uid,
                        createdAt = existing?.createdAt ?: budget.createdAt,
                        updatedAt = null
                    )
                ).await()
            } else {
                docRef.set(
                    budget.copy(
                        id = docId,
                        userId = uid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBudget(budgetId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("budgets").document(budgetId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ========================================================================
    // SAVINGS GOALS & CONTRIBUTIONS
    // Rule: Do not double-count goal contributions as income.
    // ========================================================================

    suspend fun saveSavingsGoal(goal: SavingsGoal): String {
        val uid = requireUserId()
        require(goal.targetAmountPaisa > 0L) { "Target amount must be greater than zero." }
        val isNew = goal.id.isBlank()
        val docId = if (isNew) "goal_${UUID.randomUUID().toString().take(8)}" else goal.id
        val docRef = userDoc(uid).collection("savings_goals").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    goal.copy(
                        id = docId,
                        userId = uid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            } else {
                val snap = docRef.get().await()
                val existing = snap.toObject(SavingsGoal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                docRef.set(
                    goal.copy(
                        id = docId,
                        userId = uid,
                        createdAt = existing?.createdAt ?: goal.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun addSavingsContribution(
        goalId: String,
        amountPaisa: Long,
        sourceAccountId: String,
        sourceAccountName: String,
        notes: String
    ) {
        val uid = requireUserId()
        require(amountPaisa > 0L) { "Contribution amount must be positive." }
        val goalRef = userDoc(uid).collection("savings_goals").document(goalId)
        val contribId = "sc_${UUID.randomUUID().toString().take(10)}"
        val contribRef = userDoc(uid).collection("savings_contributions").document(contribId)
        try {
            val goalSnap = goalRef.get().await()
            val goal = goalSnap.toObject(SavingsGoal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Savings goal not found.")

            val accSnap = if (sourceAccountId.isNotBlank()) {
                userDoc(uid).collection("accounts").document(sourceAccountId).get().await()
            } else null

            val batch = db.batch()
            // 1. Update goal's saved amount
            batch.set(
                goalRef,
                goal.copy(
                    id = goalId,
                    userId = uid,
                    currentSavedPaisa = goal.currentSavedPaisa + amountPaisa,
                    updatedAt = null
                )
            )
            // 2. Record contribution history (not counted as income!)
            batch.set(
                contribRef,
                SavingsContribution(
                    id = contribId,
                    userId = uid,
                    goalId = goalId,
                    goalName = goal.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = sourceAccountId,
                    sourceAccountName = sourceAccountName,
                    dateEpochDay = DhakaDateUtils.todayEpochDay(),
                    notes = notes.take(500),
                    createdAt = null,
                    updatedAt = null
                )
            )
            // 3. If a source account was selected, deduct allocated savings from that account balance
            val accObj = accSnap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (accObj != null && sourceAccountId.isNotBlank()) {
                val accRef = userDoc(uid).collection("accounts").document(sourceAccountId)
                batch.set(
                    accRef,
                    accObj.copy(
                        id = sourceAccountId,
                        userId = uid,
                        currentBalancePaisa = accObj.currentBalancePaisa - amountPaisa,
                        updatedAt = null
                    )
                )
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, goalRef.path)
            throw e
        }
    }

    suspend fun deleteSavingsGoal(goalId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("savings_goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ========================================================================
    // RECURRING TRANSACTIONS & IDEMPOTENT AUTO-POSTING ENGINE
    // ========================================================================

    suspend fun saveRecurringRule(rule: RecurringTransaction): String {
        val uid = requireUserId()
        require(rule.amountPaisa > 0L) { "Recurring amount must be positive." }
        val isNew = rule.id.isBlank()
        val docId = if (isNew) "rec_${UUID.randomUUID().toString().take(8)}" else rule.id
        val docRef = userDoc(uid).collection("recurring_transactions").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    rule.copy(
                        id = docId,
                        userId = uid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            } else {
                val snap = docRef.get().await()
                val existing = snap.toObject(RecurringTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                docRef.set(
                    rule.copy(
                        id = docId,
                        userId = uid,
                        createdAt = existing?.createdAt ?: rule.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleRecurringRule(rule: RecurringTransaction) {
        saveRecurringRule(rule.copy(isActive = !rule.isActive))
    }

    suspend fun deleteRecurringRule(ruleId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("recurring_transactions").document(ruleId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    /**
     * Checks all active recurring transactions whose nextDueDateEpochDay <= todayEpochDay.
     * Uses deterministic transaction document IDs ("tx_rec_${rule.id}_${dueEpochDay}")
     * so reopening the app never duplicates an already-posted recurring transaction.
     * Returns the count of newly posted recurring transactions.
     */
    suspend fun processDueRecurringRules(rules: List<RecurringTransaction>): Int {
        val uid = requireUserId()
        val today = DhakaDateUtils.todayEpochDay()
        val dueRules = rules.filter {
            it.isActive && it.nextDueDateEpochDay in 1..today && it.lastProcessedDateEpochDay < it.nextDueDateEpochDay
        }
        if (dueRules.isEmpty()) return 0

        var postedCount = 0
        for (rule in dueRules) {
            var dueDay = rule.nextDueDateEpochDay
            var lastProcessed = rule.lastProcessedDateEpochDay
            var safetyCounter = 0

            while (dueDay <= today && safetyCounter < 12) {
                safetyCounter++
                val deterministicTxId = "tx_rec_${rule.id}_$dueDay"
                val txRef = userDoc(uid).collection("transactions").document(deterministicTxId)
                val existingTxSnap = txRef.get().await()
                if (!existingTxSnap.exists()) {
                    val newTx = FinTransaction(
                        id = deterministicTxId,
                        userId = uid,
                        title = "${rule.title} (Recurring)",
                        amountPaisa = rule.amountPaisa,
                        type = rule.type,
                        categoryId = rule.categoryId,
                        categoryName = rule.categoryName,
                        accountId = rule.accountId,
                        accountName = rule.accountName,
                        toAccountId = "",
                        toAccountName = "",
                        dateEpochDay = dueDay,
                        timeFormatted = "09:00",
                        timestampMillis = System.currentTimeMillis(),
                        paymentMethod = rule.accountName,
                        notes = "Auto-posted from recurring schedule (${rule.frequency})",
                        attachmentUri = "",
                        recurringRuleId = rule.id,
                        createdAt = null,
                        updatedAt = null
                    )
                    addTransaction(newTx)
                    postedCount++
                }
                lastProcessed = dueDay
                dueDay = DhakaDateUtils.advanceDueDate(dueDay, rule.frequency)
            }

            // Update the rule's nextDueDateEpochDay and lastProcessedDateEpochDay
            saveRecurringRule(
                rule.copy(
                    nextDueDateEpochDay = dueDay,
                    lastProcessedDateEpochDay = lastProcessed
                )
            )
        }
        return postedCount
    }

    // ========================================================================
    // LOANS & DEBTS CRUD + REPAYMENT TRACKING
    // ========================================================================

    suspend fun saveLoan(loan: LoanRecord): String {
        val uid = requireUserId()
        require(loan.principalAmountPaisa > 0L) { "Loan principal must be greater than zero." }
        val isNew = loan.id.isBlank()
        val docId = if (isNew) "loan_${UUID.randomUUID().toString().take(8)}" else loan.id
        val docRef = userDoc(uid).collection("loans").document(docId)
        try {
            if (isNew) {
                val status = if (loan.outstandingAmountPaisa <= 0L) "PAID" else "ACTIVE"
                docRef.set(
                    loan.copy(
                        id = docId,
                        userId = uid,
                        status = status,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            } else {
                val snap = docRef.get().await()
                val existing = snap.toObject(LoanRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val status = if (loan.outstandingAmountPaisa <= 0L) "PAID" else "ACTIVE"
                docRef.set(
                    loan.copy(
                        id = docId,
                        userId = uid,
                        status = status,
                        createdAt = existing?.createdAt ?: loan.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun addLoanPayment(
        loan: LoanRecord,
        paymentAmountPaisa: Long,
        accountId: String,
        accountName: String,
        notes: String
    ) {
        val uid = requireUserId()
        require(paymentAmountPaisa > 0L) { "Payment amount must be positive." }
        require(loan.status == "ACTIVE") { "This loan is already marked as paid." }

        val loanRef = userDoc(uid).collection("loans").document(loan.id)
        val paymentId = "lp_${UUID.randomUUID().toString().take(10)}"
        val paymentRef = userDoc(uid).collection("loan_payments").document(paymentId)

        try {
            val loanSnap = loanRef.get().await()
            val existingLoan = loanSnap.toObject(LoanRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Loan record not found.")

            val newOutstanding = (existingLoan.outstandingAmountPaisa - paymentAmountPaisa).coerceAtLeast(0L)
            val newStatus = if (newOutstanding == 0L) "PAID" else "ACTIVE"

            val accSnap = if (accountId.isNotBlank()) {
                userDoc(uid).collection("accounts").document(accountId).get().await()
            } else null

            val batch = db.batch()
            // 1. Update loan outstanding balance & status
            batch.set(
                loanRef,
                existingLoan.copy(
                    id = existingLoan.id,
                    userId = uid,
                    outstandingAmountPaisa = newOutstanding,
                    status = newStatus,
                    updatedAt = null
                )
            )
            // 2. Record loan payment entry
            batch.set(
                paymentRef,
                LoanPayment(
                    id = paymentId,
                    userId = uid,
                    loanId = existingLoan.id,
                    personOrOrg = existingLoan.personOrOrg,
                    amountPaisa = paymentAmountPaisa,
                    accountId = accountId,
                    accountName = accountName,
                    dateEpochDay = DhakaDateUtils.todayEpochDay(),
                    notes = notes.take(500),
                    createdAt = null,
                    updatedAt = null
                )
            )
            // 3. Update account balance if an account was chosen:
            // Repaying BORROWED money decreases our account balance; receiving LENT repayment increases it.
            val accObj = accSnap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (accObj != null && accountId.isNotBlank()) {
                val delta = if (existingLoan.type == "BORROWED") -paymentAmountPaisa else paymentAmountPaisa
                val accRef = userDoc(uid).collection("accounts").document(accountId)
                batch.set(
                    accRef,
                    accObj.copy(
                        id = accountId,
                        userId = uid,
                        currentBalancePaisa = accObj.currentBalancePaisa + delta,
                        updatedAt = null
                    )
                )
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, loanRef.path)
            throw e
        }
    }

    suspend fun deleteLoan(loanId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("loans").document(loanId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
