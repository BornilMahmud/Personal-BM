package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Account
import com.example.data.model.FinTransaction
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PersonalBmRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun accountAndTransactionBalanceReconciliation_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID().toString().take(6)}@test.com")
        val repo = PersonalBmRepository(firestore)

        // 1. Create an account with 10,000 BDT (1,000,000 paisa)
        val accId = withTimeout(5000L) {
            repo.saveAccount(
                Account(
                    userId = aliceUid,
                    name = "bKash",
                    type = "MOBILE_WALLET",
                    initialBalancePaisa = 1_000_000L,
                    currentBalancePaisa = 1_000_000L,
                    colorHex = "#E2136E",
                    iconName = "smartphone",
                    isDefault = true
                )
            )
        }

        // 2. Add an expense of 2,500 BDT (250,000 paisa)
        val txId = withTimeout(5000L) {
            repo.addTransaction(
                FinTransaction(
                    userId = aliceUid,
                    title = "Dinner",
                    amountPaisa = 250_000L,
                    type = "EXPENSE",
                    categoryId = "cat_exp_food",
                    categoryName = "Food & Dining",
                    accountId = accId,
                    accountName = "bKash",
                    dateEpochDay = 20767L,
                    timeFormatted = "20:00",
                    timestampMillis = 1794302000000L,
                    paymentMethod = "bKash"
                )
            )
        }
        assertTrue(txId.isNotBlank())

        // 3. Verify account balance decreased to 7,500 BDT (750,000 paisa)
        val accounts = withTimeout(3000L) {
            repo.observeAccounts(aliceUid).first { list -> list.any { it.id == accId } }
        }
        val updatedAcc = accounts.first { it.id == accId }
        assertEquals(750_000L, updatedAcc.currentBalancePaisa)
    }

    @Test
    fun observeAccounts_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID().toString().take(6)}@test.com")
        val aliceRepo = PersonalBmRepository(firestore)
        withTimeout(5000L) {
            aliceRepo.saveAccount(
                Account(
                    userId = aliceUid,
                    name = "Cash",
                    type = "CASH",
                    initialBalancePaisa = 500_000L,
                    currentBalancePaisa = 500_000L,
                    colorHex = "#10B981",
                    iconName = "payments",
                    isDefault = true
                )
            )
        }

        // Sign in as Bob and attempt to observe Alice's accounts
        signInTestUser("bob_${UUID.randomUUID().toString().take(6)}@test.com")
        val bobRepo = PersonalBmRepository(firestore)
        var caughtError: Throwable? = null
        try {
            withTimeout(3000L) {
                bobRepo.observeAccounts(aliceUid).first()
            }
        } catch (e: Throwable) {
            caughtError = e
        }
        assertNotNull("Expected exception when Bob reads Alice's accounts", caughtError)
        val firestoreEx = caughtError!!.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    @Test
    fun observeTransactions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = PersonalBmRepository(firestore)
        var caughtError: Throwable? = null
        try {
            withTimeout(3000L) {
                repo.observeTransactions("unauth_user_123").first()
            }
        } catch (e: Throwable) {
            caughtError = e
        }
        assertNotNull("Expected exception when unauthenticated caller observes transactions", caughtError)
        val firestoreEx = caughtError!!.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }
}
