package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.MoneyUtils
import com.example.data.model.parseHexColor
import com.example.ui.components.AdaptiveTransactionsSection
import com.example.ui.components.DonutSlice
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.iconForName
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.PersonalBmUiState
import com.example.ui.viewmodel.TransactionFilterState
import kotlin.math.ceil
import kotlin.math.max

/**
 * Section 6: Complete Transaction Management Screen with Search, Date/Type/Category/Account Filters,
 * Sorting, Pagination, Duplicate, Edit, and Delete with confirmation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    uiState: PersonalBmUiState,
    onFilterChange: ((TransactionFilterState) -> TransactionFilterState) -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (FinTransaction) -> Unit,
    onDuplicateTransaction: (FinTransaction) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val filter = uiState.txFilter
    val today = DhakaDateUtils.todayEpochDay()
    val currentMonthKey = DhakaDateUtils.currentMonthKey()
    val (curYear, _, _) = DhakaDateUtils.epochDayToYearMonthDay(today)

    val categoryColorMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.colorHex }
    }
    val categoryIconMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.iconName }
    }

    val filteredTransactions = remember(uiState.transactions, filter, today) {
        uiState.transactions
            .filter { tx ->
                val matchesQuery = filter.searchQuery.isBlank() ||
                    tx.title.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.categoryName.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.accountName.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.notes.contains(filter.searchQuery, ignoreCase = true)

                val matchesType = filter.typeFilter == "ALL" || tx.type == filter.typeFilter
                val matchesCategory = filter.categoryIdFilter == "ALL" || tx.categoryId == filter.categoryIdFilter
                val matchesAccount = filter.accountIdFilter == "ALL" ||
                    tx.accountId == filter.accountIdFilter || tx.toAccountId == filter.accountIdFilter

                val matchesPeriod = when (filter.periodFilter) {
                    "TODAY" -> tx.dateEpochDay == today
                    "THIS_WEEK" -> tx.dateEpochDay in (today - 6L)..today
                    "THIS_MONTH" -> DhakaDateUtils.epochDayToMonthKey(tx.dateEpochDay) == currentMonthKey
                    "THIS_YEAR" -> DhakaDateUtils.epochDayToYearMonthDay(tx.dateEpochDay).first == curYear
                    else -> true
                }

                matchesQuery && matchesType && matchesCategory && matchesAccount && matchesPeriod
            }
            .let { list ->
                when (filter.sortBy) {
                    "DATE_ASC" -> list.sortedWith(compareBy<FinTransaction> { it.dateEpochDay }.thenBy { it.timestampMillis })
                    "AMOUNT_DESC" -> list.sortedByDescending { it.amountPaisa }
                    "AMOUNT_ASC" -> list.sortedBy { it.amountPaisa }
                    else -> list.sortedWith(compareByDescending<FinTransaction> { it.dateEpochDay }.thenByDescending { it.timestampMillis })
                }
            }
    }

    val totalPages = max(1, ceil(filteredTransactions.size / filter.pageSize.toDouble()).toInt())
    val safePage = filter.currentPage.coerceIn(1, totalPages)
    val pagedTransactions = remember(filteredTransactions, safePage, filter.pageSize) {
        val fromIdx = (safePage - 1) * filter.pageSize
        val toIdx = (fromIdx + filter.pageSize).coerceAtMost(filteredTransactions.size)
        if (fromIdx in 0 until toIdx) filteredTransactions.subList(fromIdx, toIdx) else emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredTransactions.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onAddTransactionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("transactions_add_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add")
                }
            }
        }

        // Search & Filter Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = filter.searchQuery,
                        onValueChange = { q ->
                            onFilterChange { it.copy(searchQuery = q, currentPage = 1) }
                        },
                        label = { Text("Search title, category, account, notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (filter.searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { onFilterChange { it.copy(searchQuery = "", currentPage = 1) } }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Type Filter Row
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ALL" to "All Types",
                            "EXPENSE" to "Expenses",
                            "INCOME" to "Income",
                            "TRANSFER" to "Transfers"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = filter.typeFilter == key,
                                onClick = { onFilterChange { it.copy(typeFilter = key, currentPage = 1) } },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Date Period Filter Row
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ALL" to "All Dates",
                            "TODAY" to "Today",
                            "THIS_WEEK" to "Last 7 Days",
                            "THIS_MONTH" to "This Month",
                            "THIS_YEAR" to "This Year"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = filter.periodFilter == key,
                                onClick = { onFilterChange { it.copy(periodFilter = key, currentPage = 1) } },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Account Filter Row
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = filter.accountIdFilter == "ALL",
                            onClick = { onFilterChange { it.copy(accountIdFilter = "ALL", currentPage = 1) } },
                            label = { Text("All Accounts") }
                        )
                        uiState.accounts.forEach { acc ->
                            FilterChip(
                                selected = filter.accountIdFilter == acc.id,
                                onClick = { onFilterChange { it.copy(accountIdFilter = acc.id, currentPage = 1) } },
                                label = { Text(acc.name) }
                            )
                        }
                    }

                    // Sort By Row
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "DATE_DESC" to "Newest First",
                            "DATE_ASC" to "Oldest First",
                            "AMOUNT_DESC" to "Highest Amount",
                            "AMOUNT_ASC" to "Lowest Amount"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = filter.sortBy == key,
                                onClick = { onFilterChange { it.copy(sortBy = key, currentPage = 1) } },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        if (pagedTransactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                    EmptyStateCard(
                        title = "No Matching Transactions",
                        subtitle = "Try adjusting your filters or record a new income, expense, or transfer.",
                        actionLabel = "Add Transaction",
                        onAction = onAddTransactionClick
                    )
                }
            }
        } else {
            item {
                AdaptiveTransactionsSection(
                    transactions = pagedTransactions,
                    currencySymbol = symbol,
                    categoryColorMap = categoryColorMap,
                    categoryIconMap = categoryIconMap,
                    onEdit = onEditTransaction,
                    onDuplicate = onDuplicateTransaction,
                    onDelete = onDeleteTransaction,
                    modifier = Modifier.widthIn(max = 1280.dp)
                )
            }

            // Pagination Bar
            if (totalPages > 1) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1280.dp)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onFilterChange { it.copy(currentPage = (safePage - 1).coerceAtLeast(1)) } },
                            shape = RoundedCornerShape(10.dp),
                            enabled = safePage > 1
                        ) {
                            Text("← Previous")
                        }
                        Text(
                            text = "Page $safePage of $totalPages",
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedButton(
                            onClick = { onFilterChange { it.copy(currentPage = (safePage + 1).coerceAtMost(totalPages)) } },
                            shape = RoundedCornerShape(10.dp),
                            enabled = safePage < totalPages
                        ) {
                            Text("Next →")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

/**
 * Sections 7 & 8: Dedicated Income Management & Expense Management Modules with Custom Category creation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryTypedModuleScreen(
    moduleType: String, // "INCOME" or "EXPENSE"
    uiState: PersonalBmUiState,
    onAddTransactionWithType: (String) -> Unit,
    onEditTransaction: (FinTransaction) -> Unit,
    onDuplicateTransaction: (FinTransaction) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit,
    onSaveCategory: (Category) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    val isIncome = moduleType == "INCOME"
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val currentMonthKey = DhakaDateUtils.currentMonthKey()
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val typedCategories = remember(uiState.categories, moduleType) {
        uiState.categories.filter { it.type == moduleType }
    }
    val typedTransactions = remember(uiState.transactions, moduleType, selectedCategoryFilter) {
        uiState.transactions.filter {
            it.type == moduleType && (selectedCategoryFilter == "ALL" || it.categoryId == selectedCategoryFilter)
        }
    }

    val totalAllTime = typedTransactions.sumOf { it.amountPaisa }
    val currentMonthTotal = typedTransactions
        .filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey }
        .sumOf { it.amountPaisa }

    val categoryColorMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.colorHex }
    }
    val categoryIconMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.iconName }
    }

    val slices = remember(uiState.transactions, moduleType, categoryColorMap) {
        uiState.transactions.filter { it.type == moduleType }
            .groupBy { it.categoryId to it.categoryName }
            .map { (pair, list) ->
                DonutSlice(
                    id = pair.first,
                    label = pair.second.ifBlank { "Other" },
                    amountPaisa = list.sumOf { it.amountPaisa },
                    color = parseHexColor(
                        categoryColorMap[pair.first] ?: if (isIncome) "#059669" else "#E11D48",
                        if (isIncome) EmeraldSuccess else CrimsonDanger
                    )
                )
            }
            .sortedByDescending { it.amountPaisa }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = moduleType,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { cat ->
                onSaveCategory(cat)
                showAddCategoryDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isIncome) "Income" else "Expenses",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${typedTransactions.size} records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onAddTransactionWithType(moduleType) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) EmeraldSuccess else CrimsonDanger
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isIncome) "Add Income" else "Add Expense")
                }
            }
        }

        // Top Summary Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricCard(
                    title = if (isIncome) "Monthly Income" else "Monthly Expenses",
                    amountPaisa = currentMonthTotal,
                    currencySymbol = symbol,
                    subtitle = DhakaDateUtils.formatMonthKeyDisplay(currentMonthKey),
                    accentColor = if (isIncome) EmeraldSuccess else CrimsonDanger,
                    icon = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = if (isIncome) "Cumulative Income" else "Cumulative Expenses",
                    amountPaisa = totalAllTime,
                    currencySymbol = symbol,
                    subtitle = "${typedTransactions.size} total records",
                    accentColor = RoyalBlue,
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Distribution Chart
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isIncome) "Income Source Analysis" else "Category-Wise Expense Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InteractiveDonutChart(slices = slices, currencySymbol = symbol)
                }
            }
        }

        // Categories Filter & Custom Category Creator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categories (${typedCategories.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Custom Category", color = RoyalBlue)
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALL",
                            onClick = { selectedCategoryFilter = "ALL" },
                            label = { Text("All") }
                        )
                        typedCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat.id,
                                onClick = { selectedCategoryFilter = cat.id },
                                leadingIcon = {
                                    Icon(
                                        imageVector = iconForName(cat.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(cat.name) },
                                trailingIcon = if (cat.isCustom) {
                                    {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Delete custom category",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { onDeleteCategory(cat.id) }
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        item {
            AdaptiveTransactionsSection(
                transactions = typedTransactions,
                currencySymbol = symbol,
                categoryColorMap = categoryColorMap,
                categoryIconMap = categoryIconMap,
                onEdit = onEditTransaction,
                onDuplicate = onDuplicateTransaction,
                onDelete = onDeleteTransaction,
                modifier = Modifier.widthIn(max = 1280.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun AddCategoryDialog(
    type: String,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf(if (type == "INCOME") "#059669" else "#E11D48") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom ${if (type == "INCOME") "Income" else "Expense"} Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name (English)") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Category Name (বাংলা - Optional)") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#059669", "#2563EB", "#0284C7", "#8B5CF6", "#EC4899", "#D97706", "#E11D48").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorHex == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Category(
                                name = name.trim().take(80),
                                nameBn = nameBn.trim().ifBlank { name.trim() }.take(120),
                                type = type,
                                iconName = "category",
                                colorHex = colorHex,
                                isCustom = true
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Section 10: Accounts & Wallets Management (Cash, Bank Account, bKash, Nagad, Rocket, Upay, Custom)
 * with Transfer Money shortcut, Edit Account, Safe Delete, and Account Transaction History.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsAndWalletsScreen(
    uiState: PersonalBmUiState,
    onSaveAccount: (Account) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onTransferClick: () -> Unit,
    onEditTransaction: (FinTransaction) -> Unit,
    onDuplicateTransaction: (FinTransaction) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var selectedAccountForHistory by remember { mutableStateOf<String?>(null) }

    val totalBalance = remember(uiState.accounts) {
        uiState.accounts.sumOf { it.currentBalancePaisa }
    }

    val categoryColorMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.colorHex }
    }
    val categoryIconMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.iconName }
    }

    val accountHistoryTxs = remember(uiState.transactions, selectedAccountForHistory) {
        if (selectedAccountForHistory == null) emptyList()
        else uiState.transactions.filter {
            it.accountId == selectedAccountForHistory || it.toAccountId == selectedAccountForHistory
        }
    }

    if (showAccountDialog) {
        AccountEditorDialog(
            initial = editingAccount,
            currencySymbol = symbol,
            onDismiss = {
                showAccountDialog = false
                editingAccount = null
            },
            onSave = { acc ->
                onSaveAccount(acc)
                showAccountDialog = false
                editingAccount = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wallets & Accounts",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Total: ${MoneyUtils.formatPaisa(totalBalance, symbol, showDecimals = true)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalBlue
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onTransferClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("accounts_transfer_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Transfer")
                    }
                    Button(
                        onClick = {
                            editingAccount = null
                            showAccountDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_account_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }

        items(uiState.accounts, key = { it.id }) { acc ->
            val accColor = parseHexColor(acc.colorHex, RoyalBlue)
            val txCount = uiState.transactions.count { it.accountId == acc.id || it.toAccountId == acc.id }
            val isSelectedHistory = selectedAccountForHistory == acc.id

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = if (isSelectedHistory) 2.dp else 1.dp,
                    color = if (isSelectedHistory) accColor else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp)
                    .clickable {
                        selectedAccountForHistory = if (isSelectedHistory) null else acc.id
                    }
                    .testTag("account_card_${acc.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForName(acc.iconName),
                            contentDescription = acc.name,
                            tint = accColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = accColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = acc.type.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Opening: ${MoneyUtils.formatPaisa(acc.initialBalancePaisa, symbol)} • $txCount transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = MoneyUtils.formatPaisa(acc.currentBalancePaisa, symbol, showDecimals = true),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = if (acc.currentBalancePaisa >= 0L) MaterialTheme.colorScheme.onSurface else CrimsonDanger
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    editingAccount = acc
                                    showAccountDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit account", modifier = Modifier.size(16.dp))
                            }
                            if (txCount == 0 && !acc.isDefault) {
                                IconButton(
                                    onClick = { onDeleteAccount(acc.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete account",
                                        tint = CrimsonDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedAccountForHistory != null) {
            val accName = uiState.accounts.firstOrNull { it.id == selectedAccountForHistory }?.name ?: "Account"
            item {
                Column(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                    Text(
                        text = "$accName Transaction History (${accountHistoryTxs.size})",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    AdaptiveTransactionsSection(
                        transactions = accountHistoryTxs,
                        currencySymbol = symbol,
                        categoryColorMap = categoryColorMap,
                        categoryIconMap = categoryIconMap,
                        onEdit = onEditTransaction,
                        onDuplicate = onDuplicateTransaction,
                        onDelete = onDeleteTransaction
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountEditorDialog(
    initial: Account?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    val isEdit = initial != null
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "MOBILE_WALLET") }
    var initialBalanceStr by remember {
        mutableStateOf(
            if (initial != null) MoneyUtils.paisaToInputString(initial.initialBalancePaisa) else "0"
        )
    }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: "#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Account — ${initial?.name}" else "Create Account / Wallet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. City Bank / bKash Personal)") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Account Type", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CASH", "BANK", "MOBILE_WALLET", "OTHER").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.replace("_", " ")) }
                        )
                    }
                }
                OutlinedTextField(
                    value = initialBalanceStr,
                    onValueChange = { initialBalanceStr = it },
                    label = { Text("Initial / Opening Balance ($currencySymbol)") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#059669", "#2563EB", "#E2136E", "#F7941D", "#8C3494", "#0072BC", "#0284C7").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorHex == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val initPaisa = MoneyUtils.parseTakaToPaisa(initialBalanceStr, allowZero = true) ?: 0L
                    val iconName = when (type) {
                        "CASH" -> "payments"
                        "BANK" -> "account_balance"
                        "MOBILE_WALLET" -> "smartphone"
                        else -> "wallet"
                    }
                    onSave(
                        Account(
                            id = initial?.id ?: "",
                            userId = initial?.userId ?: "",
                            name = name.trim().take(80),
                            type = type,
                            initialBalancePaisa = initPaisa,
                            currentBalancePaisa = initial?.currentBalancePaisa ?: initPaisa,
                            colorHex = colorHex,
                            iconName = iconName,
                            isDefault = initial?.isDefault ?: false,
                            createdAt = initial?.createdAt,
                            updatedAt = null
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEdit) "Update" else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
