package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.MoneyUtils
import com.example.data.model.UserSettings
import com.example.ui.components.TransactionRowItem
import com.example.ui.components.UserAvatarBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VibrantCyan
import com.example.ui.viewmodel.PersonalBmUiState
import com.example.util.ExportAndBackupManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Section 15: Interactive Financial Calendar with daily transaction indicators,
 * recurring due markers, month navigation, and click-to-inspect date details.
 */
@Composable
fun FinancialCalendarScreen(
    uiState: PersonalBmUiState,
    onMonthChange: (String) -> Unit,
    onSelectDate: (Long) -> Unit,
    onAddTransactionOnDate: (Long) -> Unit = {},
    onEditTransaction: (FinTransaction) -> Unit,
    onDuplicateTransaction: (FinTransaction) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val monthKey = uiState.selectedMonthKey
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = parts.getOrNull(1)?.toIntOrNull() ?: 11

    val daysInMonth = DhakaDateUtils.daysInMonth(year, month)
    val startOffset = DhakaDateUtils.firstDayOfWeekOffset(year, month)
    val selectedDayEpoch = uiState.selectedCalendarEpochDay

    val txsByEpochDay = remember(uiState.transactions) {
        uiState.transactions.groupBy { it.dateEpochDay }
    }
    val recurringDueEpochDays = remember(uiState.recurringTransactions) {
        uiState.recurringTransactions.filter { it.isActive }.map { it.nextDueDateEpochDay }.toSet()
    }

    val selectedDayTxs = txsByEpochDay[selectedDayEpoch] ?: emptyList()
    val selectedDayIncome = selectedDayTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    val selectedDayExpense = selectedDayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onMonthChange(DhakaDateUtils.previousMonthKey(monthKey)) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }
                    Text(
                        text = DhakaDateUtils.formatMonthKeyDisplay(monthKey),
                        style = MaterialTheme.typography.titleSmall,
                        color = RoyalBlue
                    )
                    IconButton(onClick = { onMonthChange(DhakaDateUtils.nextMonthKey(monthKey)) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Calendar Grid Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Weekday Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { wd ->
                            Text(
                                text = wd,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7
                    for (week in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (dow in 0..6) {
                                val cellIdx = week * 7 + dow
                                val dayNum = cellIdx - startOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val epoch = DhakaDateUtils.toEpochDay(year, month, dayNum)
                                    val dayTxs = txsByEpochDay[epoch] ?: emptyList()
                                    val hasIncome = dayTxs.any { it.type == "INCOME" }
                                    val hasExpense = dayTxs.any { it.type == "EXPENSE" }
                                    val hasRecurring = recurringDueEpochDays.contains(epoch)
                                    val isSelected = epoch == selectedDayEpoch

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) RoyalBlue.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) RoyalBlue else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onSelectDate(epoch) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                if (hasIncome) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(EmeraldSuccess)
                                                    )
                                                }
                                                if (hasExpense) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(CrimsonDanger)
                                                    )
                                                }
                                                if (hasRecurring) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(AmberWarning)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendDot(color = EmeraldSuccess, label = "Income")
                        LegendDot(color = CrimsonDanger, label = "Expense")
                        LegendDot(color = AmberWarning, label = "Recurring Due")
                    }
                }
            }
        }

        // Selected Day Summary & Transactions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DhakaDateUtils.formatFullDateWithDay(selectedDayEpoch),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedDayTxs.size} entries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${MoneyUtils.formatPaisa(selectedDayIncome, symbol)}",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "-${MoneyUtils.formatPaisa(selectedDayExpense, symbol)}",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = CrimsonDanger
                            )
                        }
                    }
                    Button(
                        onClick = { onAddTransactionOnDate(selectedDayEpoch) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_add_tx_on_date_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Transaction on ${DhakaDateUtils.formatEpochDay(selectedDayEpoch)}")
                    }
                }
            }
        }

        items(selectedDayTxs, key = { it.id }) { tx ->
            TransactionRowItem(
                tx = tx,
                currencySymbol = symbol,
                onEdit = { onEditTransaction(tx) },
                onDuplicate = { onDuplicateTransaction(tx) },
                onDelete = { onDeleteTransaction(tx) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Section 16: Reports and Exports (8 Report types: Daily, Weekly, Monthly, Yearly, Category, Account, Income, Expense)
 * with CSV & PDF generation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsAndExportsScreen(
    uiState: PersonalBmUiState
) {
    val context = LocalContext.current
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val today = DhakaDateUtils.todayEpochDay()
    val currentMonthKey = DhakaDateUtils.currentMonthKey()
    val (curYear, _, _) = DhakaDateUtils.epochDayToYearMonthDay(today)

    var reportType by remember { mutableStateOf("MONTHLY") }
    var selectedCategoryId by remember { mutableStateOf(uiState.categories.firstOrNull()?.id ?: "") }
    var selectedAccountId by remember { mutableStateOf(uiState.accounts.firstOrNull()?.id ?: "") }

    val reportTransactions = remember(uiState.transactions, reportType, selectedCategoryId, selectedAccountId, today) {
        when (reportType) {
            "DAILY" -> uiState.transactions.filter { it.dateEpochDay == today }
            "WEEKLY" -> uiState.transactions.filter { it.dateEpochDay in (today - 6L)..today }
            "MONTHLY" -> uiState.transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey }
            "YEARLY" -> uiState.transactions.filter { DhakaDateUtils.epochDayToYearMonthDay(it.dateEpochDay).first == curYear }
            "CATEGORY" -> uiState.transactions.filter { it.categoryId == selectedCategoryId }
            "ACCOUNT" -> uiState.transactions.filter { it.accountId == selectedAccountId || it.toAccountId == selectedAccountId }
            "INCOME" -> uiState.transactions.filter { it.type == "INCOME" }
            "EXPENSE" -> uiState.transactions.filter { it.type == "EXPENSE" }
            else -> uiState.transactions
        }
    }

    val totalIn = reportTransactions.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    val totalOut = reportTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    val netFlow = totalIn - totalOut

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reports & Exports",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Export CSV or PDF statements",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Report Type Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Report Type", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "DAILY" to "Daily Report",
                            "WEEKLY" to "Weekly Report",
                            "MONTHLY" to "Monthly Report",
                            "YEARLY" to "Yearly Report",
                            "CATEGORY" to "Category Report",
                            "ACCOUNT" to "Account Report",
                            "INCOME" to "Income Report",
                            "EXPENSE" to "Expense Report"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = reportType == key,
                                onClick = { reportType = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    if (reportType == "CATEGORY") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Filter Category", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryId == cat.id,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                    }

                    if (reportType == "ACCOUNT") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Filter Account", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.accounts.forEach { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Printable Report Preview & Export Actions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$reportType FINANCIAL STATEMENT",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${reportTransactions.size} transactions • Generated in Asia/Dhaka",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Income", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = MoneyUtils.formatPaisa(totalIn, symbol, true),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = EmeraldSuccess
                            )
                        }
                        Column {
                            Text("Expenses", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = MoneyUtils.formatPaisa(totalOut, symbol, true),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = CrimsonDanger
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Balance", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = MoneyUtils.formatPaisa(netFlow, symbol, true, showPlusSign = true),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = if (netFlow >= 0L) RoyalBlue else CrimsonDanger
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                ExportAndBackupManager.exportTransactionsCsv(
                                    context = context,
                                    reportTitle = "$reportType Report",
                                    transactions = reportTransactions,
                                    currencySymbol = symbol
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_csv_button")
                        ) {
                            Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV")
                        }

                        Button(
                            onClick = {
                                ExportAndBackupManager.exportFinancialReportPdf(
                                    context = context,
                                    reportTitle = "$reportType Financial Report",
                                    periodSubtitle = DhakaDateUtils.formatEpochDay(today),
                                    transactions = reportTransactions,
                                    accounts = uiState.accounts,
                                    currencySymbol = symbol
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF")
                        }
                    }
                }
            }
        }

        items(reportTransactions.take(25), key = { it.id }) { tx ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tx.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${DhakaDateUtils.formatEpochDay(tx.dateEpochDay)} • ${tx.categoryName} • ${tx.accountName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = MoneyUtils.formatPaisa(tx.amountPaisa, symbol, true),
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = when (tx.type) {
                            "INCOME" -> EmeraldSuccess
                            "EXPENSE" -> CrimsonDanger
                            else -> VibrantCyan
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Section 19: Settings (Profile, Currency BDT/৳, Timezone Asia/Dhaka, Theme Light/Dark/System,
 * Language English / বাংলা, Default Account, Week Start Day, Data Backup & Sign Out).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: PersonalBmUiState,
    isCloudSignedIn: Boolean = false,
    onUpdateSettings: (UserSettings) -> Unit,
    onUpdateUserProfile: (UserSettings, String, Boolean) -> Unit = { s, _, _ -> onUpdateSettings(s) },
    onSyncToCloud: () -> Unit = {},
    onSignOutClick: () -> Unit
) {
    val context = LocalContext.current
    val current = uiState.settings
    val googleUser = remember(isCloudSignedIn) {
        try { Firebase.auth.currentUser } catch (_: Exception) { null }
    }
    val googlePhotoUrl = remember(googleUser) {
        googleUser?.photoUrl?.toString() ?: ""
    }

    var displayName by remember(current.displayName) { mutableStateOf(current.displayName) }
    var email by remember(current.email, googleUser?.email) {
        mutableStateOf(current.email.ifBlank { googleUser?.email ?: "" })
    }
    var photoUrl by remember(uiState.photoUrl, googlePhotoUrl) {
        mutableStateOf(uiState.photoUrl.ifBlank { googlePhotoUrl })
    }
    var isCustomPhoto by remember { mutableStateOf(false) }

    var currencyCode by remember(current.currencyCode) { mutableStateOf(current.currencyCode.ifBlank { "BDT" }) }
    var currencySymbol by remember(current.currencySymbol) { mutableStateOf(current.currencySymbol.ifBlank { "৳" }) }
    var themeMode by remember(current.themeMode) { mutableStateOf(current.themeMode.ifBlank { "light" }) }
    var language by remember(current.language) { mutableStateOf(current.language) }
    var defaultAccountId by remember(current.defaultAccountId) { mutableStateOf(current.defaultAccountId) }
    var weekStartDay by remember(current.weekStartDay) { mutableStateOf(current.weekStartDay) }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUrl = uri.toString()
            isCustomPhoto = true
            onUpdateUserProfile(
                current.copy(
                    displayName = displayName.trim().ifBlank { "Personal BM User" }.take(120),
                    email = email.trim().take(254)
                ),
                photoUrl,
                true
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == "bn") "সেটিংস" else "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Google Auth & User Profile Card (Detects Google Photo & Name + Fully Updatable)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UserAvatarBadge(
                            displayName = displayName.ifBlank { "Guest" },
                            photoUrl = photoUrl,
                            size = 64.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName.ifBlank { "Guest" },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isCloudSignedIn) email.ifBlank { "Google Account Connected" } else "Logged in as Guest (Local Device Storage)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = if (isCloudSignedIn) EmeraldSuccess.copy(alpha = 0.14f) else RoyalBlue.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCloudSignedIn) Icons.Default.CloudDone else Icons.Default.Backup,
                                        contentDescription = null,
                                        tint = if (isCloudSignedIn) EmeraldSuccess else RoyalBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCloudSignedIn) "Personal Google Account • Saved to Cloud" else "Guest Mode • Local Storage Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCloudSignedIn) EmeraldSuccess else RoyalBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                profilePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Photo")
                        }
                        if (googlePhotoUrl.isNotBlank() && photoUrl != googlePhotoUrl) {
                            TextButton(
                                onClick = {
                                    photoUrl = googlePhotoUrl
                                    isCustomPhoto = false
                                    onUpdateUserProfile(
                                        current.copy(displayName = displayName.trim().ifBlank { googleUser?.displayName ?: "User" }),
                                        googlePhotoUrl,
                                        false
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Use Google Photo")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name (Detected from Google or Custom)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_display_name_input")
                    )

                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = {
                            photoUrl = it
                            isCustomPhoto = true
                        },
                        label = { Text("Profile Image URL / URI (Updatable)") },
                        placeholder = { Text("https://... or choose photo above") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_photo_url_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = currencyCode,
                            onValueChange = { currencyCode = it },
                            label = { Text("Currency Code") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = currencySymbol,
                            onValueChange = { currencySymbol = it },
                            label = { Text("Symbol") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Language (ভাষা)", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = language == "en",
                            onClick = {
                                language = "en"
                                onUpdateSettings(current.copy(language = "en", themeMode = themeMode))
                            },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = language == "bn",
                            onClick = {
                                language = "bn"
                                onUpdateSettings(current.copy(language = "bn", themeMode = themeMode))
                            },
                            label = { Text("বাংলা (Bengali)") }
                        )
                    }

                    Text("Theme", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("light" to "Light", "dark" to "Dark & Orange", "system" to "Auto").forEach { (k, label) ->
                            FilterChip(
                                selected = themeMode == k,
                                onClick = {
                                    themeMode = k
                                    onUpdateSettings(current.copy(themeMode = k, language = language))
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("settings_theme_$k")
                            )
                        }
                    }

                    Text("Default Transaction Account", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.accounts.forEach { acc ->
                            FilterChip(
                                selected = defaultAccountId == acc.id,
                                onClick = {
                                    defaultAccountId = acc.id
                                    onUpdateSettings(current.copy(defaultAccountId = acc.id, themeMode = themeMode, language = language))
                                },
                                label = { Text(acc.name) }
                            )
                        }
                    }

                    Text("Week Boundary Start Day", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SATURDAY", "SUNDAY", "MONDAY").forEach { day ->
                            FilterChip(
                                selected = weekStartDay == day,
                                onClick = {
                                    weekStartDay = day
                                    onUpdateSettings(current.copy(weekStartDay = day, themeMode = themeMode, language = language))
                                },
                                label = { Text(day) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onUpdateUserProfile(
                                current.copy(
                                    displayName = displayName.trim().ifBlank { "Personal BM User" }.take(120),
                                    email = email.trim().take(254),
                                    currencyCode = currencyCode.trim().ifBlank { "BDT" }.take(10),
                                    currencySymbol = currencySymbol.trim().ifBlank { "৳" }.take(10),
                                    timezone = "Asia/Dhaka",
                                    language = language,
                                    themeMode = themeMode,
                                    defaultAccountId = defaultAccountId,
                                    weekStartDay = weekStartDay
                                ),
                                photoUrl.trim(),
                                isCustomPhoto
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_settings_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Profile & Save Preferences")
                    }
                }
            }
        }

        // Google Cloud Sync, Local Storage & Backup Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Firebase Cloud Sync & Local Backup", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (isCloudSignedIn) {
                            "Signed in with Google (${email.ifBlank { current.displayName }}). All transactions, monthly allowances, expenses, savings goals, and profile updates are automatically saved into Firebase Firestore and mirrored locally."
                        } else {
                            "Currently running in Default Local Storage mode. Sign in with Google below at any time to detect your Google profile photo & name and save all your data to Firebase Cloud."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isCloudSignedIn) {
                        Button(
                            onClick = onSyncToCloud,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sync_firebase_cloud_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync All Records to Firebase Now")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            ExportAndBackupManager.exportFullBackupJson(
                                context = context,
                                accounts = uiState.accounts,
                                categories = uiState.categories,
                                transactions = uiState.transactions,
                                budgets = uiState.budgets,
                                goals = uiState.savingsGoals,
                                recurring = uiState.recurringTransactions,
                                loans = uiState.loans
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_json_button")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Complete JSON Backup")
                    }

                    Button(
                        onClick = onSignOutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCloudSignedIn) CrimsonDanger else RoyalBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = if (isCloudSignedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isCloudSignedIn) "Sign Out of Google Account (Switch to Local)"
                            else "Sign in with Google & Save to Firebase"
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
