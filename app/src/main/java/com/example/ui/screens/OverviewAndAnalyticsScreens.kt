package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.DefaultSeedData
import com.example.data.model.DhakaDateUtils
import com.example.data.model.EventBreakdownItem
import com.example.data.model.FinTransaction
import com.example.data.model.LifeEventType
import com.example.data.model.MoneyUtils
import com.example.data.model.NavSection
import com.example.data.model.RecurringTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.parseHexColor
import com.example.ui.components.AdaptiveTransactionsSection
import com.example.ui.components.ChartBarPoint
import com.example.ui.components.DonutSlice
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.HeroTotalBalanceCard
import com.example.ui.components.InteractiveBarChart
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.InteractiveLineChart
import com.example.ui.components.QuickActionsStrip
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.iconForName
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VibrantCyan
import com.example.ui.viewmodel.PersonalBmUiState
import kotlinx.coroutines.delay
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewScreen(
    uiState: PersonalBmUiState,
    onAddTransactionClick: () -> Unit,
    onAddExpenseClick: () -> Unit = onAddTransactionClick,
    onAddIncomeClick: () -> Unit = onAddTransactionClick,
    onTransferMoneyClick: () -> Unit = onAddTransactionClick,
    onQuickAllowance: (Long, String, String, String, Boolean, Long, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onQuickExpense: (String, Long, String, String, String, String, String, Long, String) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onQuickSavings: (String, Long, Long, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onQuickEventExpense: (LifeEventType, String, List<EventBreakdownItem>, String, String, String, Long, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onEditTransaction: (FinTransaction) -> Unit,
    onDuplicateTransaction: (FinTransaction) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit,
    onNavigate: (NavSection) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val lang = uiState.settings.language
    val currentMonthKey = uiState.selectedMonthKey.ifBlank { DhakaDateUtils.currentMonthKey() }
    val prevMonthKey = DhakaDateUtils.previousMonthKey(currentMonthKey)

    val totalBalancePaisa = remember(uiState.accounts) {
        uiState.accounts.sumOf { it.currentBalancePaisa }
    }
    val totalIncomePaisa = remember(uiState.transactions) {
        uiState.transactions.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    }
    val totalExpensePaisa = remember(uiState.transactions) {
        uiState.transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    }
    val totalSavingsGoalsPaisa = remember(uiState.savingsGoals) {
        uiState.savingsGoals.sumOf { it.currentSavedPaisa }
    }

    val currentMonthTxs = remember(uiState.transactions, currentMonthKey) {
        uiState.transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey }
    }
    val prevMonthTxs = remember(uiState.transactions, prevMonthKey) {
        uiState.transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == prevMonthKey }
    }

    val currentMonthIncome = currentMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    val currentMonthExpense = currentMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    val currentMonthSavingsContribs = remember(uiState.savingsContributions, currentMonthKey) {
        uiState.savingsContributions
            .filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey }
            .sumOf { it.amountPaisa }
    }
    val currentMonthWalletDeductedSavings = remember(uiState.savingsContributions, currentMonthKey) {
        uiState.savingsContributions
            .filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey && it.sourceAccountId.isNotBlank() }
            .sumOf { it.amountPaisa }
    }
    val currentMonthRemaining = currentMonthIncome - currentMonthExpense - currentMonthWalletDeductedSavings
    val currentMonthNet = currentMonthIncome - currentMonthExpense

    val prevMonthNet = prevMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa } -
        prevMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    val momChangePct = if (prevMonthNet != 0L) {
        (((currentMonthNet - prevMonthNet) * 100.0) / kotlin.math.abs(prevMonthNet)).toInt()
    } else if (currentMonthIncome > 0L) {
        (((currentMonthIncome - currentMonthExpense) * 100.0) / currentMonthIncome).toInt()
    } else 0

    var trendTab by remember { mutableStateOf("DAILY") }
    // Default to Bar Chart (useSmoothLine = false) so the Bar Chart is immediately visible on the Dashboard!
    var useSmoothLine by remember { mutableStateOf(false) }
    val trendPoints = remember(uiState.transactions, trendTab) {
        buildTrendPoints(uiState.transactions, trendTab)
    }

    val sparklineValues = remember(trendPoints, totalBalancePaisa) {
        val pts = trendPoints.map { (it.incomePaisa - it.expensePaisa) + totalBalancePaisa / 2L }
        if (pts.size >= 2) pts else listOf(100L, 120L, 115L, 140L, 160L, 185L)
    }

    val categoryColorMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.colorHex }
    }
    val categoryIconMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.iconName }
    }

    val expenseDonutSlices = remember(currentMonthTxs, uiState.transactions, categoryColorMap) {
        val source = if (currentMonthTxs.any { it.type == "EXPENSE" }) currentMonthTxs else uiState.transactions
        source.filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId to it.categoryName }
            .map { (pair, list) ->
                DonutSlice(
                    id = pair.first,
                    label = pair.second.ifBlank { "Other" },
                    amountPaisa = list.sumOf { it.amountPaisa },
                    color = parseHexColor(categoryColorMap[pair.first] ?: "#2563EB", RoyalBlue)
                )
            }
            .sortedByDescending { it.amountPaisa }
    }

    val monthBudgets = remember(uiState.budgets, currentMonthKey) {
        uiState.budgets.filter { it.monthKey == currentMonthKey }
    }

    val upcomingBills = remember(uiState.recurringTransactions) {
        uiState.recurringTransactions
            .filter { it.isActive }
            .sortedBy { it.nextDueDateEpochDay }
            .take(4)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideDesktop = maxWidth >= 840.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideDesktop) 24.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                var liveTimeText by remember { mutableStateOf(DhakaDateUtils.formatTime12Hour(DhakaDateUtils.currentTimeFormatted())) }
                LaunchedEffect(Unit) {
                    while (true) {
                        liveTimeText = DhakaDateUtils.formatTime12Hour(DhakaDateUtils.currentTimeFormatted())
                        delay(15_000L)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1280.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (lang == "bn") "ড্যাশবোর্ড" else "Overview",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${DhakaDateUtils.formatEpochDay(DhakaDateUtils.todayEpochDay())} • $liveTimeText",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Button(
                        onClick = onAddTransactionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_add_tx_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == "bn") "যোগ" else "Add")
                    }
                }
            }

            // Hero Balance & Summary Metric Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isWideDesktop) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            HeroTotalBalanceCard(
                                totalBalancePaisa = totalBalancePaisa,
                                currentMonthNetPaisa = currentMonthRemaining,
                                momChangePct = momChangePct,
                                accounts = uiState.accounts,
                                sparklineValues = sparklineValues,
                                currencySymbol = symbol,
                                language = lang,
                                onWalletsClick = { onNavigate(NavSection.ACCOUNTS) },
                                modifier = Modifier.weight(1.35f)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SummaryMetricCard(
                                        title = if (lang == "bn") "মাসিক ভাতা" else "Allowance",
                                        amountPaisa = currentMonthIncome,
                                        currencySymbol = symbol,
                                        subtitle = "Total: ${MoneyUtils.formatPaisa(totalIncomePaisa, symbol)}",
                                        accentColor = EmeraldSuccess,
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryMetricCard(
                                        title = if (lang == "bn") "মাসিক ব্যয়" else "Expenses",
                                        amountPaisa = currentMonthExpense,
                                        currencySymbol = symbol,
                                        subtitle = "Total: ${MoneyUtils.formatPaisa(totalExpensePaisa, symbol)}",
                                        accentColor = CrimsonDanger,
                                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                SummaryMetricCard(
                                    title = if (lang == "bn") "সঞ্চয় ও অবশিষ্ট" else "Savings & Remaining",
                                    amountPaisa = totalSavingsGoalsPaisa,
                                    currencySymbol = symbol,
                                    subtitle = "Left: ${MoneyUtils.formatPaisa(currentMonthRemaining, symbol, showPlusSign = true)}",
                                    accentColor = RoyalBlue,
                                    icon = Icons.Default.Savings,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        HeroTotalBalanceCard(
                            totalBalancePaisa = totalBalancePaisa,
                            currentMonthNetPaisa = currentMonthRemaining,
                            momChangePct = momChangePct,
                            accounts = uiState.accounts,
                            sparklineValues = sparklineValues,
                            currencySymbol = symbol,
                            language = lang,
                            onWalletsClick = { onNavigate(NavSection.ACCOUNTS) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryMetricCard(
                                title = if (lang == "bn") "ভাতা ও আয়" else "Allowance",
                                amountPaisa = currentMonthIncome,
                                currencySymbol = symbol,
                                subtitle = "Total: ${MoneyUtils.formatPaisa(totalIncomePaisa, symbol)}",
                                accentColor = EmeraldSuccess,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricCard(
                                title = if (lang == "bn") "মাসিক ব্যয়" else "Expenses",
                                amountPaisa = currentMonthExpense,
                                currencySymbol = symbol,
                                subtitle = "Total: ${MoneyUtils.formatPaisa(totalExpensePaisa, symbol)}",
                                accentColor = CrimsonDanger,
                                icon = Icons.AutoMirrored.Filled.TrendingDown,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        SummaryMetricCard(
                            title = if (lang == "bn") "মোট সঞ্চয়" else "Savings",
                            amountPaisa = totalSavingsGoalsPaisa,
                            currencySymbol = symbol,
                            subtitle = "Remaining Allowance: ${MoneyUtils.formatPaisa(currentMonthRemaining, symbol, showPlusSign = true)}",
                            accentColor = RoyalBlue,
                            icon = Icons.Default.Savings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    QuickActionsStrip(
                        onAddExpense = onAddExpenseClick,
                        onAddIncome = onAddIncomeClick,
                        onTransferMoney = onTransferMoneyClick,
                        onViewReports = { onNavigate(NavSection.REPORTS) }
                    )

                    QuickFinancialEntryCard(
                        accounts = uiState.accounts,
                        categories = uiState.categories,
                        savingsGoals = uiState.savingsGoals,
                        defaultAccountId = uiState.settings.defaultAccountId,
                        currencySymbol = symbol,
                        currentMonthIncome = currentMonthIncome,
                        currentMonthExpense = currentMonthExpense,
                        currentMonthSavings = currentMonthSavingsContribs,
                        currentMonthRemaining = currentMonthRemaining,
                        onRecordAllowance = onQuickAllowance,
                        onRecordExpense = onQuickExpense,
                        onRecordSavings = onQuickSavings,
                        onRecordEventExpense = onQuickEventExpense
                    )

                    DashboardLifeEventsSection(
                        transactions = uiState.transactions,
                        accounts = uiState.accounts,
                        currencySymbol = symbol,
                        onAddEventCostItems = { type, name, items, accId, accName ->
                            onQuickEventExpense(
                                type,
                                name,
                                items,
                                accId,
                                accName,
                                "",
                                DhakaDateUtils.todayEpochDay(),
                                DhakaDateUtils.currentTimeFormatted()
                            )
                        },
                        onDeleteTransaction = onDeleteTransaction,
                        onOpenEventsModule = { onNavigate(NavSection.EVENTS) }
                    )
                }
            }

            if (uiState.transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                        EmptyStateCard(
                            title = "No Transactions Yet",
                            subtitle = "Add your first allowance, expense, or event above.",
                            actionLabel = "Add Transaction",
                            onAction = onAddTransactionClick
                        )
                    }
                }
            }

            // Cash Flow Bar Chart & Expense Categories Donut Chart
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                    if (isWideDesktop) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            CashFlowChartCard(
                                trendTab = trendTab,
                                onTrendTabChange = { trendTab = it },
                                useSmoothLine = useSmoothLine,
                                onToggleChartType = { useSmoothLine = !useSmoothLine },
                                trendPoints = trendPoints,
                                symbol = symbol,
                                onNavigateAnalytics = { onNavigate(NavSection.ANALYTICS) },
                                modifier = Modifier.weight(1.35f)
                            )
                            ExpenseCategoryDonutCard(
                                slices = expenseDonutSlices,
                                symbol = symbol,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            CashFlowChartCard(
                                trendTab = trendTab,
                                onTrendTabChange = { trendTab = it },
                                useSmoothLine = useSmoothLine,
                                onToggleChartType = { useSmoothLine = !useSmoothLine },
                                trendPoints = trendPoints,
                                symbol = symbol,
                                onNavigateAnalytics = { onNavigate(NavSection.ANALYTICS) }
                            )
                            ExpenseCategoryDonutCard(
                                slices = expenseDonutSlices,
                                symbol = symbol
                            )
                        }
                    }
                }
            }

            // Budget Progress & Financial Insights
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                    if (isWideDesktop) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            BudgetProgressCard(
                                monthBudgets = monthBudgets,
                                currentMonthExpense = currentMonthExpense,
                                currentMonthTxs = currentMonthTxs,
                                symbol = symbol,
                                onNavigateBudgets = { onNavigate(NavSection.BUDGETS) },
                                modifier = Modifier.weight(1f)
                            )
                            UpcomingBillsAndInsightsCard(
                                upcomingBills = upcomingBills,
                                expenseDonutSlices = expenseDonutSlices,
                                currentMonthIncome = currentMonthIncome,
                                currentMonthExpense = currentMonthExpense,
                                symbol = symbol,
                                onNavigateRecurring = { onNavigate(NavSection.RECURRING) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            BudgetProgressCard(
                                monthBudgets = monthBudgets,
                                currentMonthExpense = currentMonthExpense,
                                currentMonthTxs = currentMonthTxs,
                                symbol = symbol,
                                onNavigateBudgets = { onNavigate(NavSection.BUDGETS) }
                            )
                            UpcomingBillsAndInsightsCard(
                                upcomingBills = upcomingBills,
                                expenseDonutSlices = expenseDonutSlices,
                                currentMonthIncome = currentMonthIncome,
                                currentMonthExpense = currentMonthExpense,
                                symbol = symbol,
                                onNavigateRecurring = { onNavigate(NavSection.RECURRING) }
                            )
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onNavigate(NavSection.TRANSACTIONS) }) {
                            Text("All (${uiState.transactions.size}) →", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    AdaptiveTransactionsSection(
                        transactions = uiState.transactions.take(8),
                        currencySymbol = symbol,
                        categoryColorMap = categoryColorMap,
                        categoryIconMap = categoryIconMap,
                        onEdit = onEditTransaction,
                        onDuplicate = onDuplicateTransaction,
                        onDelete = onDeleteTransaction
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CashFlowChartCard(
    trendTab: String,
    onTrendTabChange: (String) -> Unit,
    useSmoothLine: Boolean,
    onToggleChartType: () -> Unit,
    trendPoints: List<ChartBarPoint>,
    symbol: String,
    onNavigateAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Cash Flow Chart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleChartType,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (useSmoothLine) Icons.Default.BarChart else Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Toggle Chart Style",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    TextButton(onClick = onNavigateAnalytics) {
                        Text("More →", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly").forEach { (key, label) ->
                    FilterChip(
                        selected = trendTab == key,
                        onClick = { onTrendTabChange(key) },
                        label = { Text(label) }
                    )
                }
            }

            if (useSmoothLine) {
                InteractiveLineChart(
                    points = trendPoints,
                    currencySymbol = symbol,
                    showIncomeSeries = true,
                    showExpenseSeries = true
                )
            } else {
                InteractiveBarChart(
                    points = trendPoints,
                    currencySymbol = symbol,
                    showIncomeSeries = true,
                    showExpenseSeries = true
                )
            }
        }
    }
}

@Composable
private fun ExpenseCategoryDonutCard(
    slices: List<DonutSlice>,
    symbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CrimsonDanger.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DonutLarge,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Expense Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            InteractiveDonutChart(
                slices = slices,
                currencySymbol = symbol
            )
        }
    }
}

