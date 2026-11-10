package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToLong

// ============================================================================
// FIRESTORE DATA MODELS (100% 1-to-1 aligned with firestore.rules)
// Monetary amounts are stored as 64-bit integer Paisa (1 BDT = 100 Paisa).
// Never use floating-point arithmetic for financial balances.
// ============================================================================

@IgnoreExtraProperties
data class UserSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val timezone: String = "Asia/Dhaka",
    val language: String = "en", // "en" or "bn"
    val themeMode: String = "light", // "light" (default Minimal Light), "dark", "system"
    val defaultAccountId: String = "acc_cash",
    val weekStartDay: String = "SATURDAY", // "SATURDAY", "SUNDAY", "MONDAY"
    val seededDefaults: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Account(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "CASH", // "CASH", "BANK", "MOBILE_WALLET", "OTHER"
    val initialBalancePaisa: Long = 0L,
    val currentBalancePaisa: Long = 0L,
    val colorHex: String = "#3B82F6",
    val iconName: String = "wallet",
    @field:PropertyName("isDefault") @get:PropertyName("isDefault") val isDefault: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Category(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val nameBn: String = "",
    val type: String = "EXPENSE", // "INCOME" or "EXPENSE"
    val iconName: String = "category",
    val colorHex: String = "#3B82F6",
    @field:PropertyName("isCustom") @get:PropertyName("isCustom") val isCustom: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FinTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountPaisa: Long = 0L,
    val type: String = "EXPENSE", // "INCOME", "EXPENSE", "TRANSFER"
    val categoryId: String = "",
    val categoryName: String = "",
    val accountId: String = "",
    val accountName: String = "",
    val toAccountId: String = "",
    val toAccountName: String = "",
    val dateEpochDay: Long = 0L,
    val timeFormatted: String = "12:00",
    val timestampMillis: Long = 0L,
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val attachmentUri: String = "",
    val recurringRuleId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Budget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val monthKey: String = "", // "YYYY-MM"
    val categoryId: String = "", // "ALL" or category ID
    val categoryName: String = "",
    val limitPaisa: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavingsGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val targetAmountPaisa: Long = 0L,
    val currentSavedPaisa: Long = 0L,
    val targetDateEpochDay: Long = 0L,
    val description: String = "",
    val colorHex: String = "#10B981",
    val iconName: String = "savings",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavingsContribution(
    @DocumentId val id: String = "",
    val userId: String = "",
    val goalId: String = "",
    val goalName: String = "",
    val amountPaisa: Long = 0L,
    val sourceAccountId: String = "",
    val sourceAccountName: String = "",
    val dateEpochDay: Long = 0L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RecurringTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountPaisa: Long = 0L,
    val type: String = "EXPENSE", // "INCOME" or "EXPENSE"
    val categoryId: String = "",
    val categoryName: String = "",
    val accountId: String = "",
    val accountName: String = "",
    val frequency: String = "MONTHLY", // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val nextDueDateEpochDay: Long = 0L,
    val lastProcessedDateEpochDay: Long = 0L,
    @field:PropertyName("isActive") @get:PropertyName("isActive") val isActive: Boolean = true,
    val reminderEnabled: Boolean = true,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LoanRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val personOrOrg: String = "",
    val type: String = "BORROWED", // "BORROWED" (debt owed) or "LENT" (receivable)
    val principalAmountPaisa: Long = 0L,
    val outstandingAmountPaisa: Long = 0L,
    val startDateEpochDay: Long = 0L,
    val dueDateEpochDay: Long = 0L,
    val accountId: String = "",
    val accountName: String = "",
    val status: String = "ACTIVE", // "ACTIVE" or "PAID"
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LoanPayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val loanId: String = "",
    val personOrOrg: String = "",
    val amountPaisa: Long = 0L,
    val accountId: String = "",
    val accountName: String = "",
    val dateEpochDay: Long = 0L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// ============================================================================
// NAVIGATION SECTIONS (13 MODULES)
// ============================================================================

enum class NavSection(val route: String, val labelEn: String, val labelBn: String) {
    OVERVIEW("overview", "Overview", "সারসংক্ষেপ"),
    TRANSACTIONS("transactions", "Transactions", "লেনদেনসমূহ"),
    INCOME("income", "Income", "আয় ব্যবস্থাপনা"),
    EXPENSES("expenses", "Expenses", "ব্যয় ব্যবস্থাপনা"),
    ACCOUNTS("accounts", "Accounts & Wallets", "অ্যাকাউন্ট ও ওয়ালেট"),
    ANALYTICS("analytics", "Analytics", "অ্যানালিটিক্স"),
    BUDGETS("budgets", "Budgets", "বাজেট"),
    SAVINGS_GOALS("savings_goals", "Savings Goals", "সঞ্চয় লক্ষ্য"),
    RECURRING("recurring", "Recurring Transactions", "নিয়মিত লেনদেন"),
    LOANS("loans", "Loans & Debts", "ঋণ ও দেনা-পাওনা"),
    CALENDAR("calendar", "Calendar", "আর্থিক ক্যালেন্ডার"),
    REPORTS("reports", "Reports", "রিপোর্ট ও এক্সপোর্ট"),
    SETTINGS("settings", "Settings", "সেটিংস");

    fun label(lang: String): String = if (lang == "bn") labelBn else labelEn
}

// ============================================================================
// EXACT PAISA ARITHMETIC & BDT FORMATTER
// ============================================================================

object MoneyUtils {
    /**
     * Parses a user-entered string like "1250.50" or "5,000" into exact Paisa (Long).
     * Returns null if invalid or <= 0 (when requirePositive is true).
     */
    fun parseTakaToPaisa(input: String, allowZero: Boolean = false): Long? {
        val cleaned = input.replace(",", "").replace("৳", "").trim()
        if (cleaned.isEmpty()) return null
        val bd = cleaned.toBigDecimalOrNull() ?: return null
        val paisa = bd.multiply(java.math.BigDecimal(100))
            .setScale(0, java.math.RoundingMode.HALF_UP)
            .toLong()
        if (!allowZero && paisa <= 0L) return null
        return paisa
    }

    fun paisaToInputString(paisa: Long): String {
        val whole = paisa / 100L
        val rem = abs(paisa % 100L)
        return if (rem == 0L) "$whole" else String.format(Locale.US, "%d.%02d", whole, rem)
    }

    /**
     * Formats Paisa into Bangladeshi grouping (e.g., ৳ 1,25,450.00 or ৳ 1,25,450).
     */
    fun formatPaisa(
        paisa: Long,
        symbol: String = "৳",
        showDecimals: Boolean = false,
        showPlusSign: Boolean = false
    ): String {
        val isNegative = paisa < 0L
        val absPaisa = abs(paisa)
        val takaWhole = absPaisa / 100L
        val paisaPart = absPaisa % 100L

        val formattedWhole = formatBangladeshiGrouping(takaWhole)
        val signPrefix = when {
            isNegative -> "-"
            showPlusSign && paisa > 0L -> "+"
            else -> ""
        }
        return if (showDecimals || paisaPart != 0L) {
            String.format(Locale.US, "%s%s %s.%02d", signPrefix, symbol, formattedWhole, paisaPart)
        } else {
            "$signPrefix$symbol $formattedWhole"
        }
    }

    private fun formatBangladeshiGrouping(number: Long): String {
        val s = number.toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        var rest = s.substring(0, s.length - 3)
        val parts = mutableListOf<String>()
        while (rest.length > 2) {
            parts.add(0, rest.substring(rest.length - 2))
            rest = rest.substring(0, rest.length - 2)
        }
        if (rest.isNotEmpty()) {
            parts.add(0, rest)
        }
        return parts.joinToString(",") + "," + lastThree
    }
}

// ============================================================================
// ASIA/DHAKA TIMEZONE & CALENDAR UTILITIES
// ============================================================================

object DhakaDateUtils {
    val dhakaTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun calendar(): Calendar = Calendar.getInstance(dhakaTimeZone, Locale.US)

    fun todayEpochDay(): Long {
        val cal = calendar()
        return toEpochDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentTimeFormatted(): String {
        val fmt = SimpleDateFormat("HH:mm", Locale.US)
        fmt.timeZone = dhakaTimeZone
        return fmt.format(Date())
    }

    fun currentMonthKey(): String {
        val cal = calendar()
        return String.format(
            Locale.US,
            "%04d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        )
    }

    fun previousMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        return if (month == 1) {
            String.format(Locale.US, "%04d-12", year - 1)
        } else {
            String.format(Locale.US, "%04d-%02d", year, month - 1)
        }
    }

    fun nextMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        return if (month == 12) {
            String.format(Locale.US, "%04d-01", year + 1)
        } else {
            String.format(Locale.US, "%04d-%02d", year, month + 1)
        }
    }

    fun toEpochDay(year: Int, monthOneBased: Int, dayOfMonth: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        cal.clear()
        cal.set(year, monthOneBased - 1, dayOfMonth, 0, 0, 0)
        return cal.timeInMillis / 86_400_000L
    }

    fun epochDayToYearMonthDay(epochDay: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        cal.clear()
        cal.timeInMillis = epochDay * 86_400_000L
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun epochDayToMonthKey(epochDay: Long): String {
        val (y, m, _) = epochDayToYearMonthDay(epochDay)
        return String.format(Locale.US, "%04d-%02d", y, m)
    }

    fun formatEpochDay(epochDay: Long): String {
        val (y, m, d) = epochDayToYearMonthDay(epochDay)
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val mName = monthNames.getOrElse((m - 1).coerceIn(0, 11)) { "Jan" }
        return String.format(Locale.US, "%02d %s %04d", d, mName, y)
    }

    fun formatEpochDayShort(epochDay: Long): String {
        val (_, m, d) = epochDayToYearMonthDay(epochDay)
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val mName = monthNames.getOrElse((m - 1).coerceIn(0, 11)) { "Jan" }
        return String.format(Locale.US, "%02d %s", d, mName)
    }

    fun formatMonthKeyDisplay(monthKey: String): String {
        val parts = monthKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val mName = monthNames.getOrElse((month - 1).coerceIn(0, 11)) { "January" }
        return "$mName $year"
    }

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun daysInMonth(year: Int, monthOneBased: Int): Int {
        return when (monthOneBased) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    fun firstDayOfWeekOffset(year: Int, monthOneBased: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        cal.clear()
        cal.set(year, monthOneBased - 1, 1)
        // Sunday = 0 .. Saturday = 6
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    fun advanceDueDate(epochDay: Long, frequency: String): Long {
        val (y, m, d) = epochDayToYearMonthDay(epochDay)
        return when (frequency) {
            "DAILY" -> epochDay + 1L
            "WEEKLY" -> epochDay + 7L
            "MONTHLY" -> {
                val nextMonth = if (m == 12) 1 else m + 1
                val nextYear = if (m == 12) y + 1 else y
                val maxDay = daysInMonth(nextYear, nextMonth)
                toEpochDay(nextYear, nextMonth, d.coerceAtMost(maxDay))
            }
            "YEARLY" -> {
                val nextYear = y + 1
                val maxDay = daysInMonth(nextYear, m)
                toEpochDay(nextYear, m, d.coerceAtMost(maxDay))
            }
            else -> epochDay + 30L
        }
    }
}

// ============================================================================
// DEFAULT ACCOUNTS & CATEGORIES SEED DATA
// ============================================================================

object DefaultSeedData {
    fun defaultAccounts(userId: String): List<Pair<String, Account>> = listOf(
        "acc_cash" to Account(
            id = "acc_cash",
            userId = userId,
            name = "Cash",
            type = "CASH",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#10B981",
            iconName = "payments",
            isDefault = true
        ),
        "acc_bank" to Account(
            id = "acc_bank",
            userId = userId,
            name = "Bank Account",
            type = "BANK",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#3B82F6",
            iconName = "account_balance",
            isDefault = true
        ),
        "acc_bkash" to Account(
            id = "acc_bkash",
            userId = userId,
            name = "bKash",
            type = "MOBILE_WALLET",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#E2136E",
            iconName = "smartphone",
            isDefault = true
        ),
        "acc_nagad" to Account(
            id = "acc_nagad",
            userId = userId,
            name = "Nagad",
            type = "MOBILE_WALLET",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#F7941D",
            iconName = "smartphone",
            isDefault = true
        ),
        "acc_rocket" to Account(
            id = "acc_rocket",
            userId = userId,
            name = "Rocket",
            type = "MOBILE_WALLET",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#8C3494",
            iconName = "smartphone",
            isDefault = true
        ),
        "acc_upay" to Account(
            id = "acc_upay",
            userId = userId,
            name = "Upay",
            type = "MOBILE_WALLET",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#0072BC",
            iconName = "smartphone",
            isDefault = true
        ),
        "acc_other" to Account(
            id = "acc_other",
            userId = userId,
            name = "Other",
            type = "OTHER",
            initialBalancePaisa = 0L,
            currentBalancePaisa = 0L,
            colorHex = "#64748B",
            iconName = "wallet",
            isDefault = true
        )
    )

    fun defaultCategories(userId: String): List<Pair<String, Category>> = listOf(
        // Default Income Categories
        "cat_inc_salary" to Category("cat_inc_salary", userId, "Salary", "বেতন", "INCOME", "work", "#10B981", false),
        "cat_inc_freelance" to Category("cat_inc_freelance", userId, "Freelancing", "ফ্রিল্যান্সিং", "INCOME", "laptop", "#06B6D4", false),
        "cat_inc_business" to Category("cat_inc_business", userId, "Business", "ব্যবসা", "INCOME", "store", "#3B82F6", false),
        "cat_inc_video" to Category("cat_inc_video", userId, "Video Editing", "ভিডিও এডিটিং", "INCOME", "movie", "#8B5CF6", false),
        "cat_inc_dev" to Category("cat_inc_dev", userId, "Development", "সফটওয়্যার ডেভেলপমেন্ট", "INCOME", "code", "#0EA5E9", false),
        "cat_inc_invest" to Category("cat_inc_invest", userId, "Investments", "বিনিয়োগ", "INCOME", "trending_up", "#14B8A6", false),
        "cat_inc_gifts" to Category("cat_inc_gifts", userId, "Gifts", "উপহার", "INCOME", "redeem", "#EC4899", false),
        "cat_inc_other" to Category("cat_inc_other", userId, "Other Income", "অন্যান্য আয়", "INCOME", "payments", "#64748B", false),

        // Default Expense Categories
        "cat_exp_food" to Category("cat_exp_food", userId, "Food & Dining", "খাবার ও রেস্তোরাঁ", "EXPENSE", "restaurant", "#EF4444", false),
        "cat_exp_transport" to Category("cat_exp_transport", userId, "Transportation", "যাতায়াত", "EXPENSE", "directions_bus", "#F59E0B", false),
        "cat_exp_shopping" to Category("cat_exp_shopping", userId, "Shopping", "কেনাকাটা", "EXPENSE", "shopping_bag", "#EC4899", false),
        "cat_exp_education" to Category("cat_exp_education", userId, "Education", "শিক্ষা", "EXPENSE", "school", "#3B82F6", false),
        "cat_exp_university" to Category("cat_exp_university", userId, "University", "বিশ্ববিদ্যালয়", "EXPENSE", "account_balance", "#6366F1", false),
        "cat_exp_housing" to Category("cat_exp_housing", userId, "Housing", "আবাসন", "EXPENSE", "home", "#8B5CF6", false),
        "cat_exp_rent" to Category("cat_exp_rent", userId, "Rent", "বাড়ি ভাড়া", "EXPENSE", "apartment", "#A855F7", false),
        "cat_exp_utilities" to Category("cat_exp_utilities", userId, "Utilities", "ইউটিলিটি বিল", "EXPENSE", "bolt", "#EAB308", false),
        "cat_exp_internet" to Category("cat_exp_internet", userId, "Internet", "ইন্টারনেট বিল", "EXPENSE", "wifi", "#06B6D4", false),
        "cat_exp_mobile" to Category("cat_exp_mobile", userId, "Mobile Recharge", "মোবাইল রিচার্জ", "EXPENSE", "phone_android", "#10B981", false),
        "cat_exp_health" to Category("cat_exp_health", userId, "Healthcare", "চিকিৎসা ও স্বাস্থ্য", "EXPENSE", "medical_services", "#F43F5E", false),
        "cat_exp_entertain" to Category("cat_exp_entertain", userId, "Entertainment", "বিনোদন", "EXPENSE", "ConfirmationNumber", "#D946EF", false),
        "cat_exp_travel" to Category("cat_exp_travel", userId, "Travel", "ভ্রমণ", "EXPENSE", "flight", "#0EA5E9", false),
        "cat_exp_subs" to Category("cat_exp_subs", userId, "Subscriptions", "সাবস্ক্রিপশন", "EXPENSE", "subscriptions", "#6366F1", false),
        "cat_exp_personal" to Category("cat_exp_personal", userId, "Personal Care", "ব্যক্তিগত যত্ন", "EXPENSE", "spa", "#14B8A6", false),
        "cat_exp_family" to Category("cat_exp_family", userId, "Family", "পরিবার", "EXPENSE", "family_restroom", "#F97316", false),
        "cat_exp_tech" to Category("cat_exp_tech", userId, "Technology", "প্রযুক্তি ও গ্যাজেট", "EXPENSE", "devices", "#3B82F6", false),
        "cat_exp_other" to Category("cat_exp_other", userId, "Other", "অন্যান্য ব্যয়", "EXPENSE", "more_horiz", "#64748B", false)
    )
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFF3B82F6)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val value = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000L or value)
        } else {
            Color(value)
        }
    } catch (_: Exception) {
        fallback
    }
}
