package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DhakaDateUtils
import com.example.data.model.LoanRecord
import com.example.data.model.MoneyUtils
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.parseHexColor
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SummaryMetricCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VibrantCyan
import com.example.ui.viewmodel.PersonalBmUiState
import kotlin.math.max

/**
 * Section 11: Budget Management with Monthly Total & Category-Specific Budgets,
 * Progress Bars, Previous Month Comparisons, and 75%, 90%, and 100% Warnings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetsScreen(
    uiState: PersonalBmUiState,
    onMonthChange: (String) -> Unit,
    onSaveBudget: (Budget) -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val monthKey = uiState.selectedMonthKey
    val prevMonthKey = DhakaDateUtils.previousMonthKey(monthKey)
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    val expenseCategories = remember(uiState.categories) {
        uiState.categories.filter { it.type == "EXPENSE" }
    }
    val monthBudgets = remember(uiState.budgets, monthKey) {
        uiState.budgets.filter { it.monthKey == monthKey }
    }
    val monthExpenses = remember(uiState.transactions, monthKey) {
        uiState.transactions.filter {
            it.type == "EXPENSE" && DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == monthKey
        }
    }
    val prevMonthExpenses = remember(uiState.transactions, prevMonthKey) {
        uiState.transactions.filter {
            it.type == "EXPENSE" && DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == prevMonthKey
        }
    }

    val totalBudgetLimit = monthBudgets.sumOf { it.limitPaisa }
    val totalMonthSpent = monthExpenses.sumOf { it.amountPaisa }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            monthKey = monthKey,
            expenseCategories = expenseCategories,
            currencySymbol = symbol,
            onDismiss = { showAddBudgetDialog = false },
            onSave = { bgt ->
                onSaveBudget(bgt)
                showAddBudgetDialog = false
            }
        )
    }

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
                Column {
                    Text(
                        text = "Monthly Budgets",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onMonthChange(DhakaDateUtils.previousMonthKey(monthKey)) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }
                        Text(
                            text = DhakaDateUtils.formatMonthKeyDisplay(monthKey),
                            style = MaterialTheme.typography.titleMedium,
                            color = RoyalBlue
                        )
                        IconButton(onClick = { onMonthChange(DhakaDateUtils.nextMonthKey(monthKey)) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }
                }
                Button(
                    onClick = { showAddBudgetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_budget_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set Budget")
                }
            }
        }

        // Overall Budget Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricCard(
                    title = "Total Budgeted",
                    amountPaisa = totalBudgetLimit,
                    currencySymbol = symbol,
                    subtitle = "${monthBudgets.size} category limits",
                    accentColor = RoyalBlue,
                    icon = Icons.Default.Savings,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = "Remaining Budget",
                    amountPaisa = totalBudgetLimit - totalMonthSpent,
                    currencySymbol = symbol,
                    subtitle = "Spent: ${MoneyUtils.formatPaisa(totalMonthSpent, symbol)}",
                    accentColor = if (totalBudgetLimit >= totalMonthSpent) EmeraldSuccess else CrimsonDanger,
                    icon = Icons.Default.WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (monthBudgets.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Budgets Set for ${DhakaDateUtils.formatMonthKeyDisplay(monthKey)}",
                    subtitle = "Create category spending limits (e.g., Food ৳5,000, Transportation ৳3,000) to receive 75%, 90%, and 100% threshold warnings.",
                    actionLabel = "Create First Budget",
                    onAction = { showAddBudgetDialog = true }
                )
            }
        } else {
            items(monthBudgets, key = { it.id }) { bgt ->
                val spent = if (bgt.categoryId == "ALL") {
                    totalMonthSpent
                } else {
                    monthExpenses.filter { it.categoryId == bgt.categoryId }.sumOf { it.amountPaisa }
                }
                val prevSpent = if (bgt.categoryId == "ALL") {
                    prevMonthExpenses.sumOf { it.amountPaisa }
                } else {
                    prevMonthExpenses.filter { it.categoryId == bgt.categoryId }.sumOf { it.amountPaisa }
                }
                val remaining = bgt.limitPaisa - spent
                val pct = if (bgt.limitPaisa > 0L) ((spent * 100.0) / bgt.limitPaisa).toInt() else 0
                val statusColor = when {
                    pct >= 100 -> CrimsonDanger
                    pct >= 75 -> AmberWarning
                    else -> EmeraldSuccess
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = bgt.categoryName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Last month spent: ${MoneyUtils.formatPaisa(prevSpent, symbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = statusColor.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$pct% used",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteBudget(bgt.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete budget", tint = CrimsonDanger)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${MoneyUtils.formatPaisa(spent, symbol, showDecimals = true)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Limit: ${MoneyUtils.formatPaisa(bgt.limitPaisa, symbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (remaining >= 0L) "Left: ${MoneyUtils.formatPaisa(remaining, symbol)}"
                                else "Over: ${MoneyUtils.formatPaisa(-remaining, symbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }

                        if (pct >= 75) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = statusColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            pct >= 100 -> "100% Threshold Reached: You have exceeded your ${bgt.categoryName} budget!"
                                            pct >= 90 -> "90% Warning: Almost out of budget for ${bgt.categoryName}."
                                            else -> "75% Alert: You have used three-quarters of your ${bgt.categoryName} budget."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddBudgetDialog(
    monthKey: String,
    expenseCategories: List<Category>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(expenseCategories.firstOrNull()?.id ?: "ALL") }
    var limitInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget (${DhakaDateUtils.formatMonthKeyDisplay(monthKey)})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Category", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryId == "ALL",
                        onClick = { selectedCategoryId = "ALL" },
                        label = { Text("Overall Monthly Budget") }
                    )
                    expenseCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Monthly Limit ($currencySymbol)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitPaisa = MoneyUtils.parseTakaToPaisa(limitInput) ?: return@Button
                    val catName = if (selectedCategoryId == "ALL") {
                        "Overall Monthly Budget"
                    } else {
                        expenseCategories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Category"
                    }
                    onSave(
                        Budget(
                            monthKey = monthKey,
                            categoryId = selectedCategoryId,
                            categoryName = catName,
                            limitPaisa = limitPaisa
                        )
                    )
                }
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Section 12: Savings Goals Management System with Contributions (never double-counted as income).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavingsGoalsScreen(
    uiState: PersonalBmUiState,
    onSaveGoal: (SavingsGoal) -> Unit,
    onContribute: (String, Long, String, String, String) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var contributingGoal by remember { mutableStateOf<SavingsGoal?>(null) }

    val totalSaved = uiState.savingsGoals.sumOf { it.currentSavedPaisa }
    val totalTarget = uiState.savingsGoals.sumOf { it.targetAmountPaisa }

    if (showCreateGoalDialog) {
        CreateSavingsGoalDialog(
            currencySymbol = symbol,
            onDismiss = { showCreateGoalDialog = false },
            onSave = { goal ->
                onSaveGoal(goal)
                showCreateGoalDialog = false
            }
        )
    }

    if (contributingGoal != null) {
        AddSavingsContributionDialog(
            goal = contributingGoal!!,
            accounts = uiState.accounts,
            currencySymbol = symbol,
            onDismiss = { contributingGoal = null },
            onConfirm = { amtPaisa, accId, accName, notes ->
                onContribute(contributingGoal!!.id, amtPaisa, accId, accName, notes)
                contributingGoal = null
            }
        )
    }

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
                Column {
                    Text(
                        text = "Savings Goals",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Saved ${MoneyUtils.formatPaisa(totalSaved, symbol)} of ${MoneyUtils.formatPaisa(totalTarget, symbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSuccess
                    )
                }
                Button(
                    onClick = { showCreateGoalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_savings_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Goal")
                }
            }
        }

        if (uiState.savingsGoals.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Savings Goals Yet",
                    subtitle = "Plan for a laptop, emergency fund, university tuition, or travel and track contributions over time.",
                    actionLabel = "Create Savings Goal",
                    onAction = { showCreateGoalDialog = true }
                )
            }
        } else {
            items(uiState.savingsGoals, key = { it.id }) { goal ->
                val pct = if (goal.targetAmountPaisa > 0L) {
                    ((goal.currentSavedPaisa * 100.0) / goal.targetAmountPaisa).toInt()
                } else 0
                val goalColor = parseHexColor(goal.colorHex, EmeraldSuccess)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Target Date: ${DhakaDateUtils.formatEpochDay(goal.targetDateEpochDay)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (goal.description.isNotBlank()) {
                                    Text(
                                        text = goal.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { contributingGoal = goal },
                                    colors = ButtonDefaults.buttonColors(containerColor = goalColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("+ Add Funds")
                                }
                                IconButton(onClick = { onDeleteGoal(goal.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete goal", tint = CrimsonDanger)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            color = goalColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Saved: ${MoneyUtils.formatPaisa(goal.currentSavedPaisa, symbol, showDecimals = true)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = goalColor
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = goalColor
                            )
                            Text(
                                text = "Target: ${MoneyUtils.formatPaisa(goal.targetAmountPaisa, symbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun CreateSavingsGoalDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (SavingsGoal) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var savedStr by remember { mutableStateOf("0") }
    var targetEpochDay by remember { mutableLongStateOf(DhakaDateUtils.todayEpochDay() + 90L) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Emergency Fund / Laptop)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = savedStr,
                    onValueChange = { savedStr = it },
                    label = { Text("Initial Saved Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val (y, m, d) = DhakaDateUtils.epochDayToYearMonthDay(targetEpochDay)
                        DatePickerDialog(
                            context,
                            { _, selY, selM, selD ->
                                targetEpochDay = DhakaDateUtils.toEpochDay(selY, selM + 1, selD)
                            },
                            y,
                            m - 1,
                            d
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Target Date: ${DhakaDateUtils.formatEpochDay(targetEpochDay)}")
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetPaisa = MoneyUtils.parseTakaToPaisa(targetStr) ?: return@Button
                    val savedPaisa = MoneyUtils.parseTakaToPaisa(savedStr, allowZero = true) ?: 0L
                    onSave(
                        SavingsGoal(
                            name = name.trim().take(120),
                            targetAmountPaisa = targetPaisa,
                            currentSavedPaisa = savedPaisa,
                            targetDateEpochDay = targetEpochDay,
                            description = description.trim().take(500),
                            colorHex = "#10B981",
                            iconName = "savings"
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddSavingsContributionDialog(
    goal: SavingsGoal,
    accounts: List<Account>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Money to ${goal.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Contribution Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Deduct From Account (Optional)", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedAccountId.isBlank(),
                        onClick = { selectedAccountId = "" },
                        label = { Text("External / None") }
                    )
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtPaisa = MoneyUtils.parseTakaToPaisa(amountStr) ?: return@Button
                    val accName = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: ""
                    onConfirm(amtPaisa, selectedAccountId, accName, notes.trim())
                }
            ) {
                Text("Contribute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Section 13: Recurring Transactions (Daily, Weekly, Monthly, Yearly) with Pause/Resume & Auto-Posting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurringTransactionsScreen(
    uiState: PersonalBmUiState,
    onSaveRule: (RecurringTransaction) -> Unit,
    onToggleRule: (RecurringTransaction) -> Unit,
    onDeleteRule: (String) -> Unit,
    onRunCheckNow: () -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    if (showAddRuleDialog) {
        AddRecurringRuleDialog(
            accounts = uiState.accounts,
            categories = uiState.categories,
            currencySymbol = symbol,
            onDismiss = { showAddRuleDialog = false },
            onSave = { rule ->
                onSaveRule(rule)
                showAddRuleDialog = false
            }
        )
    }

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
                Column {
                    Text(
                        text = "Recurring Transactions",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Auto-posts due subscriptions, bills, rent & salary without duplicates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onRunCheckNow) {
                        Icon(Icons.Default.Refresh, contentDescription = "Process due items now")
                    }
                    Button(
                        onClick = { showAddRuleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_recurring_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Schedule")
                    }
                }
            }
        }

        if (uiState.recurringTransactions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Recurring Payments Scheduled",
                    subtitle = "Automate monthly rent, internet bills, subscriptions, or salary so due entries post automatically.",
                    actionLabel = "Add Recurring Rule",
                    onAction = { showAddRuleDialog = true }
                )
            }
        } else {
            items(uiState.recurringTransactions, key = { it.id }) { rule ->
                val isIncome = rule.type == "INCOME"
                val color = if (isIncome) EmeraldSuccess else CrimsonDanger

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = rule.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (rule.isActive) RoyalBlue.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (rule.isActive) rule.frequency else "PAUSED",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (rule.isActive) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${rule.categoryName} • ${rule.accountName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Next Due: ${DhakaDateUtils.formatEpochDay(rule.nextDueDateEpochDay)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = RoyalBlue
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (isIncome) "+" else "-"}${MoneyUtils.formatPaisa(rule.amountPaisa, symbol, showDecimals = true)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Row {
                                IconButton(onClick = { onToggleRule(rule) }) {
                                    Icon(
                                        imageVector = if (rule.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (rule.isActive) "Pause" else "Resume",
                                        tint = RoyalBlue
                                    )
                                }
                                IconButton(onClick = { onDeleteRule(rule.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete recurring rule",
                                        tint = CrimsonDanger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddRecurringRuleDialog(
    accounts: List<Account>,
    categories: List<Category>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (RecurringTransaction) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var nextDueEpochDay by remember { mutableLongStateOf(DhakaDateUtils.todayEpochDay()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "acc_cash") }
    val filteredCats = categories.filter { it.type == type }
    var selectedCategoryId by remember(type) { mutableStateOf(filteredCats.firstOrNull()?.id ?: "cat_exp_rent") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Recurring Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "EXPENSE", onClick = { type = "EXPENSE" }, label = { Text("Expense") })
                    FilterChip(selected = type == "INCOME", onClick = { type = "INCOME" }, label = { Text("Income") })
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Monthly House Rent / Broadband)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq) }
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        val (y, m, d) = DhakaDateUtils.epochDayToYearMonthDay(nextDueEpochDay)
                        DatePickerDialog(
                            context,
                            { _, selY, selM, selD ->
                                nextDueEpochDay = DhakaDateUtils.toEpochDay(selY, selM + 1, selD)
                            },
                            y,
                            m - 1,
                            d
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Due Date: ${DhakaDateUtils.formatEpochDay(nextDueEpochDay)}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtPaisa = MoneyUtils.parseTakaToPaisa(amountStr) ?: return@Button
                    val acc = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()
                    val cat = filteredCats.firstOrNull { it.id == selectedCategoryId } ?: filteredCats.firstOrNull()
                    onSave(
                        RecurringTransaction(
                            title = title.trim().ifBlank { "Recurring Entry" }.take(160),
                            amountPaisa = amtPaisa,
                            type = type,
                            categoryId = cat?.id ?: "cat_other",
                            categoryName = cat?.name ?: "Other",
                            accountId = acc?.id ?: "acc_cash",
                            accountName = acc?.name ?: "Cash",
                            frequency = frequency,
                            nextDueDateEpochDay = nextDueEpochDay,
                            lastProcessedDateEpochDay = 0L,
                            isActive = true,
                            reminderEnabled = true,
                            notes = ""
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Section 14: Loans and Debts Management (Money Borrowed vs Money Lent, Partial & Complete Repayments).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoansAndDebtsScreen(
    uiState: PersonalBmUiState,
    onSaveLoan: (LoanRecord) -> Unit,
    onRecordPayment: (LoanRecord, Long, String, String, String) -> Unit,
    onDeleteLoan: (String) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var payingLoan by remember { mutableStateOf<LoanRecord?>(null) }

    val totalBorrowedOwed = uiState.loans
        .filter { it.type == "BORROWED" && it.status == "ACTIVE" }
        .sumOf { it.outstandingAmountPaisa }
    val totalLentReceivable = uiState.loans
        .filter { it.type == "LENT" && it.status == "ACTIVE" }
        .sumOf { it.outstandingAmountPaisa }

    if (showAddLoanDialog) {
        AddLoanDialog(
            accounts = uiState.accounts,
            currencySymbol = symbol,
            onDismiss = { showAddLoanDialog = false },
            onSave = { loan ->
                onSaveLoan(loan)
                showAddLoanDialog = false
            }
        )
    }

    if (payingLoan != null) {
        RecordLoanPaymentDialog(
            loan = payingLoan!!,
            accounts = uiState.accounts,
            currencySymbol = symbol,
            onDismiss = { payingLoan = null },
            onConfirm = { amtPaisa, accId, accName, notes ->
                onRecordPayment(payingLoan!!, amtPaisa, accId, accName, notes)
                payingLoan = null
            }
        )
    }

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
                Column {
                    Text(
                        text = "Loans & Debts",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Track money borrowed, money lent & partial repayments",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddLoanDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_loan_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Loan")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricCard(
                    title = "Amount Owed (Debt)",
                    amountPaisa = totalBorrowedOwed,
                    currencySymbol = symbol,
                    subtitle = "Money Borrowed",
                    accentColor = CrimsonDanger,
                    icon = Icons.Default.WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = "Amount to Receive",
                    amountPaisa = totalLentReceivable,
                    currencySymbol = symbol,
                    subtitle = "Money Lent",
                    accentColor = EmeraldSuccess,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uiState.loans.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Active Loans or Debts",
                    subtitle = "Keep a clear record of money borrowed from or lent to friends, family, or institutions.",
                    actionLabel = "Record Loan / Debt",
                    onAction = { showAddLoanDialog = true }
                )
            }
        } else {
            items(uiState.loans, key = { it.id }) { loan ->
                val isBorrowed = loan.type == "BORROWED"
                val paidPaisa = (loan.principalAmountPaisa - loan.outstandingAmountPaisa).coerceAtLeast(0L)
                val pct = if (loan.principalAmountPaisa > 0L) {
                    ((paidPaisa * 100.0) / loan.principalAmountPaisa).toInt()
                } else 100
                val paymentsForLoan = uiState.loanPayments.filter { it.loanId == loan.id }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = loan.personOrOrg,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = if (loan.status == "PAID") EmeraldSuccess.copy(alpha = 0.14f)
                                        else if (isBorrowed) CrimsonDanger.copy(alpha = 0.14f)
                                        else VibrantCyan.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (loan.status == "PAID") "SETTLED" else loan.type,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (loan.status == "PAID") EmeraldSuccess
                                            else if (isBorrowed) CrimsonDanger else VibrantCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Due: ${DhakaDateUtils.formatEpochDay(loan.dueDateEpochDay)} • Principal: ${MoneyUtils.formatPaisa(loan.principalAmountPaisa, symbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (loan.status == "ACTIVE") {
                                    Button(
                                        onClick = { payingLoan = loan },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Repay")
                                    }
                                }
                                IconButton(onClick = { onDeleteLoan(loan.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete loan", tint = CrimsonDanger)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            color = EmeraldSuccess,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paid: ${MoneyUtils.formatPaisa(paidPaisa, symbol)} ($pct%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "Outstanding: ${MoneyUtils.formatPaisa(loan.outstandingAmountPaisa, symbol, showDecimals = true)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (loan.outstandingAmountPaisa > 0L) CrimsonDanger else EmeraldSuccess
                            )
                        }

                        if (paymentsForLoan.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Repayment History (${paymentsForLoan.size}): " +
                                    paymentsForLoan.take(3).joinToString(" • ") {
                                        "${MoneyUtils.formatPaisa(it.amountPaisa, symbol)} on ${DhakaDateUtils.formatEpochDayShort(it.dateEpochDay)}"
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun AddLoanDialog(
    accounts: List<Account>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (LoanRecord) -> Unit
) {
    val context = LocalContext.current
    var personOrOrg by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BORROWED") }
    var principalStr by remember { mutableStateOf("") }
    var dueDateEpochDay by remember { mutableLongStateOf(DhakaDateUtils.todayEpochDay() + 30L) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Loan / Debt Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "BORROWED",
                        onClick = { type = "BORROWED" },
                        label = { Text("Money Borrowed (I Owe)") }
                    )
                    FilterChip(
                        selected = type == "LENT",
                        onClick = { type = "LENT" },
                        label = { Text("Money Lent (Owed to Me)") }
                    )
                }
                OutlinedTextField(
                    value = personOrOrg,
                    onValueChange = { personOrOrg = it },
                    label = { Text("Person or Organization") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val (y, m, d) = DhakaDateUtils.epochDayToYearMonthDay(dueDateEpochDay)
                        DatePickerDialog(
                            context,
                            { _, selY, selM, selD ->
                                dueDateEpochDay = DhakaDateUtils.toEpochDay(selY, selM + 1, selD)
                            },
                            y,
                            m - 1,
                            d
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Due Date: ${DhakaDateUtils.formatEpochDay(dueDateEpochDay)}")
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val principalPaisa = MoneyUtils.parseTakaToPaisa(principalStr) ?: return@Button
                    val defaultAcc = accounts.firstOrNull()
                    onSave(
                        LoanRecord(
                            personOrOrg = personOrOrg.trim().take(120),
                            type = type,
                            principalAmountPaisa = principalPaisa,
                            outstandingAmountPaisa = principalPaisa,
                            startDateEpochDay = DhakaDateUtils.todayEpochDay(),
                            dueDateEpochDay = dueDateEpochDay,
                            accountId = defaultAcc?.id ?: "",
                            accountName = defaultAcc?.name ?: "",
                            status = "ACTIVE",
                            notes = notes.trim().take(500)
                        )
                    )
                },
                enabled = personOrOrg.isNotBlank()
            ) {
                Text("Save Loan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordLoanPaymentDialog(
    loan: LoanRecord,
    accounts: List<Account>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String) -> Unit
) {
    var amountStr by remember { mutableStateOf(MoneyUtils.paisaToInputString(loan.outstandingAmountPaisa)) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Repayment — ${loan.personOrOrg}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Outstanding Balance: ${MoneyUtils.formatPaisa(loan.outstandingAmountPaisa, currencySymbol, showDecimals = true)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Settlement Account", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtPaisa = MoneyUtils.parseTakaToPaisa(amountStr) ?: return@Button
                    val accName = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: ""
                    onConfirm(amtPaisa, selectedAccountId, accName, notes.trim())
                }
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
