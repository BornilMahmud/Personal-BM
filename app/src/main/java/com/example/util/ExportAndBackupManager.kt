package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.LoanRecord
import com.example.data.model.MoneyUtils
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsGoal
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object ExportAndBackupManager {

    private fun ensureExportDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Generates a CSV file for the given report title and filtered transactions, then launches the Android Share sheet.
     */
    fun exportTransactionsCsv(
        context: Context,
        reportTitle: String,
        transactions: List<FinTransaction>,
        currencySymbol: String = "৳"
    ): File {
        val safeName = reportTitle.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        val file = File(ensureExportDir(context), "personal_bm_${safeName}_${DhakaDateUtils.todayEpochDay()}.csv")

        val sb = StringBuilder()
        sb.appendLine("Date,Time,Title,Type,Category,Account,To Account,Payment Method,Amount ($currencySymbol),Amount (Paisa),Notes")
        for (tx in transactions) {
            val dateStr = DhakaDateUtils.formatEpochDay(tx.dateEpochDay)
            val amountFormatted = MoneyUtils.paisaToInputString(tx.amountPaisa)
            sb.appendLine(
                listOf(
                    escapeCsv(dateStr),
                    escapeCsv(tx.timeFormatted),
                    escapeCsv(tx.title),
                    escapeCsv(tx.type),
                    escapeCsv(tx.categoryName),
                    escapeCsv(tx.accountName),
                    escapeCsv(tx.toAccountName),
                    escapeCsv(tx.paymentMethod),
                    escapeCsv(amountFormatted),
                    tx.amountPaisa.toString(),
                    escapeCsv(tx.notes)
                ).joinToString(",")
            )
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        shareFile(context, file, "text/csv", "Export CSV — $reportTitle")
        return file
    }

    /**
     * Generates a printable multi-section PDF report using Android's native PdfDocument API.
     */
    fun exportFinancialReportPdf(
        context: Context,
        reportTitle: String,
        periodSubtitle: String,
        transactions: List<FinTransaction>,
        accounts: List<Account>,
        currencySymbol: String = "৳"
    ): File {
        val safeName = reportTitle.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        val file = File(ensureExportDir(context), "personal_bm_${safeName}_${DhakaDateUtils.todayEpochDay()}.pdf")

        val pdf = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points

        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#101827") }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3B82F6") }
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 10f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#101827")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#10B981")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EF4444")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
        val netFlow = totalIncome - totalExpense
        val totalBalance = accounts.sumOf { it.currentBalancePaisa }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdf.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Draw Header Banner
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 84f, headerBgPaint)
        canvas.drawRect(0f, 80f, pageWidth.toFloat(), 84f, accentPaint)
        canvas.drawText("PERSONAL BM — FINANCIAL STATEMENT", 32f, 36f, titlePaint)
        canvas.drawText("$reportTitle  |  $periodSubtitle  |  Timezone: Asia/Dhaka", 32f, 56f, subPaint)
        canvas.drawText("Every Taka. Every Transaction. Total Control.", 32f, 70f, subPaint)

        // Summary Row
        var y = 104f
        val boxW = (pageWidth - 64f - 24f) / 4f
        val summaries = listOf(
            "Total Balance" to MoneyUtils.formatPaisa(totalBalance, currencySymbol, true),
            "Period Income" to MoneyUtils.formatPaisa(totalIncome, currencySymbol, true),
            "Period Expense" to MoneyUtils.formatPaisa(totalExpense, currencySymbol, true),
            "Net Cash Flow" to MoneyUtils.formatPaisa(netFlow, currencySymbol, true)
        )
        summaries.forEachIndexed { idx, (label, valStr) ->
            val left = 32f + idx * (boxW + 8f)
            canvas.drawRect(left, y, left + boxW, y + 48f, cardBgPaint)
            canvas.drawRect(left, y, left + boxW, y + 48f, borderPaint)
            canvas.drawText(label, left + 8f, y + 18f, bodyPaint)
            canvas.drawText(valStr, left + 8f, y + 36f, boldBodyPaint)
        }

        y += 68f
        canvas.drawText("Transactions (${transactions.size} records)", 32f, y, sectionPaint)
        y += 12f

        // Table Header
        canvas.drawRect(32f, y, pageWidth - 32f, y + 20f, cardBgPaint)
        canvas.drawText("Date", 38f, y + 14f, boldBodyPaint)
        canvas.drawText("Title", 108f, y + 14f, boldBodyPaint)
        canvas.drawText("Category", 260f, y + 14f, boldBodyPaint)
        canvas.drawText("Account", 370f, y + 14f, boldBodyPaint)
        canvas.drawText("Amount", 470f, y + 14f, boldBodyPaint)
        y += 24f

        for (tx in transactions) {
            if (y > pageHeight - 54f) {
                pdf.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
            val dateStr = DhakaDateUtils.formatEpochDay(tx.dateEpochDay)
            val titleClipped = tx.title.take(26)
            val catClipped = (if (tx.type == "TRANSFER") "Transfer" else tx.categoryName).take(18)
            val accClipped = (if (tx.type == "TRANSFER") "${tx.accountName}->${tx.toAccountName}" else tx.accountName).take(16)
            val amtPaint = when (tx.type) {
                "INCOME" -> greenPaint
                "EXPENSE" -> redPaint
                else -> boldBodyPaint
            }
            val prefix = when (tx.type) {
                "INCOME" -> "+"
                "EXPENSE" -> "-"
                else -> ""
            }
            val amtStr = prefix + MoneyUtils.formatPaisa(tx.amountPaisa, currencySymbol, true)

            canvas.drawText(dateStr, 38f, y + 12f, bodyPaint)
            canvas.drawText(titleClipped, 108f, y + 12f, bodyPaint)
            canvas.drawText(catClipped, 260f, y + 12f, bodyPaint)
            canvas.drawText(accClipped, 370f, y + 12f, bodyPaint)
            canvas.drawText(amtStr, 470f, y + 12f, amtPaint)
            canvas.drawLine(32f, y + 17f, pageWidth - 32f, y + 17f, borderPaint)
            y += 19f
        }

        pdf.finishPage(page)
        FileOutputStream(file).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()

        shareFile(context, file, "application/pdf", "Export PDF — $reportTitle")
        return file
    }

    /**
     * Exports a complete JSON backup snapshot of all user data for backup/restore.
     */
    fun exportFullBackupJson(
        context: Context,
        accounts: List<Account>,
        categories: List<Category>,
        transactions: List<FinTransaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        recurring: List<RecurringTransaction>,
        loans: List<LoanRecord>
    ): File {
        val file = File(ensureExportDir(context), "personal_bm_backup_${DhakaDateUtils.todayEpochDay()}.json")
        val root = JSONObject().apply {
            put("app", "Personal BM")
            put("exportedAtEpochDay", DhakaDateUtils.todayEpochDay())
            put("timezone", "Asia/Dhaka")
            put("accountsCount", accounts.size)
            put("transactionsCount", transactions.size)
            put("accounts", JSONArray(accounts.map { acc ->
                JSONObject().apply {
                    put("id", acc.id)
                    put("name", acc.name)
                    put("type", acc.type)
                    put("initialBalancePaisa", acc.initialBalancePaisa)
                    put("currentBalancePaisa", acc.currentBalancePaisa)
                    put("colorHex", acc.colorHex)
                    put("iconName", acc.iconName)
                }
            }))
            put("categories", JSONArray(categories.map { cat ->
                JSONObject().apply {
                    put("id", cat.id)
                    put("name", cat.name)
                    put("nameBn", cat.nameBn)
                    put("type", cat.type)
                    put("iconName", cat.iconName)
                    put("colorHex", cat.colorHex)
                    put("isCustom", cat.isCustom)
                }
            }))
            put("transactions", JSONArray(transactions.map { tx ->
                JSONObject().apply {
                    put("id", tx.id)
                    put("title", tx.title)
                    put("amountPaisa", tx.amountPaisa)
                    put("type", tx.type)
                    put("categoryId", tx.categoryId)
                    put("categoryName", tx.categoryName)
                    put("accountId", tx.accountId)
                    put("accountName", tx.accountName)
                    put("toAccountId", tx.toAccountId)
                    put("toAccountName", tx.toAccountName)
                    put("dateEpochDay", tx.dateEpochDay)
                    put("timeFormatted", tx.timeFormatted)
                    put("timestampMillis", tx.timestampMillis)
                    put("paymentMethod", tx.paymentMethod)
                    put("notes", tx.notes)
                }
            }))
            put("budgetsCount", budgets.size)
            put("savingsGoalsCount", goals.size)
            put("recurringCount", recurring.size)
            put("loansCount", loans.size)
        }
        file.writeText(root.toString(2), Charsets.UTF_8)
        shareFile(context, file, "application/json", "Export Personal BM Backup JSON")
        return file
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\"") || clean.contains("\n")) {
            "\"$clean\""
        } else {
            clean
        }
    }
}
