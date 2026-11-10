package com.example.data.repository

import android.content.Context
import android.net.Uri
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
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PersonalBmRepository(
    private val db: FirebaseFirestore,
    private val localStore: LocalPersonalBmStore? = null
) {
    // CRITICAL: Always initialize with R.string.firestore_database_id and LocalPersonalBmStore
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        localStore = LocalPersonalBmStore(context.applicationContext)
    )

    private val auth = Firebase.auth

    private fun activeUserId(): String {
        val cloudUid = auth.currentUser?.uid
        if (cloudUid != null) return cloudUid
        if (localStore != null) return LocalPersonalBmStore.DEFAULT_LOCAL_USER_ID
        throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun requireCloudUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    // ========================================================================
    // REAL-TIME OBSERVATION STREAMS
    // ========================================================================

    fun observeSettings(uid: String): Flow<UserSettings?> {
        if (localStore != null) {
            return localStore.observeSettings(uid)
        }
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

    fun observePhotoUrl(uid: String): Flow<String> {
        if (localStore != null) {
            return localStore.observePhotoUrl(uid)
        }
        val googlePhoto = try {
            auth.currentUser?.photoUrl?.toString() ?: ""
        } catch (_: Exception) {
            ""
        }
        return flowOf(googlePhoto)
    }

    fun observeAccounts(uid: String): Flow<List<Account>> {
        if (localStore != null) {
            return localStore.observeAccounts(uid)
        }
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
        if (localStore != null) {
            return localStore.observeCategories(uid)
        }
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
        if (localStore != null) {
            return localStore.observeTransactions(uid)
        }
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
        if (localStore != null) {
            return localStore.observeBudgets(uid)
        }
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
        if (localStore != null) {
            return localStore.observeSavingsGoals(uid)
        }
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
        if (localStore != null) {
            return localStore.observeSavingsContributions(uid)
        }
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
        if (localStore != null) {
            return localStore.observeRecurringTransactions(uid)
        }
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
        if (localStore != null) {
            return localStore.observeLoans(uid)
        }
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
        if (localStore != null) {
            return localStore.observeLoanPayments(uid)
        }
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

    suspend fun ensureDefaultsSeeded(
        displayName: String,
        email: String,
        photoUrl: String = ""
    ) {
        val uid = activeUserId()
        val googleUser = try { auth.currentUser } catch (_: Exception) { null }
        val effectiveName = googleUser?.displayName?.takeIf { it.isNotBlank() } ?: displayName
        val effectiveEmail = googleUser?.email?.takeIf { it.isNotBlank() } ?: email
        val effectivePhoto = googleUser?.photoUrl?.toString()?.takeIf { it.isNotBlank() } ?: photoUrl

        localStore?.ensureSeeded(uid, effectiveName, effectiveEmail, effectivePhoto)

        val cloudUid = googleUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }

        // Migrate any local default user data into this Google user's store so nothing is lost on login
        localStore?.migrateDefaultUserToCloudUser(
            cloudUid = cloudUid!!,
            googleDisplayName = effectiveName,
            googleEmail = effectiveEmail,
            googlePhotoUrl = effectivePhoto
        )

        val settingsRef = userDoc(cloudUid!!).collection("settings").document("preferences")
        try {
            val existingSettingsSnap = settingsRef.get().await()
            if (existingSettingsSnap.exists()) {
                val existing = existingSettingsSnap.toObject(
                    UserSettings::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (existing?.seededDefaults == true) {
                    if (localStore != null) {
                        syncLocalAndCloudForGoogleUser(cloudUid)
                    }
                    return
                }
            }

            val batch = db.batch()
            val settingsModel = UserSettings(
                id = "preferences",
                userId = cloudUid,
                displayName = effectiveName.ifBlank { "Personal BM User" }.take(120),
                email = effectiveEmail.take(254),
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

            for ((docId, acc) in DefaultSeedData.defaultAccounts(cloudUid)) {
                val accRef = userDoc(cloudUid).collection("accounts").document(docId)
                batch.set(accRef, acc.copy(createdAt = null, updatedAt = null))
            }

            for ((docId, cat) in DefaultSeedData.defaultCategories(cloudUid)) {
                val catRef = userDoc(cloudUid).collection("categories").document(docId)
                batch.set(catRef, cat.copy(createdAt = null, updatedAt = null))
            }

            batch.commit().await()

            if (localStore != null) {
                syncLocalAndCloudForGoogleUser(cloudUid)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.WRITE, settingsRef.path)
                throw e
            }
        }
    }

    /**
     * Synchronizes all collections between LocalPersonalBmStore and Firebase Firestore
     * when a user is signed in with Google Auth.
     */
    suspend fun syncLocalAndCloudForGoogleUser(cloudUid: String = auth.currentUser?.uid ?: "") {
        if (cloudUid.isBlank() || cloudUid == LocalPersonalBmStore.DEFAULT_LOCAL_USER_ID || localStore == null) return
        try {
            // 1. Pull existing cloud documents
            val cloudSettings = userDoc(cloudUid).collection("settings").whereEqualTo("userId", cloudUid).get().await()
                .documents.firstOrNull()?.toObject(UserSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudAccounts = userDoc(cloudUid).collection("accounts").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudCategories = userDoc(cloudUid).collection("categories").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudTxs = userDoc(cloudUid).collection("transactions").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(FinTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudBudgets = userDoc(cloudUid).collection("budgets").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(Budget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudGoals = userDoc(cloudUid).collection("savings_goals").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(SavingsGoal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudContribs = userDoc(cloudUid).collection("savings_contributions").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(SavingsContribution::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudRecurring = userDoc(cloudUid).collection("recurring_transactions").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(RecurringTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudLoans = userDoc(cloudUid).collection("loans").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(LoanRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val cloudLoanPayments = userDoc(cloudUid).collection("loan_payments").whereEqualTo("userId", cloudUid).get().await()
                .toObjects(LoanPayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)

            // 2. Merge cloud into local store
            localStore.mergeFromCloud(
                uid = cloudUid,
                cloudSettings = cloudSettings,
                cloudAccounts = cloudAccounts,
                cloudCategories = cloudCategories,
                cloudTransactions = cloudTxs,
                cloudBudgets = cloudBudgets,
                cloudGoals = cloudGoals,
                cloudContribs = cloudContribs,
                cloudRecurring = cloudRecurring,
                cloudLoans = cloudLoans,
                cloudLoanPayments = cloudLoanPayments
            )

            // 3. Push merged local store documents that aren't in Firestore yet up to Firestore
            val cloudTxIds = cloudTxs.map { it.id }.toSet()
            val cloudAccMap = cloudAccounts.associateBy { it.id }
            val cloudCatIds = cloudCategories.map { it.id }.toSet()
            val cloudBudgetIds = cloudBudgets.map { it.id }.toSet()
            val cloudGoalIds = cloudGoals.map { it.id }.toSet()
            val cloudContribIds = cloudContribs.map { it.id }.toSet()
            val cloudRecIds = cloudRecurring.map { it.id }.toSet()
            val cloudLoanIds = cloudLoans.map { it.id }.toSet()
            val cloudLpIds = cloudLoanPayments.map { it.id }.toSet()

            for (acc in localStore.getAccountsSnapshot(cloudUid)) {
                val existingCloud = cloudAccMap[acc.id]
                if (existingCloud == null || existingCloud.currentBalancePaisa != acc.currentBalancePaisa) {
                    val docRef = userDoc(cloudUid).collection("accounts").document(acc.id)
                    docRef.set(
                        acc.copy(
                            id = acc.id,
                            userId = cloudUid,
                            createdAt = existingCloud?.createdAt,
                            updatedAt = null
                        )
                    ).await()
                }
            }
            for (cat in localStore.getCategoriesSnapshot(cloudUid)) {
                if (cat.id !in cloudCatIds) {
                    userDoc(cloudUid).collection("categories").document(cat.id)
                        .set(cat.copy(id = cat.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (tx in localStore.getTransactionsSnapshot(cloudUid)) {
                if (tx.id !in cloudTxIds) {
                    userDoc(cloudUid).collection("transactions").document(tx.id)
                        .set(tx.copy(id = tx.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (b in localStore.getBudgetsSnapshot(cloudUid)) {
                if (b.id !in cloudBudgetIds) {
                    userDoc(cloudUid).collection("budgets").document(b.id)
                        .set(b.copy(id = b.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (g in localStore.getGoalsSnapshot(cloudUid)) {
                if (g.id !in cloudGoalIds) {
                    userDoc(cloudUid).collection("savings_goals").document(g.id)
                        .set(g.copy(id = g.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (c in localStore.getContributionsSnapshot(cloudUid)) {
                if (c.id !in cloudContribIds) {
                    userDoc(cloudUid).collection("savings_contributions").document(c.id)
                        .set(c.copy(id = c.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (r in localStore.getRecurringSnapshot(cloudUid)) {
                if (r.id !in cloudRecIds) {
                    userDoc(cloudUid).collection("recurring_transactions").document(r.id)
                        .set(r.copy(id = r.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (l in localStore.getLoansSnapshot(cloudUid)) {
                if (l.id !in cloudLoanIds) {
                    userDoc(cloudUid).collection("loans").document(l.id)
                        .set(l.copy(id = l.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
            for (lp in localStore.getLoanPaymentsSnapshot(cloudUid)) {
                if (lp.id !in cloudLpIds) {
                    userDoc(cloudUid).collection("loan_payments").document(lp.id)
                        .set(lp.copy(id = lp.id, userId = cloudUid, createdAt = null, updatedAt = null)).await()
                }
            }
        } catch (_: Exception) {
            // Non-fatal background cloud sync
        }
    }

    // ========================================================================
    // USER SETTINGS & PROFILE (GOOGLE AUTH IMAGE & NAME UPDATABLE)
    // ========================================================================

    suspend fun updateUserProfile(
        updatedSettings: UserSettings,
        newPhotoUrl: String,
        isCustomPhoto: Boolean = true
    ) {
        val uid = activeUserId()
        localStore?.setPhotoUrl(uid, newPhotoUrl, isCustom = isCustomPhoto)
        localStore?.updateSettings(uid, updatedSettings, markCustomName = true)

        val googleUser = try { auth.currentUser } catch (_: Exception) { null }
        if (googleUser != null) {
            try {
                val profileUpdates = userProfileChangeRequest {
                    displayName = updatedSettings.displayName.take(120)
                    if (newPhotoUrl.isNotBlank()) {
                        photoUri = Uri.parse(newPhotoUrl)
                    }
                }
                googleUser.updateProfile(profileUpdates).await()
            } catch (_: Exception) {
                // Ignore if photoUri cannot be updated on FirebaseAuth user object directly
            }
        }
        updateSettings(updatedSettings)
    }

    suspend fun updateSettings(updated: UserSettings) {
        val uid = activeUserId()
        localStore?.updateSettings(uid, updated)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docId = updated.id.ifBlank { "preferences" }
        val docRef = userDoc(cloudUid!!).collection("settings").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val current = snap.toObject(UserSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val preservedCreatedAt = current?.createdAt ?: updated.createdAt
                docRef.set(
                    updated.copy(
                        id = docId,
                        userId = cloudUid,
                        createdAt = preservedCreatedAt,
                        updatedAt = null
                    )
                ).await()
            } else {
                docRef.set(
                    updated.copy(
                        id = docId,
                        userId = cloudUid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
    }

    // ========================================================================
    // ACCOUNTS & WALLETS CRUD
    // ========================================================================

    suspend fun saveAccount(account: Account): String {
        val uid = activeUserId()
        val localDocId = localStore?.saveAccount(uid, account)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val isNew = account.id.isBlank()
        val docId = localDocId ?: if (isNew) "acc_${UUID.randomUUID().toString().take(8)}" else account.id
        val docRef = userDoc(cloudUid!!).collection("accounts").document(docId)
        try {
            if (isNew) {
                val toCreate = account.copy(
                    id = docId,
                    userId = cloudUid,
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
                    userId = cloudUid,
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
            if (localStore == null) {
                handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun deleteAccount(accountId: String, existingTransactions: List<FinTransaction>) {
        val uid = activeUserId()
        val hasLinkedTx = existingTransactions.any {
            it.accountId == accountId || it.toAccountId == accountId
        }
        if (hasLinkedTx) {
            throw IllegalStateException("Cannot delete an account that has existing transactions. Delete or reassign its transactions first.")
        }
        localStore?.deleteAccount(uid, accountId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docRef = userDoc(cloudUid!!).collection("accounts").document(accountId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
                throw e
            }
        }
    }

    // ========================================================================
    // CATEGORIES CRUD
    // ========================================================================

    suspend fun saveCategory(category: Category): String {
        val uid = activeUserId()
        val localDocId = localStore?.saveCategory(uid, category)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val isNew = category.id.isBlank()
        val docId = localDocId ?: if (isNew) "cat_${UUID.randomUUID().toString().take(8)}" else category.id
        val docRef = userDoc(cloudUid!!).collection("categories").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    category.copy(
                        id = docId,
                        userId = cloudUid,
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
                        userId = cloudUid,
                        createdAt = existing?.createdAt ?: category.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        val uid = activeUserId()
        localStore?.deleteCategory(uid, categoryId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docRef = userDoc(cloudUid!!).collection("categories").document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
                throw e
            }
        }
    }

    // ========================================================================
    // TRANSACTIONS CRUD WITH ATOMIC ACCOUNT BALANCE RECONCILIATION
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
        val uid = activeUserId()
        require(tx.amountPaisa > 0L) { "Transaction amount must be greater than zero." }
        if (tx.type == "TRANSFER") {
            require(tx.accountId.isNotBlank() && tx.toAccountId.isNotBlank() && tx.accountId != tx.toAccountId) {
                "Transfer requires two distinct accounts."
            }
        }
        val localDocId = localStore?.addTransaction(uid, tx)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val docId = localDocId ?: tx.id.ifBlank { "tx_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val txRef = userDoc(cloudUid!!).collection("transactions").document(docId)
        try {
            val deltas = computeBalanceDeltas(tx, 1L)
            val accountSnaps = deltas.keys.associateWith { accId ->
                userDoc(cloudUid).collection("accounts").document(accId).get().await()
            }

            val batch = db.batch()
            batch.set(
                txRef,
                tx.copy(
                    id = docId,
                    userId = cloudUid,
                    createdAt = null,
                    updatedAt = null
                )
            )

            for ((accId, delta) in deltas) {
                if (delta == 0L) continue
                val snap = accountSnaps[accId]
                val accObj = snap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (accObj != null) {
                    val accRef = userDoc(cloudUid).collection("accounts").document(accId)
                    batch.set(
                        accRef,
                        accObj.copy(
                            id = accId,
                            userId = cloudUid,
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
            if (localStore == null) {
                handleFirestoreError(e, OperationType.CREATE, txRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun updateTransaction(updatedTx: FinTransaction) {
        val uid = activeUserId()
        require(updatedTx.id.isNotBlank()) { "Transaction ID required for update." }
        require(updatedTx.amountPaisa > 0L) { "Transaction amount must be greater than zero." }
        localStore?.updateTransaction(uid, updatedTx)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val txRef = userDoc(cloudUid!!).collection("transactions").document(updatedTx.id)
        try {
            val existingSnap = txRef.get().await()
            val oldTx = existingSnap.toObject(FinTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Original transaction not found.")

            val combinedDeltas = mutableMapOf<String, Long>()
            for ((accId, d) in computeBalanceDeltas(oldTx, -1L)) {
                combinedDeltas[accId] = (combinedDeltas[accId] ?: 0L) + d
            }
            for ((accId, d) in computeBalanceDeltas(updatedTx, 1L)) {
                combinedDeltas[accId] = (combinedDeltas[accId] ?: 0L) + d
            }

            val accountSnaps = combinedDeltas.keys.associateWith { accId ->
                userDoc(cloudUid).collection("accounts").document(accId).get().await()
            }

            val batch = db.batch()
            batch.set(
                txRef,
                updatedTx.copy(
                    userId = cloudUid,
                    createdAt = oldTx.createdAt,
                    updatedAt = null
                )
            )

            for ((accId, delta) in combinedDeltas) {
                if (delta == 0L) continue
                val snap = accountSnaps[accId]
                val accObj = snap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (accObj != null) {
                    val accRef = userDoc(cloudUid).collection("accounts").document(accId)
                    batch.set(
                        accRef,
                        accObj.copy(
                            id = accId,
                            userId = cloudUid,
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
            if (localStore == null) {
                handleFirestoreError(e, OperationType.UPDATE, txRef.path)
                throw e
            }
        }
    }

    suspend fun deleteTransaction(txId: String) {
        val uid = activeUserId()
        localStore?.deleteTransaction(uid, txId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val txRef = userDoc(cloudUid!!).collection("transactions").document(txId)
        try {
            val existingSnap = txRef.get().await()
            val oldTx = existingSnap.toObject(FinTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: return

            val reversalDeltas = computeBalanceDeltas(oldTx, -1L)
            val accountSnaps = reversalDeltas.keys.associateWith { accId ->
                userDoc(cloudUid).collection("accounts").document(accId).get().await()
            }

            val batch = db.batch()
            batch.delete(txRef)

            for ((accId, delta) in reversalDeltas) {
                if (delta == 0L) continue
                val snap = accountSnaps[accId]
                val accObj = snap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (accObj != null) {
                    val accRef = userDoc(cloudUid).collection("accounts").document(accId)
                    batch.set(
                        accRef,
                        accObj.copy(
                            id = accId,
                            userId = cloudUid,
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
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, txRef.path)
                throw e
            }
        }
    }

    // ========================================================================
    // BUDGETS CRUD
    // ========================================================================

    suspend fun saveBudget(budget: Budget): String {
        val uid = activeUserId()
        require(budget.limitPaisa > 0L) { "Budget limit must be positive." }
        val localDocId = localStore?.saveBudget(uid, budget)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val docId = localDocId ?: budget.id.ifBlank { "bgt_${budget.monthKey}_${budget.categoryId}" }
        val docRef = userDoc(cloudUid!!).collection("budgets").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(Budget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                docRef.set(
                    budget.copy(
                        id = docId,
                        userId = cloudUid,
                        createdAt = existing?.createdAt ?: budget.createdAt,
                        updatedAt = null
                    )
                ).await()
            } else {
                docRef.set(
                    budget.copy(
                        id = docId,
                        userId = cloudUid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun deleteBudget(budgetId: String) {
        val uid = activeUserId()
        localStore?.deleteBudget(uid, budgetId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docRef = userDoc(cloudUid!!).collection("budgets").document(budgetId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
                throw e
            }
        }
    }

    // ========================================================================
    // SAVINGS GOALS & CONTRIBUTIONS
    // ========================================================================

    suspend fun saveSavingsGoal(goal: SavingsGoal): String {
        val uid = activeUserId()
        require(goal.targetAmountPaisa > 0L) { "Target amount must be greater than zero." }
        val localDocId = localStore?.saveSavingsGoal(uid, goal)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val isNew = goal.id.isBlank()
        val docId = localDocId ?: if (isNew) "goal_${UUID.randomUUID().toString().take(8)}" else goal.id
        val docRef = userDoc(cloudUid!!).collection("savings_goals").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    goal.copy(
                        id = docId,
                        userId = cloudUid,
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
                        userId = cloudUid,
                        createdAt = existing?.createdAt ?: goal.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun addSavingsContribution(
        goalId: String,
        amountPaisa: Long,
        sourceAccountId: String,
        sourceAccountName: String,
        notes: String
    ) {
        val uid = activeUserId()
        require(amountPaisa > 0L) { "Contribution amount must be positive." }
        localStore?.addSavingsContribution(uid, goalId, amountPaisa, sourceAccountId, sourceAccountName, notes)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val goalRef = userDoc(cloudUid!!).collection("savings_goals").document(goalId)
        val contribId = "sc_${UUID.randomUUID().toString().take(10)}"
        val contribRef = userDoc(cloudUid).collection("savings_contributions").document(contribId)
        try {
            val goalSnap = goalRef.get().await()
            val goal = goalSnap.toObject(SavingsGoal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Savings goal not found.")

            val accSnap = if (sourceAccountId.isNotBlank()) {
                userDoc(cloudUid).collection("accounts").document(sourceAccountId).get().await()
            } else null

            val batch = db.batch()
            batch.set(
                goalRef,
                goal.copy(
                    id = goalId,
                    userId = cloudUid,
                    currentSavedPaisa = goal.currentSavedPaisa + amountPaisa,
                    updatedAt = null
                )
            )
            batch.set(
                contribRef,
                SavingsContribution(
                    id = contribId,
                    userId = cloudUid,
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
            val accObj = accSnap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (accObj != null && sourceAccountId.isNotBlank()) {
                val accRef = userDoc(cloudUid).collection("accounts").document(sourceAccountId)
                batch.set(
                    accRef,
                    accObj.copy(
                        id = sourceAccountId,
                        userId = cloudUid,
                        currentBalancePaisa = accObj.currentBalancePaisa - amountPaisa,
                        updatedAt = null
                    )
                )
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.WRITE, goalRef.path)
                throw e
            }
        }
    }

    /**
     * One-step savings insertion: finds an existing savings goal by name (or creates one if none matches)
     * and records the contribution + deducts from the source wallet if specified.
     */
    suspend fun allocateQuickSavings(
        goalName: String,
        amountPaisa: Long,
        targetAmountPaisa: Long,
        sourceAccountId: String,
        sourceAccountName: String,
        notes: String
    ) {
        val uid = activeUserId()
        require(amountPaisa > 0L) { "Savings amount must be greater than zero." }
        val cleanName = goalName.trim().ifBlank { "Monthly Savings" }
        val existingGoals = observeSavingsGoals(uid).firstOrNull() ?: emptyList()
        val matchingGoal = existingGoals.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
        val goalId = if (matchingGoal != null) {
            matchingGoal.id
        } else {
            val effectiveTarget = if (targetAmountPaisa >= amountPaisa) targetAmountPaisa else (amountPaisa * 5L)
            saveSavingsGoal(
                SavingsGoal(
                    name = cleanName.take(120),
                    targetAmountPaisa = effectiveTarget,
                    currentSavedPaisa = 0L,
                    targetDateEpochDay = DhakaDateUtils.todayEpochDay() + 180L,
                    description = notes.take(500),
                    colorHex = "#10B981",
                    iconName = "savings"
                )
            )
        }
        addSavingsContribution(
            goalId = goalId,
            amountPaisa = amountPaisa,
            sourceAccountId = sourceAccountId,
            sourceAccountName = sourceAccountName,
            notes = notes
        )
    }

    suspend fun deleteSavingsGoal(goalId: String) {
        val uid = activeUserId()
        localStore?.deleteSavingsGoal(uid, goalId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docRef = userDoc(cloudUid!!).collection("savings_goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
                throw e
            }
        }
    }

    // ========================================================================
    // RECURRING TRANSACTIONS & IDEMPOTENT AUTO-POSTING ENGINE
    // ========================================================================

    suspend fun saveRecurringRule(rule: RecurringTransaction): String {
        val uid = activeUserId()
        require(rule.amountPaisa > 0L) { "Recurring amount must be positive." }
        val localDocId = localStore?.saveRecurringRule(uid, rule)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val isNew = rule.id.isBlank()
        val docId = localDocId ?: if (isNew) "rec_${UUID.randomUUID().toString().take(8)}" else rule.id
        val docRef = userDoc(cloudUid!!).collection("recurring_transactions").document(docId)
        try {
            if (isNew) {
                docRef.set(
                    rule.copy(
                        id = docId,
                        userId = cloudUid,
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
                        userId = cloudUid,
                        createdAt = existing?.createdAt ?: rule.createdAt,
                        updatedAt = null
                    )
                ).await()
            }
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun toggleRecurringRule(rule: RecurringTransaction) {
        saveRecurringRule(rule.copy(isActive = !rule.isActive))
    }

    suspend fun deleteRecurringRule(ruleId: String) {
        val uid = activeUserId()
        localStore?.deleteRecurringRule(uid, ruleId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docRef = userDoc(cloudUid!!).collection("recurring_transactions").document(ruleId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
                throw e
            }
        }
    }

    suspend fun processDueRecurringRules(rules: List<RecurringTransaction>): Int {
        val uid = activeUserId()
        val today = DhakaDateUtils.todayEpochDay()
        val dueRules = rules.filter {
            it.isActive && it.nextDueDateEpochDay in 1..today && it.lastProcessedDateEpochDay < it.nextDueDateEpochDay
        }
        if (dueRules.isEmpty()) return 0

        val existingTxs = observeTransactions(uid).firstOrNull() ?: emptyList()
        val existingTxIds = existingTxs.map { it.id }.toSet()

        var postedCount = 0
        for (rule in dueRules) {
            var dueDay = rule.nextDueDateEpochDay
            var lastProcessed = rule.lastProcessedDateEpochDay
            var safetyCounter = 0

            while (dueDay <= today && safetyCounter < 12) {
                safetyCounter++
                val deterministicTxId = "tx_rec_${rule.id}_$dueDay"
                val alreadyPosted = if (localStore != null) {
                    existingTxIds.contains(deterministicTxId)
                } else {
                    val txRef = userDoc(uid).collection("transactions").document(deterministicTxId)
                    txRef.get().await().exists()
                }
                if (!alreadyPosted) {
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
        val uid = activeUserId()
        require(loan.principalAmountPaisa > 0L) { "Loan principal must be greater than zero." }
        val localDocId = localStore?.saveLoan(uid, loan)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localDocId != null) return localDocId
            requireCloudUserId()
        }
        val isNew = loan.id.isBlank()
        val docId = localDocId ?: if (isNew) "loan_${UUID.randomUUID().toString().take(8)}" else loan.id
        val docRef = userDoc(cloudUid!!).collection("loans").document(docId)
        try {
            if (isNew) {
                val status = if (loan.outstandingAmountPaisa <= 0L) "PAID" else "ACTIVE"
                docRef.set(
                    loan.copy(
                        id = docId,
                        userId = cloudUid,
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
                        userId = cloudUid,
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
            if (localStore == null) {
                handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
                throw e
            }
            return docId
        }
    }

    suspend fun addLoanPayment(
        loan: LoanRecord,
        paymentAmountPaisa: Long,
        accountId: String,
        accountName: String,
        notes: String
    ) {
        val uid = activeUserId()
        require(paymentAmountPaisa > 0L) { "Payment amount must be positive." }
        require(loan.status == "ACTIVE") { "This loan is already marked as paid." }
        localStore?.addLoanPayment(uid, loan, paymentAmountPaisa, accountId, accountName, notes)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }

        val loanRef = userDoc(cloudUid!!).collection("loans").document(loan.id)
        val paymentId = "lp_${UUID.randomUUID().toString().take(10)}"
        val paymentRef = userDoc(cloudUid).collection("loan_payments").document(paymentId)

        try {
            val loanSnap = loanRef.get().await()
            val existingLoan = loanSnap.toObject(LoanRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Loan record not found.")

            val newOutstanding = (existingLoan.outstandingAmountPaisa - paymentAmountPaisa).coerceAtLeast(0L)
            val newStatus = if (newOutstanding == 0L) "PAID" else "ACTIVE"

            val accSnap = if (accountId.isNotBlank()) {
                userDoc(cloudUid).collection("accounts").document(accountId).get().await()
            } else null

            val batch = db.batch()
            batch.set(
                loanRef,
                existingLoan.copy(
                    id = existingLoan.id,
                    userId = cloudUid,
                    outstandingAmountPaisa = newOutstanding,
                    status = newStatus,
                    updatedAt = null
                )
            )
            batch.set(
                paymentRef,
                LoanPayment(
                    id = paymentId,
                    userId = cloudUid,
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
            val accObj = accSnap?.toObject(Account::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (accObj != null && accountId.isNotBlank()) {
                val delta = if (existingLoan.type == "BORROWED") -paymentAmountPaisa else paymentAmountPaisa
                val accRef = userDoc(cloudUid).collection("accounts").document(accountId)
                batch.set(
                    accRef,
                    accObj.copy(
                        id = accountId,
                        userId = cloudUid,
                        currentBalancePaisa = accObj.currentBalancePaisa + delta,
                        updatedAt = null
                    )
                )
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.WRITE, loanRef.path)
                throw e
            }
        }
    }

    suspend fun deleteLoan(loanId: String) {
        val uid = activeUserId()
        localStore?.deleteLoan(uid, loanId)
        val cloudUid = auth.currentUser?.uid
        if (cloudUid == null) {
            if (localStore != null) return
            requireCloudUserId()
        }
        val docRef = userDoc(cloudUid!!).collection("loans").document(loanId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (localStore == null) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
                throw e
            }
        }
    }
}
