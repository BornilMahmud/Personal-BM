package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Budget
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.MoneyUtils
import com.example.data.model.NavSection
import com.example.data.model.RecurringTransaction
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
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VibrantCyan
import com.example.ui.viewmodel.PersonalBmUiState
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewScreen(
    uiState: PersonalBmUiState,
    onAddTransactionClick: () -> Unit,
    onAddExpenseClick: () -> Unit = onAddTransactionClick,
    onAddIncomeClick: () -> Unit = onAddTransactionClick,
    onTransferMoneyClick: () -> Unit = onAddTransactionClick,
    onEditTransaction: (FinTransaction) -> Unit,
    onDuplicateTransaction: (FinTransaction) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit,
    onNavigate: (NavSection) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val lang = uiState.settings.language
    val currentMonthKey = uiState.selectedMonthKey.ifBlank { DhakaDateUtils.currentMonthKey() }
    val prevMonthKey = DhakaDateUtils.previousMonthKey(currentMonthKey)

    // Total Balance across all accounts
    val totalBalancePaisa = remember(uiState.accounts) {
        uiState.accounts.sumOf { it.currentBalancePaisa }
    }
    // Total Income & Expenses (excluding transfers so transfers are never double-counted)
    val totalIncomePaisa = remember(uiState.transactions) {
        uiState.transactions.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    }
    val totalExpensePaisa = remember(uiState.transactions) {
        uiState.transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    }
    val totalSavingsGoalsPaisa = remember(uiState.savingsGoals) {
        uiState.savingsGoals.sumOf { it.currentSavedPaisa }
    }
    val netSavingsPaisa = totalSavingsGoalsPaisa + max(0L, totalIncomePaisa - totalExpensePaisa)

    // Current vs Previous Month Comparison
    val currentMonthTxs = remember(uiState.transactions, currentMonthKey) {
        uiState.transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey }
    }
    val prevMonthTxs = remember(uiState.transactions, prevMonthKey) {
        uiState.transactions.filter { DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == prevMonthKey }
    }

    val currentMonthIncome = currentMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa }
    val currentMonthExpense = currentMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    val currentMonthNet = currentMonthIncome - currentMonthExpense

    val prevMonthNet = prevMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amountPaisa } -
        prevMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
    val momChangePct = if (prevMonthNet != 0L) {
        (((currentMonthNet - prevMonthNet) * 100.0) / kotlin.math.abs(prevMonthNet)).toInt()
    } else if (currentMonthIncome > 0L) {
        (((currentMonthIncome - currentMonthExpense) * 100.0) / currentMonthIncome).toInt()
    } else 0

    var trendTab by remember { mutableStateOf("DAILY") } // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    var useSmoothLine by remember { mutableStateOf(true) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Column {
                        Text(
                            text = if (lang == "bn") "আর্থিক ড্যাশবোর্ড" else "Financial Overview",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${DhakaDateUtils.formatMonthKeyDisplay(currentMonthKey)} • Asia/Dhaka • BDT ($symbol)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onAddTransactionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_add_tx_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (lang == "bn") "নতুন লেনদেন" else "Add Entry")
                    }
                }
            }

            // Hero Balance & Summary Metric Cards (Adaptive 12-Col on Desktop vs Mobile Stack)
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
                                currentMonthNetPaisa = currentMonthNet,
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
                                        title = if (lang == "bn") "মোট আয়" else "Total Income",
                                        amountPaisa = totalIncomePaisa,
                                        currencySymbol = symbol,
                                        subtitle = "Month: ${MoneyUtils.formatPaisa(currentMonthIncome, symbol)}",
                                        accentColor = EmeraldSuccess,
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryMetricCard(
                                        title = if (lang == "bn") "মোট ব্যয়" else "Total Expenses",
                                        amountPaisa = totalExpensePaisa,
                                        currencySymbol = symbol,
                                        subtitle = "Month: ${MoneyUtils.formatPaisa(currentMonthExpense, symbol)}",
                                        accentColor = CrimsonDanger,
                                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                SummaryMetricCard(
                                    title = if (lang == "bn") "মোট সঞ্চয়" else "Total Savings & Reserve",
                                    amountPaisa = netSavingsPaisa,
                                    currencySymbol = symbol,
                                    subtitle = "${uiState.savingsGoals.size} active savings goals",
                                    accentColor = RoyalBlue,
                                    icon = Icons.Default.Savings,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Mobile-First Sequence:
                        // 1. Large Total Balance Card
                        HeroTotalBalanceCard(
                            totalBalancePaisa = totalBalancePaisa,
                            currentMonthNetPaisa = currentMonthNet,
                            momChangePct = momChangePct,
                            accounts = uiState.accounts,
                            sparklineValues = sparklineValues,
                            currencySymbol = symbol,
                            language = lang,
                            onWalletsClick = { onNavigate(NavSection.ACCOUNTS) }
                        )
                        // 2. Two-Column Income and Expense Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryMetricCard(
                                title = if (lang == "bn") "মোট আয়" else "Total Income",
                                amountPaisa = totalIncomePaisa,
                                currencySymbol = symbol,
                                subtitle = "Month: ${MoneyUtils.formatPaisa(currentMonthIncome, symbol)}",
                                accentColor = EmeraldSuccess,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricCard(
                                title = if (lang == "bn") "মোট ব্যয়" else "Total Expenses",
                                amountPaisa = totalExpensePaisa,
                                currencySymbol = symbol,
                                subtitle = "Month: ${MoneyUtils.formatPaisa(currentMonthExpense, symbol)}",
                                accentColor = CrimsonDanger,
                                icon = Icons.AutoMirrored.Filled.TrendingDown,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 3. Savings Summary
                        SummaryMetricCard(
                            title = if (lang == "bn") "মোট সঞ্চয়" else "Total Savings",
                            amountPaisa = netSavingsPaisa,
                            currencySymbol = symbol,
                            subtitle = "${uiState.savingsGoals.size} savings goals • Net month flow ${MoneyUtils.formatPaisa(currentMonthNet, symbol, showPlusSign = true)}",
                            accentColor = RoyalBlue,
                            icon = Icons.Default.Savings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. Quick Actions Strip (Add Expense, Add Income, Transfer Money, View Reports)
                    QuickActionsStrip(
                        onAddExpense = onAddExpenseClick,
                        onAddIncome = onAddIncomeClick,
                        onTransferMoney = onTransferMoneyClick,
                        onViewReports = { onNavigate(NavSection.REPORTS) }
                    )
                }
            }

            // Empty State when no transactions exist
            if (uiState.transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                        EmptyStateCard(
                            title = "Your Personal BM Ledger is Ready",
                            subtitle = "Your Bangladeshi accounts (Cash, Bank, bKash, Nagad, Rocket, Upay) and categories are pre-loaded. Record your first income, expense, or transfer to unlock real-time charts and insights.",
                            actionLabel = "Add First Transaction",
                            onAction = onAddTransactionClick
                        )
                    }
                }
            }

            // Cash Flow Chart & Expense Categories Donut Chart (Side-by-side on Desktop, Stacked on Mobile)
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
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

            // Budget Progress, Upcoming Bills & Financial Insights (Side-by-side on Desktop, Stacked on Mobile)
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
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

            // Recent Transactions Header + Adaptive Table / Cards
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
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = { onNavigate(NavSection.TRANSACTIONS) }) {
                            Text("View All (${uiState.transactions.size}) →", color = RoyalBlue)
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
                Column {
                    Text(
                        text = "Cash Flow & Spending Trend",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Income vs Expense comparison",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleChartType() }
                    ) {
                        Text(
                            text = if (useSmoothLine) "Area Line" else "Bars",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoyalBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    TextButton(onClick = onNavigateAnalytics) {
                        Text("Analytics →", color = RoyalBlue)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            Text(
                text = "Expense Categories",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Category legend & percentage breakdown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                Column {
                    Text(
                        text = "Budget Progress",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Monthly spending limits & remaining balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onNavigateBudgets) {
                    Text("Manage →", color = RoyalBlue)
                }
            }

            if (monthBudgets.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "No monthly budgets configured yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Set category limits to track utilization and receive 75%, 90%, and 100% alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                    color = MaterialTheme.colorScheme.onSurface
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
                                    .height(7.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventRepeat, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upcoming Bills & Insights",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = onNavigateRecurring) {
                    Text("Bills →", color = RoyalBlue)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Due ${DhakaDateUtils.formatEpochDayShort(bill.nextDueDateEpochDay)} • ${bill.frequency}",
                                style = MaterialTheme.typography.bodySmall,
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
            } else {
                Text(
                    text = "No scheduled recurring bills due soon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = VibrantCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Financial Insights",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            InsightRow(
                label = "Highest Spending Category",
                value = highestCat?.let { "${it.label} (${MoneyUtils.formatPaisa(it.amountPaisa, symbol)})" } ?: "None yet"
            )
            InsightRow(
                label = "Average Daily Expenditure",
                value = MoneyUtils.formatPaisa(avgDailyExpense, symbol, showDecimals = true)
            )
            InsightRow(
                label = "Monthly Savings Rate",
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Section 9: Interactive Analytics Dashboard with 4 time periods (DAILY, WEEKLY, MONTHLY, YEARLY)
 * and Line / Bar / Donut chart switching.
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Interactive Analytics & Charts",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Real-time calculations across Daily, Weekly, Monthly, and Yearly horizons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "Time Horizon",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                    ) {
                        listOf(
                            "DAILY" to "Daily (Today)",
                            "WEEKLY" to "Weekly (7D)",
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

                    Text(
                        text = "Chart Visualization",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        FilterChip(
                            selected = chartType == "BAR",
                            onClick = { onChartTypeChange("BAR") },
                            label = { Text("Bar Chart") },
                            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("chart_type_BAR")
                        )
                        FilterChip(
                            selected = chartType == "LINE",
                            onClick = { onChartTypeChange("LINE") },
                            label = { Text("Line Chart") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("chart_type_LINE")
                        )
                        FilterChip(
                            selected = chartType == "DONUT",
                            onClick = { onChartTypeChange("DONUT") },
                            label = { Text("Donut Chart") },
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
                    title = "$period Income",
                    amountPaisa = periodIncome,
                    currencySymbol = symbol,
                    subtitle = "${filteredPeriodTxs.count { it.type == "INCOME" }} entries",
                    accentColor = EmeraldSuccess,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = "$period Expense",
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
                        text = "$period Income vs Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                        text = "$period Key Metrics",
                        style = MaterialTheme.typography.titleLarge,
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
                            label = "7-Day Average Daily Spending",
                            value = MoneyUtils.formatPaisa(periodExpense / 7L, symbol, showDecimals = true)
                        )
                    }
                    if (period == "YEARLY") {
                        InsightRow(
                            label = "Leap Year Status ($curYear)",
                            value = if (DhakaDateUtils.isLeapYear(curYear)) "Leap Year (366 days)" else "Standard Year (365 days)"
                        )
                        InsightRow(
                            label = "Highest Spending Month",
                            value = highestMonth?.let { "${it.first} (${MoneyUtils.formatPaisa(it.second, symbol)})" } ?: "N/A",
                            valueColor = CrimsonDanger
                        )
                        InsightRow(
                            label = "Lowest Spending Month",
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
                        text = "Expense Categories Breakdown",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (donutSlices.isEmpty()) {
                        Text(
                            text = "No expenses recorded in this period.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val totalExp = max(1L, donutSlices.sumOf { it.amountPaisa })
                        donutSlices.forEach { slice ->
                            val pct = ((slice.amountPaisa * 100.0) / totalExp).toInt()
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            fontWeight = FontWeight.Medium
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
 * Builds accurate Daily (last 14 days), Weekly (last 8 weeks), Monthly (12 months of year),
 * and Yearly (last 5 years) chart points in Asia/Dhaka time.
 */
fun buildTrendPoints(
    transactions: List<FinTransaction>,
    horizon: String
): List<ChartBarPoint> {
    val today = DhakaDateUtils.todayEpochDay()
    val (curYear, _, _) = DhakaDateUtils.epochDayToYearMonthDay(today)

    return when (horizon) {
        "DAILY" -> {
            (13 downTo 0).map { offset ->
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
