package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.repository.LocalPersonalBmStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Personal BM", appName)
    }

    @Test
    fun `default theme is white light theme and local ledger stores transactions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = LocalPersonalBmStore(context)
        val uid = "test_user_robolectric"
        store.ensureSeeded(
            uid = uid,
            displayName = "Bornil Mahmud",
            email = "bornil@example.com",
            photoUrl = "https://example.com/avatar.png"
        )
        val settings = store.getSettingsSnapshot(uid)
        assertEquals("light", settings.themeMode)
        assertEquals("https://example.com/avatar.png", store.getPhotoUrl(uid))

        store.addTransaction(
            uid = uid,
            tx = FinTransaction(
                id = "tx_test_1",
                title = "Monthly Allowance",
                amountPaisa = 25000_00L,
                type = "INCOME",
                categoryId = "cat_inc_allowance",
                categoryName = "Allowance / Pocket Money",
                accountId = "acc_cash",
                accountName = "Cash Wallet",
                dateEpochDay = DhakaDateUtils.todayEpochDay(),
                timeFormatted = DhakaDateUtils.currentTimeFormatted()
            )
        )
        val txs = store.getTransactionsSnapshot(uid)
        assertTrue(txs.any { it.id == "tx_test_1" && it.amountPaisa == 25000_00L })
    }
}