@Composable
private fun BudgetProgressCard(
    monthBudgets: List<Budget>,
    currentMonthExpense: Long,
    currentMonthTxs: List<FinTransaction>,
    symbol: String,
    onNavigateBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onNavigateBudgets) {
                    Text("Manage →", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (monthBudgets.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No monthly budgets set yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                monthBudgets.take(4).forEach { bgt ->
                    val spent = if (bgt.categoryId == "ALL") {
                        currentMonthExpense
                    } else {
                        currentMonthTxs.filter { it.type == "EXPENSE" && it.categoryId == bgt.categoryId }
                            .sumOf { it.amountPaisa }
                    }
                    val remaining = bgt.limitPaisa - spent
                    val pct = if (bgt.limitPaisa > 0L) ((spent * 100.0) / bgt.limitPaisa).toInt() else 0
                    val animatedProgress by animateFloatAsState(
                        targetValue = (pct / 100f).coerceIn(0f, 1f),
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        label = "budget_bar"
                    )
                    val barColor = when {
                        pct >= 100 -> CrimsonDanger
                        pct >= 75 -> AmberWarning
                        else -> EmeraldSuccess
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bgt.categoryName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = barColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: ${MoneyUtils.formatPaisa(spent, symbol)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (remaining >= 0L) "Left: ${MoneyUtils.formatPaisa(remaining, symbol)}"
                                    else "Over: ${MoneyUtils.formatPaisa(-remaining, symbol)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = barColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingBillsAndInsightsCard(
    upcomingBills: List<RecurringTransaction>,
    expenseDonutSlices: List<DonutSlice>,
    currentMonthIncome: Long,
    currentMonthExpense: Long,
    symbol: String,
    onNavigateRecurring: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highestCat = expenseDonutSlices.firstOrNull()
    val today = DhakaDateUtils.todayEpochDay()
    val (_, _, dayOfMonth) = DhakaDateUtils.epochDayToYearMonthDay(today)
    val avgDailyExpense = if (dayOfMonth > 0) currentMonthExpense / dayOfMonth else currentMonthExpense
    val savingsRatePct = if (currentMonthIncome > 0L) {
        (((currentMonthIncome - currentMonthExpense) * 100.0) / currentMonthIncome).toInt()
    } else 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bills & Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = onNavigateRecurring) {
                    Text("Bills →", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (upcomingBills.isNotEmpty()) {
                upcomingBills.forEach { bill ->
                    val isInc = bill.type == "INCOME"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Due ${DhakaDateUtils.formatEpochDayShort(bill.nextDueDateEpochDay)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${if (isInc) "+" else "-"}${MoneyUtils.formatPaisa(bill.amountPaisa, symbol)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = if (isInc) EmeraldSuccess else CrimsonDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

            InsightRow(
                label = "Top Spend",
                value = highestCat?.let { "${it.label} (${MoneyUtils.formatPaisa(it.amountPaisa, symbol)})" } ?: "None"
            )
            InsightRow(
                label = "Daily Avg",
                value = MoneyUtils.formatPaisa(avgDailyExpense, symbol, showDecimals = true)
            )
            InsightRow(
                label = "Savings Rate",
                value = "$savingsRatePct%",
                valueColor = if (savingsRatePct >= 20) EmeraldSuccess else AmberWarning
            )
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Interactive Analytics Screen with Bar / Line / Donut chart switching.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    uiState: PersonalBmUiState,
    onPeriodChange: (String) -> Unit,
    onChartTypeChange: (String) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val period = uiState.analyticsPeriod
    val chartType = uiState.analyticsChartType
    val today = DhakaDateUtils.todayEpochDay()
    val (curYear, curMonth, _) = DhakaDateUtils.epochDayToYearMonthDay(today)

    val filteredPeriodTxs = remember(uiState.transactions, period, today) {
        when (period) {
            "DAILY" -> uiState.transactions.filter { it.dateEpochDay == today }
            "WEEKLY" -> uiState.transactions.filter { it.dateEpochDay in (today - 6L)..today }
            "MONTHLY" -> {
                val mk = DhakaDateUtils.currentMonthKey()
                uiState.transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == mk }
            }
            "YEARLY" -> uiState.transactions.filter {
                val (y, _, _) = DhakaDateUtils.epochDayToYearMonthDay(it.dateEpochDay)
                y == curYear
            }
            else -> uiState.transactions
        }
    }

    val periodIncome = filteredPeriodTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    val periodExpense = filteredPeriodTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    val periodNet = periodIncome - periodExpense
    val savingsPct = if (periodIncome > 0L) (((periodIncome - periodExpense) * 100.0) / periodIncome).toInt() else 0

    val trendPoints = remember(uiState.transactions, period) {
        buildTrendPoints(uiState.transactions, period)
    }

    val categoryColorMap = remember(uiState.categories) {
        uiState.categories.associate { it.id to it.colorHex }
    }

    val donutSlices = remember(filteredPeriodTxs, categoryColorMap) {
        filteredPeriodTxs.filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId to it.categoryName }
            .map { (pair, list) ->
                DonutSlice(
                    id = pair.first,
                    label = pair.second.ifBlank { "Other" },
                    amountPaisa = list.sumOf { it.amountPaisa },
                    color = parseHexColor(categoryColorMap[pair.first] ?: "#2563EB", RoyalBlue)
                )
            }
            .sortedByDescending { it.amountPaisa }
    }

    val yearlyMonthlyTotals = remember(uiState.transactions, curYear) {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        (1..12).map { m ->
            val exp = uiState.transactions.filter {
                it.type == "EXPENSE" && DhakaDateUtils.epochDayToYearMonthDay(it.dateEpochDay).let { (y, mo, _) -> y == curYear && mo == m }
            }.sumOf { it.amountPaisa }
            monthNames[m - 1] to exp
        }
    }
    val nonZeroMonths = yearlyMonthlyTotals.filter { it.second > 0L }
    val highestMonth = nonZeroMonths.maxByOrNull { it.second }
    val lowestMonth = nonZeroMonths.minByOrNull { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        listOf(
                            "DAILY" to "Daily",
                            "WEEKLY" to "Weekly",
                            "MONTHLY" to "Monthly ($curMonth/$curYear)",
                            "YEARLY" to "Yearly ($curYear)"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = period == key,
                                onClick = { onPeriodChange(key) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("analytics_period_$key")
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = chartType == "BAR",
                            onClick = { onChartTypeChange("BAR") },
                            label = { Text("Bars") },
                            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("chart_type_BAR")
                        )
                        FilterChip(
                            selected = chartType == "LINE",
                            onClick = { onChartTypeChange("LINE") },
                            label = { Text("Line") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("chart_type_LINE")
                        )
                        FilterChip(
                            selected = chartType == "DONUT",
                            onClick = { onChartTypeChange("DONUT") },
                            label = { Text("Donut") },
                            leadingIcon = { Icon(Icons.Default.DonutLarge, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("chart_type_DONUT")
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricCard(
                    title = "Income",
                    amountPaisa = periodIncome,
                    currencySymbol = symbol,
                    subtitle = "${filteredPeriodTxs.count { it.type == "INCOME" }} entries",
                    accentColor = EmeraldSuccess,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = "Expense",
                    amountPaisa = periodExpense,
                    currencySymbol = symbol,
                    subtitle = "${filteredPeriodTxs.count { it.type == "EXPENSE" }} entries",
                    accentColor = CrimsonDanger,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Income vs Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    when (chartType) {
                        "LINE" -> InteractiveLineChart(
                            points = trendPoints,
                            currencySymbol = symbol,
                            showIncomeSeries = true,
                            showExpenseSeries = true
                        )
                        "DONUT" -> InteractiveDonutChart(
                            slices = donutSlices,
                            currencySymbol = symbol
                        )
                        else -> InteractiveBarChart(
                            points = trendPoints,
                            currencySymbol = symbol,
                            showIncomeSeries = true,
                            showExpenseSeries = true
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Summary Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    InsightRow(
                        label = "Net Cash Flow",
                        value = MoneyUtils.formatPaisa(periodNet, symbol, showDecimals = true, showPlusSign = true),
                        valueColor = if (periodNet >= 0L) EmeraldSuccess else CrimsonDanger
                    )
                    InsightRow(
                        label = "Savings Rate",
                        value = "$savingsPct%",
                        valueColor = if (savingsPct >= 20) EmeraldSuccess else AmberWarning
                    )
                    if (period == "WEEKLY") {
                        InsightRow(
                            label = "7-Day Daily Avg",
                            value = MoneyUtils.formatPaisa(periodExpense / 7L, symbol, showDecimals = true)
                        )
                    }
                    if (period == "YEARLY") {
                        InsightRow(
                            label = "Highest Month",
                            value = highestMonth?.let { "${it.first} (${MoneyUtils.formatPaisa(it.second, symbol)})" } ?: "N/A",
                            valueColor = CrimsonDanger
                        )
                        InsightRow(
                            label = "Lowest Month",
                            value = lowestMonth?.let { "${it.first} (${MoneyUtils.formatPaisa(it.second, symbol)})" } ?: "N/A",
                            valueColor = EmeraldSuccess
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Top Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (donutSlices.isEmpty()) {
                        Text(
                            text = "No expenses in this period.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val totalExp = max(1L, donutSlices.sumOf { it.amountPaisa })
                        donutSlices.forEach { slice ->
                            val pct = ((slice.amountPaisa * 100.0) / totalExp).toInt()
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(slice.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = slice.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${MoneyUtils.formatPaisa(slice.amountPaisa, symbol, true)} ($pct%)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                                    color = slice.color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

/**
 * Builds Daily (last 10 days), Weekly (last 8 weeks), Monthly (12 months), and Yearly (5 years) chart points.
 */
fun buildTrendPoints(
    transactions: List<FinTransaction>,
    horizon: String
): List<ChartBarPoint> {
    val today = DhakaDateUtils.todayEpochDay()
    val (curYear, _, _) = DhakaDateUtils.epochDayToYearMonthDay(today)

    return when (horizon) {
        "DAILY" -> {
            (9 downTo 0).map { offset ->
                val day = today - offset
                val dayTxs = transactions.filter { it.dateEpochDay == day }
                ChartBarPoint(
                    label = DhakaDateUtils.formatEpochDayShort(day),
                    incomePaisa = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa },
                    expensePaisa = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
                )
            }
        }
        "WEEKLY" -> {
            (7 downTo 0).map { w ->
                val endDay = today - (w * 7L)
                val startDay = endDay - 6L
                val wkTxs = transactions.filter { it.dateEpochDay in startDay..endDay }
                ChartBarPoint(
                    label = DhakaDateUtils.formatEpochDayShort(startDay),
                    incomePaisa = wkTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa },
                    expensePaisa = wkTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
                )
            }
        }
        "MONTHLY" -> {
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            (1..12).map { m ->
                val mk = String.format(java.util.Locale.US, "%04d-%02d", curYear, m)
                val mTxs = transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == mk }
                ChartBarPoint(
                    label = monthNames[m - 1],
                    incomePaisa = mTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa },
                    expensePaisa = mTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
                )
            }
        }
        "YEARLY" -> {
            ((curYear - 4)..curYear).map { y ->
                val yTxs = transactions.filter { DhakaDateUtils.epochDayToYearMonthDay(it.dateEpochDay).first == y }
                ChartBarPoint(
                    label = "$y",
                    incomePaisa = yTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa },
                    expensePaisa = yTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
                )
            }
        }
        else -> emptyList()
    }
}

/**
 * Clean, Non-Overlapping Quick Entry Card for Expense, Allowance, Savings, and Life Events.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickFinancialEntryCard(
    accounts: List<Account>,
    categories: List<Category>,
    savingsGoals: List<SavingsGoal>,
    defaultAccountId: String,
    currencySymbol: String,
    currentMonthIncome: Long,
    currentMonthExpense: Long,
    currentMonthSavings: Long,
    currentMonthRemaining: Long,
    onRecordAllowance: (Long, String, String, String, Boolean, Long, String) -> Unit,
    onRecordExpense: (String, Long, String, String, String, String, String, Long, String) -> Unit,
    onRecordSavings: (String, Long, Long, String, String, String) -> Unit,
    onRecordEventExpense: (LifeEventType, String, List<EventBreakdownItem>, String, String, String, Long, String) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val safeAccounts = remember(accounts) {
        accounts.ifEmpty { DefaultSeedData.defaultAccounts("local_default_user").map { it.second } }
    }
    val safeCategories = remember(categories) {
        categories.ifEmpty { DefaultSeedData.defaultCategories("local_default_user").map { it.second } }
    }
    val expenseCategories = remember(safeCategories) {
        safeCategories.filter { it.type == "EXPENSE" && !it.id.startsWith("cat_event_") }
    }

    var activeTab by remember { mutableStateOf("EXPENSE") }
    var selectedAccountId by remember(safeAccounts, defaultAccountId) {
        mutableStateOf(
            safeAccounts.firstOrNull { it.id == defaultAccountId }?.id
                ?: safeAccounts.firstOrNull()?.id
                ?: "acc_cash"
        )
    }

    var quickDateEpochDay by remember { mutableLongStateOf(DhakaDateUtils.todayEpochDay()) }
    var quickTimeFormatted by remember { mutableStateOf(DhakaDateUtils.currentTimeFormatted()) }

    var expenseWhere by remember { mutableStateOf("") }
    var expenseAmountStr by remember { mutableStateOf("") }
    var selectedExpenseCategoryId by remember(expenseCategories) {
        mutableStateOf(expenseCategories.firstOrNull()?.id ?: "cat_exp_food")
    }

    var allowanceAmountStr by remember { mutableStateOf("") }
    var allowanceTitle by remember { mutableStateOf("Monthly Allowance") }
    var alsoSetMonthlyBudget by remember { mutableStateOf(true) }

    var savingsAmountStr by remember { mutableStateOf("") }
    var savingsGoalName by remember {
        mutableStateOf(savingsGoals.firstOrNull()?.name ?: "Monthly Savings")
    }
    var savingsSourceAccountId by remember(selectedAccountId) {
        mutableStateOf(selectedAccountId)
    }

    var formError by remember { mutableStateOf<String?>(null) }

    val spentAndSavedRatio = if (currentMonthIncome > 0L) {
        ((currentMonthExpense + currentMonthSavings).toFloat() / currentMonthIncome.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedOutcomeRatio by animateFloatAsState(
        targetValue = spentAndSavedRatio,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "monthly_outcome_progress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_financial_entry_card")
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
                Text(
                    text = "Quick Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Left: ${MoneyUtils.formatPaisa(currentMonthRemaining, currencySymbol, showPlusSign = true)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = if (currentMonthRemaining >= 0L) EmeraldSuccess else CrimsonDanger
                )
            }

            LinearProgressIndicator(
                progress = { animatedOutcomeRatio },
                color = if (currentMonthRemaining >= 0L) MaterialTheme.colorScheme.primary else CrimsonDanger,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
            )

            // 4 Clean Mode Tabs with Icons & Short Labels (Never overflows!)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Triple("EXPENSE", "Expense", CrimsonDanger to Icons.AutoMirrored.Filled.TrendingDown),
                        Triple("ALLOWANCE", "Income", EmeraldSuccess to Icons.AutoMirrored.Filled.TrendingUp),
                        Triple("SAVINGS", "Savings", RoyalBlue to Icons.Default.Savings),
                        Triple("EVENT", "Events", VibrantCyan to Icons.Default.Celebration)
                    )
                    tabs.forEach { (key, label, pair) ->
                        val (accent, tabIcon) = pair
                        val selected = activeTab == key
                        Surface(
                            color = if (selected) accent else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    activeTab = key
                                    formError = null
                                }
                                .testTag("quick_tab_$key")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = label,
                                    tint = if (selected) Color.White else accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (activeTab != "EVENT") {
                // Clean 2-Button Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val (y, m, d) = DhakaDateUtils.epochDayToYearMonthDay(quickDateEpochDay)
                            DatePickerDialog(
                                context,
                                { _, selY, selM, selD ->
                                    quickDateEpochDay = DhakaDateUtils.toEpochDay(selY, selM + 1, selD)
                                },
                                y,
                                m - 1,
                                d
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Date", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DhakaDateUtils.formatEpochDayShort(quickDateEpochDay),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val parts = quickTimeFormatted.split(":")
                            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
                            val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            TimePickerDialog(
                                context,
                                { _, selH, selM ->
                                    quickTimeFormatted = String.format("%02d:%02d", selH, selM)
                                },
                                hour,
                                min,
                                false
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DhakaDateUtils.formatTime12Hour(quickTimeFormatted),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(140))
                },
                label = "quick_entry_tab_anim"
            ) { tab ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (tab) {
                        "EXPENSE" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = expenseWhere,
                                    onValueChange = {
                                        expenseWhere = it
                                        formError = null
                                    },
                                    label = { Text("Where / Item") },
                                    placeholder = { Text("Lunch, Bus...") },
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("quick_expense_where_input")
                                )
                                OutlinedTextField(
                                    value = expenseAmountStr,
                                    onValueChange = {
                                        expenseAmountStr = it
                                        formError = null
                                    },
                                    label = { Text("Amount ($currencySymbol)") },
                                    placeholder = { Text("0") },
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_expense_amount_input")
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                expenseCategories.take(8).forEach { cat ->
                                    FilterChip(
                                        selected = selectedExpenseCategoryId == cat.id,
                                        onClick = { selectedExpenseCategoryId = cat.id },
                                        label = { Text(cat.name) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = iconForName(cat.iconName),
                                                contentDescription = null,
                                                tint = parseHexColor(cat.colorHex, CrimsonDanger),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                safeAccounts.forEach { acc ->
                                    FilterChip(
                                        selected = selectedAccountId == acc.id,
                                        onClick = { selectedAccountId = acc.id },
                                        label = { Text("${acc.name} (${MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol)})") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = iconForName(acc.iconName),
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val amtPaisa = MoneyUtils.parseTakaToPaisa(expenseAmountStr, allowZero = false)
                                    if (amtPaisa == null) {
                                        formError = "Enter a valid amount."
                                        return@Button
                                    }
                                    val cat = expenseCategories.firstOrNull { it.id == selectedExpenseCategoryId }
                                        ?: expenseCategories.firstOrNull()
                                    val acc = safeAccounts.firstOrNull { it.id == selectedAccountId } ?: safeAccounts.first()
                                    onRecordExpense(
                                        expenseWhere.trim().ifBlank { cat?.name ?: "Expense" },
                                        amtPaisa,
                                        cat?.id ?: "cat_exp_food",
                                        cat?.name ?: "Food & Dining",
                                        acc.id,
                                        acc.name,
                                        "",
                                        quickDateEpochDay,
                                        quickTimeFormatted
                                    )
                                    expenseWhere = ""
                                    expenseAmountStr = ""
                                    formError = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_submit_expense_button")
                            ) {
                                Text("Add Expense")
                            }
                        }

                        "ALLOWANCE" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = allowanceAmountStr,
                                    onValueChange = {
                                        allowanceAmountStr = it
                                        formError = null
                                    },
                                    label = { Text("Amount ($currencySymbol)") },
                                    placeholder = { Text("15000") },
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_allowance_amount_input")
                                )
                                OutlinedTextField(
                                    value = allowanceTitle,
                                    onValueChange = {
                                        allowanceTitle = it
                                        formError = null
                                    },
                                    label = { Text("Source") },
                                    placeholder = { Text("Allowance") },
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("quick_allowance_title_input")
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                safeAccounts.forEach { acc ->
                                    FilterChip(
                                        selected = selectedAccountId == acc.id,
                                        onClick = { selectedAccountId = acc.id },
                                        label = { Text("${acc.name} (${MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol)})") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = iconForName(acc.iconName),
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            FilterChip(
                                selected = alsoSetMonthlyBudget,
                                onClick = { alsoSetMonthlyBudget = !alsoSetMonthlyBudget },
                                label = { Text("Sync with Monthly Budget") }
                            )

                            Button(
                                onClick = {
                                    val amtPaisa = MoneyUtils.parseTakaToPaisa(allowanceAmountStr, allowZero = false)
                                    if (amtPaisa == null) {
                                        formError = "Enter a valid amount."
                                        return@Button
                                    }
                                    val acc = safeAccounts.firstOrNull { it.id == selectedAccountId } ?: safeAccounts.first()
                                    onRecordAllowance(
                                        amtPaisa,
                                        allowanceTitle.trim().ifBlank { "Monthly Allowance" },
                                        acc.id,
                                        acc.name,
                                        alsoSetMonthlyBudget,
                                        quickDateEpochDay,
                                        quickTimeFormatted
                                    )
                                    allowanceAmountStr = ""
                                    formError = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_submit_allowance_button")
                            ) {
                                Text("Add Allowance / Income")
                            }
                        }

                        "SAVINGS" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = savingsAmountStr,
                                    onValueChange = {
                                        savingsAmountStr = it
                                        formError = null
                                    },
                                    label = { Text("Amount ($currencySymbol)") },
                                    placeholder = { Text("2000") },
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_savings_amount_input")
                                )
                                OutlinedTextField(
                                    value = savingsGoalName,
                                    onValueChange = {
                                        savingsGoalName = it
                                        formError = null
                                    },
                                    label = { Text("Goal") },
                                    placeholder = { Text("Emergency Fund") },
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("quick_savings_goal_input")
                                )
                            }

                            if (savingsGoals.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    savingsGoals.forEach { g ->
                                        FilterChip(
                                            selected = savingsGoalName.equals(g.name, ignoreCase = true),
                                            onClick = { savingsGoalName = g.name },
                                            label = { Text("${g.name} (${MoneyUtils.formatPaisa(g.currentSavedPaisa, currencySymbol)})") }
                                        )
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = savingsSourceAccountId.isBlank(),
                                    onClick = { savingsSourceAccountId = "" },
                                    label = { Text("External") }
                                )
                                safeAccounts.forEach { acc ->
                                    FilterChip(
                                        selected = savingsSourceAccountId == acc.id,
                                        onClick = { savingsSourceAccountId = acc.id },
                                        label = { Text(acc.name) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val amtPaisa = MoneyUtils.parseTakaToPaisa(savingsAmountStr, allowZero = false)
                                    if (amtPaisa == null) {
                                        formError = "Enter a valid amount."
                                        return@Button
                                    }
                                    val acc = safeAccounts.firstOrNull { it.id == savingsSourceAccountId }
                                    onRecordSavings(
                                        savingsGoalName.trim().ifBlank { "Monthly Savings" },
                                        amtPaisa,
                                        amtPaisa * 6L,
                                        acc?.id ?: "",
                                        acc?.name ?: "",
                                        "Quick Savings"
                                    )
                                    savingsAmountStr = ""
                                    formError = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_submit_savings_button")
                            ) {
                                Text("Add Savings")
                            }
                        }

                        "EVENT" -> {
                            EventExpenseCreatorForm(
                                accounts = safeAccounts,
                                currencySymbol = currencySymbol,
                                defaultDateEpochDay = quickDateEpochDay,
                                defaultTimeFormatted = quickTimeFormatted,
                                onRecordEventExpense = onRecordEventExpense
                            )
                        }
                    }
                }
            }

            if (formError != null) {
                Text(
                    text = formError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonDanger
                )
            }
        }
    }
}
