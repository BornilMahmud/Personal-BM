package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.MoneyUtils
import com.example.data.model.parseHexColor
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueSoft
import com.example.ui.theme.VibrantCyan
import kotlin.math.roundToLong

fun iconForName(name: String): ImageVector = when (name) {
    "payments" -> Icons.Default.Payments
    "account_balance" -> Icons.Default.AccountBalance
    "smartphone", "phone_android" -> Icons.Default.PhoneAndroid
    "wallet" -> Icons.Default.AccountBalanceWallet
    "work" -> Icons.Default.Work
    "laptop" -> Icons.Default.Laptop
    "store" -> Icons.Default.Store
    "movie", "ConfirmationNumber" -> Icons.Default.Movie
    "code" -> Icons.Default.Code
    "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
    "redeem" -> Icons.Default.CardGiftcard
    "restaurant" -> Icons.Default.Restaurant
    "directions_bus" -> Icons.Default.DirectionsBus
    "shopping_bag" -> Icons.Default.ShoppingBag
    "school" -> Icons.Default.School
    "home" -> Icons.Default.Home
    "apartment" -> Icons.Default.Apartment
    "bolt" -> Icons.Default.Bolt
    "wifi" -> Icons.Default.Wifi
    "medical_services" -> Icons.Default.MedicalServices
    "flight" -> Icons.Default.Flight
    "subscriptions" -> Icons.Default.Subscriptions
    "spa" -> Icons.Default.Spa
    "family_restroom" -> Icons.Default.FamilyRestroom
    "devices" -> Icons.Default.Devices
    "savings" -> Icons.Default.Savings
    "more_horiz" -> Icons.Default.MoreHoriz
    else -> Icons.Default.Category
}

/**
 * Animated financial counter with tabular monospace numerals.
 */
@Composable
fun AnimatedMoneyText(
    amountPaisa: Long,
    currencySymbol: String,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showDecimals: Boolean = true,
    showPlusSign: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedFloat by animateFloatAsState(
        targetValue = amountPaisa.toFloat(),
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "money_counter"
    )
    val displayPaisa = animatedFloat.roundToLong()
    Text(
        text = MoneyUtils.formatPaisa(displayPaisa, currencySymbol, showDecimals = showDecimals, showPlusSign = showPlusSign),
        style = style.copy(fontFamily = JetBrainsMonoFontFamily),
        fontWeight = FontWeight.Bold,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Confirmed Minimal Light Total Balance Hero Card:
 * Elevated white card (#FFFFFF) with subtle blue sparkline, 1dp #E2E8F0 border,
 * month-over-month comparison pill, and active account summary chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroTotalBalanceCard(
    totalBalancePaisa: Long,
    currentMonthNetPaisa: Long,
    momChangePct: Int,
    accounts: List<Account>,
    sparklineValues: List<Long>,
    currencySymbol: String,
    language: String,
    onWalletsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_total_balance_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RoyalBlueSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "bn") "মোট ব্যালেন্স (নেট ওয়ার্থ)" else "Total Balance",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    AnimatedMoneyText(
                        amountPaisa = totalBalancePaisa,
                        currencySymbol = currencySymbol,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        showDecimals = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isPositive = currentMonthNetPaisa >= 0L
                        Surface(
                            color = if (isPositive) EmeraldSoft else CrimsonSoft,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            val sign = if (momChangePct > 0) "+" else ""
                            Text(
                                text = "${sign}$momChangePct% vs last month",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isPositive) EmeraldSuccess else CrimsonDanger,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Net: ${MoneyUtils.formatPaisa(currentMonthNetPaisa, currencySymbol, showPlusSign = true)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Subtle Decorative Blue Sparkline
                MiniBalanceSparkline(
                    values = sparklineValues,
                    lineColor = RoyalBlue,
                    modifier = Modifier
                        .width(120.dp)
                        .height(64.dp)
                        .padding(start = 8.dp)
                )
            }

            if (accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${accounts.size} Linked Wallets & Accounts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Manage Wallets →",
                        style = MaterialTheme.typography.labelMedium,
                        color = RoyalBlue,
                        modifier = Modifier.clickable { onWalletsClick() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.take(5).forEach { acc ->
                        val accColor = parseHexColor(acc.colorHex, RoyalBlue)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(accColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mobile & Dashboard Quick Actions Strip:
 * Add Expense | Add Income | Transfer Money | View Reports
 */
@Composable
fun QuickActionsStrip(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onTransferMoney: () -> Unit,
    onViewReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            label = "Add Expense",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            accentColor = CrimsonDanger,
            softBg = CrimsonSoft,
            onClick = onAddExpense,
            testTag = "quick_action_expense",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Add Income",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = EmeraldSuccess,
            softBg = EmeraldSoft,
            onClick = onAddIncome,
            testTag = "quick_action_income",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Transfer",
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            accentColor = RoyalBlue,
            softBg = RoyalBlueSoft,
            onClick = onTransferMoney,
            testTag = "quick_action_transfer",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Reports",
            icon = Icons.Default.Assessment,
            accentColor = VibrantCyan,
            softBg = RoyalBlueSoft,
            onClick = onViewReports,
            testTag = "quick_action_reports",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    softBg: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(softBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    amountPaisa: Long,
    currencySymbol: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedMoneyText(
                amountPaisa = amountPaisa,
                currencySymbol = currencySymbol,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                showDecimals = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Adaptive Transaction List:
 * Renders a structured multi-column fintech table on desktop/tablet widths (>= 680.dp)
 * and compact touch-friendly TransactionRowItem cards on mobile (< 680.dp).
 */
@Composable
fun AdaptiveTransactionsSection(
    transactions: List<FinTransaction>,
    currencySymbol: String,
    categoryColorMap: Map<String, String>,
    categoryIconMap: Map<String, String>,
    onEdit: (FinTransaction) -> Unit,
    onDuplicate: (FinTransaction) -> Unit,
    onDelete: (FinTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val useDesktopTable = maxWidth >= 680.dp
        if (useDesktopTable && transactions.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSACTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(2.2f)
                        )
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = "ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = "DATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = "AMOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.3f)
                        )
                        Spacer(modifier = Modifier.width(96.dp))
                    }

                    transactions.forEachIndexed { index, tx ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        }
                        DesktopTransactionTableRow(
                            tx = tx,
                            currencySymbol = currencySymbol,
                            categoryColorHex = categoryColorMap[tx.categoryId] ?: "#2563EB",
                            categoryIconName = categoryIconMap[tx.categoryId] ?: "category",
                            onEdit = { onEdit(tx) },
                            onDuplicate = { onDuplicate(tx) },
                            onDelete = { onDelete(tx) }
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.forEach { tx ->
                    TransactionRowItem(
                        tx = tx,
                        currencySymbol = currencySymbol,
                        categoryColorHex = categoryColorMap[tx.categoryId] ?: "#2563EB",
                        categoryIconName = categoryIconMap[tx.categoryId] ?: "category",
                        onEdit = { onEdit(tx) },
                        onDuplicate = { onDuplicate(tx) },
                        onDelete = { onDelete(tx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopTransactionTableRow(
    tx: FinTransaction,
    currencySymbol: String,
    categoryColorHex: String,
    categoryIconName: String,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = when (tx.type) {
        "INCOME" -> EmeraldSuccess
        "TRANSFER" -> RoyalBlue
        else -> parseHexColor(categoryColorHex, CrimsonDanger)
    }
    val amountColor = when (tx.type) {
        "INCOME" -> EmeraldSuccess
        "TRANSFER" -> RoyalBlue
        else -> CrimsonDanger
    }
    val signPrefix = when (tx.type) {
        "INCOME" -> "+"
        "EXPENSE" -> "-"
        else -> "⇄ "
    }
    val iconVec = when (tx.type) {
        "INCOME" -> Icons.AutoMirrored.Filled.TrendingUp
        "TRANSFER" -> Icons.AutoMirrored.Filled.CompareArrows
        else -> iconForName(categoryIconName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("tx_item_${tx.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(2.2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = tx.categoryName,
                    tint = badgeColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tx.notes.isNotBlank()) {
                    Text(
                        text = tx.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1.3f)) {
            Surface(
                color = badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = tx.categoryName.ifBlank { tx.type },
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Text(
            text = if (tx.type == "TRANSFER") "${tx.accountName} → ${tx.toAccountName}" else tx.accountName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.3f)
        )

        Text(
            text = "${DhakaDateUtils.formatEpochDayShort(tx.dateEpochDay)} ${tx.timeFormatted}",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1.1f)
        )

        Text(
            text = "$signPrefix${MoneyUtils.formatPaisa(tx.amountPaisa, currencySymbol, showDecimals = true)}",
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
            fontWeight = FontWeight.Bold,
            color = amountColor,
            maxLines = 1,
            modifier = Modifier.weight(1.3f)
        )

        Row(
            modifier = Modifier.width(96.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(30.dp)
                    .testTag("edit_tx_${tx.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit transaction",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier
                    .size(30.dp)
                    .testTag("duplicate_tx_${tx.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate transaction",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(30.dp)
                    .testTag("delete_tx_${tx.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete transaction",
                    tint = CrimsonDanger,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    tx: FinTransaction,
    currencySymbol: String,
    categoryColorHex: String = "#2563EB",
    categoryIconName: String = "category",
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (tx.type) {
        "INCOME" -> EmeraldSuccess
        "TRANSFER" -> RoyalBlue
        else -> parseHexColor(categoryColorHex, CrimsonDanger)
    }
    val amountColor = when (tx.type) {
        "INCOME" -> EmeraldSuccess
        "TRANSFER" -> RoyalBlue
        else -> CrimsonDanger
    }
    val signPrefix = when (tx.type) {
        "INCOME" -> "+"
        "EXPENSE" -> "-"
        else -> "⇄ "
    }
    val iconVec = when (tx.type) {
        "INCOME" -> Icons.AutoMirrored.Filled.TrendingUp
        "TRANSFER" -> Icons.AutoMirrored.Filled.CompareArrows
        else -> iconForName(categoryIconName)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("tx_item_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = tx.categoryName,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val accountSummary = if (tx.type == "TRANSFER") {
                    "${tx.accountName} → ${tx.toAccountName}"
                } else {
                    "${tx.categoryName} • ${tx.accountName}"
                }
                Text(
                    text = "$accountSummary • ${DhakaDateUtils.formatEpochDayShort(tx.dateEpochDay)} ${tx.timeFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tx.notes.isNotBlank()) {
                    Text(
                        text = tx.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$signPrefix${MoneyUtils.formatPaisa(tx.amountPaisa, currencySymbol, showDecimals = true)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("edit_tx_${tx.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit transaction",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("duplicate_tx_${tx.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate transaction",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_tx_${tx.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete transaction",
                            tint = CrimsonDanger,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoyalBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = title,
                    tint = RoyalBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("empty_state_action_button")
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Validated Add / Edit Transaction Modal Bottom Sheet supporting Income, Expense, and Transfer.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionModal(
    initialTransaction: FinTransaction? = null,
    defaultType: String = "EXPENSE",
    accounts: List<Account>,
    categories: List<Category>,
    defaultAccountId: String,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onSave: (FinTransaction, Boolean) -> Unit
) {
    val context = LocalContext.current
    val isEdit = initialTransaction != null && initialTransaction.id.isNotBlank()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var txType by remember { mutableStateOf(initialTransaction?.type ?: defaultType) }
    var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
    var amountInput by remember {
        mutableStateOf(
            if (initialTransaction != null && initialTransaction.amountPaisa > 0L) {
                MoneyUtils.paisaToInputString(initialTransaction.amountPaisa)
            } else ""
        )
    }

    val filteredCategories = remember(categories, txType) {
        categories.filter { it.type == txType }
    }
    var selectedCategoryId by remember(txType, categories) {
        mutableStateOf(
            initialTransaction?.categoryId?.takeIf { it.isNotBlank() }
                ?: filteredCategories.firstOrNull()?.id
                ?: "cat_exp_food"
        )
    }

    var selectedAccountId by remember(accounts) {
        mutableStateOf(
            initialTransaction?.accountId?.takeIf { it.isNotBlank() }
                ?: accounts.firstOrNull { it.id == defaultAccountId }?.id
                ?: accounts.firstOrNull()?.id
                ?: "acc_cash"
        )
    }

    var selectedToAccountId by remember(accounts, selectedAccountId) {
        mutableStateOf(
            initialTransaction?.toAccountId?.takeIf { it.isNotBlank() }
                ?: accounts.firstOrNull { it.id != selectedAccountId }?.id
                ?: ""
        )
    }

    var dateEpochDay by remember {
        mutableLongStateOf(initialTransaction?.dateEpochDay ?: DhakaDateUtils.todayEpochDay())
    }
    var timeFormatted by remember {
        mutableStateOf(initialTransaction?.timeFormatted ?: DhakaDateUtils.currentTimeFormatted())
    }
    var paymentMethod by remember {
        mutableStateOf(initialTransaction?.paymentMethod ?: "Cash")
    }
    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }
    var attachmentUri by remember { mutableStateOf(initialTransaction?.attachmentUri ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachmentUri = uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Transaction" else "New Transaction",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction Type Selector (EXPENSE / INCOME / TRANSFER)
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
                    val types = listOf(
                        Triple("EXPENSE", "Expense", CrimsonDanger),
                        Triple("INCOME", "Income", EmeraldSuccess),
                        Triple("TRANSFER", "Transfer", RoyalBlue)
                    )
                    types.forEach { (typeKey, label, color) ->
                        val selected = txType == typeKey
                        Surface(
                            color = if (selected) color else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { txType = typeKey }
                                .testTag("tx_type_chip_$typeKey")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount & Title
            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it
                    validationError = null
                },
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("e.g. 1250.00") },
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_amount_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Transaction Title") },
                placeholder = {
                    Text(
                        when (txType) {
                            "INCOME" -> "e.g. Monthly Salary / Freelance Project"
                            "TRANSFER" -> "e.g. Bank to bKash Cash-In"
                            else -> "e.g. Grocery Shopping at Shwapno"
                        }
                    )
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Account Selector
            Text(
                text = if (txType == "TRANSFER") "From Account" else "Account / Wallet",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                accounts.forEach { acc ->
                    val selected = acc.id == selectedAccountId
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedAccountId = acc.id
                            paymentMethod = acc.name
                            if (txType == "TRANSFER" && selectedToAccountId == acc.id) {
                                selectedToAccountId = accounts.firstOrNull { it.id != acc.id }?.id ?: ""
                            }
                        },
                        label = {
                            Text("${acc.name} (${MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol)})")
                        },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            if (txType == "TRANSFER") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "To Destination Account",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                        val selected = acc.id == selectedToAccountId
                        FilterChip(
                            selected = selected,
                            onClick = { selectedToAccountId = acc.id },
                            label = {
                                Text("${acc.name} (${MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol)})")
                            },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val selected = cat.id == selectedCategoryId
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = parseHexColor(cat.colorHex, RoyalBlue).copy(alpha = 0.16f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val (y, m, d) = DhakaDateUtils.epochDayToYearMonthDay(dateEpochDay)
                        DatePickerDialog(
                            context,
                            { _, selY, selM, selD ->
                                dateEpochDay = DhakaDateUtils.toEpochDay(selY, selM + 1, selD)
                            },
                            y,
                            m - 1,
                            d
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Date", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(DhakaDateUtils.formatEpochDay(dateEpochDay))
                }

                OutlinedButton(
                    onClick = {
                        val parts = timeFormatted.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
                        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        TimePickerDialog(
                            context,
                            { _, selH, selM ->
                                timeFormatted = String.format("%02d:%02d", selH, selM)
                            },
                            hour,
                            min,
                            true
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(timeFormatted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Description / Notes (Optional)") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_notes_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach Receipt", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (attachmentUri.isBlank()) "Attach Receipt" else "Change Receipt")
                }
                if (attachmentUri.isNotBlank()) {
                    TextButton(onClick = { attachmentUri = "" }) {
                        Text("Remove Attachment", color = CrimsonDanger)
                    }
                }
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonDanger
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val amountPaisa = MoneyUtils.parseTakaToPaisa(amountInput, allowZero = false)
                    if (amountPaisa == null) {
                        validationError = "Please enter a valid positive amount."
                        return@Button
                    }
                    val trimmedTitle = title.trim().ifBlank {
                        when (txType) {
                            "TRANSFER" -> "Account Transfer"
                            else -> filteredCategories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Transaction"
                        }
                    }
                    val fromAcc = accounts.firstOrNull { it.id == selectedAccountId }
                    if (fromAcc == null) {
                        validationError = "Please select an account."
                        return@Button
                    }
                    if (txType == "TRANSFER") {
                        val toAcc = accounts.firstOrNull { it.id == selectedToAccountId }
                        if (toAcc == null || toAcc.id == fromAcc.id) {
                            validationError = "Please select a different destination account for the transfer."
                            return@Button
                        }
                        val tx = FinTransaction(
                            id = initialTransaction?.id ?: "",
                            userId = initialTransaction?.userId ?: "",
                            title = trimmedTitle.take(160),
                            amountPaisa = amountPaisa,
                            type = "TRANSFER",
                            categoryId = "transfer",
                            categoryName = "Transfer",
                            accountId = fromAcc.id,
                            accountName = fromAcc.name,
                            toAccountId = toAcc.id,
                            toAccountName = toAcc.name,
                            dateEpochDay = dateEpochDay,
                            timeFormatted = timeFormatted,
                            timestampMillis = initialTransaction?.timestampMillis?.takeIf { it > 0L } ?: System.currentTimeMillis(),
                            paymentMethod = fromAcc.name,
                            notes = notes.trim().take(1000),
                            attachmentUri = attachmentUri.take(1000),
                            recurringRuleId = initialTransaction?.recurringRuleId ?: "",
                            createdAt = initialTransaction?.createdAt,
                            updatedAt = null
                        )
                        onSave(tx, isEdit)
                        onDismiss()
                    } else {
                        val cat = filteredCategories.firstOrNull { it.id == selectedCategoryId }
                            ?: filteredCategories.firstOrNull()
                        val tx = FinTransaction(
                            id = initialTransaction?.id ?: "",
                            userId = initialTransaction?.userId ?: "",
                            title = trimmedTitle.take(160),
                            amountPaisa = amountPaisa,
                            type = txType,
                            categoryId = cat?.id ?: "cat_other",
                            categoryName = cat?.name ?: "Other",
                            accountId = fromAcc.id,
                            accountName = fromAcc.name,
                            toAccountId = "",
                            toAccountName = "",
                            dateEpochDay = dateEpochDay,
                            timeFormatted = timeFormatted,
                            timestampMillis = initialTransaction?.timestampMillis?.takeIf { it > 0L } ?: System.currentTimeMillis(),
                            paymentMethod = paymentMethod.ifBlank { fromAcc.name }.take(64),
                            notes = notes.trim().take(1000),
                            attachmentUri = attachmentUri.take(1000),
                            recurringRuleId = initialTransaction?.recurringRuleId ?: "",
                            createdAt = initialTransaction?.createdAt,
                            updatedAt = null
                        )
                        onSave(tx, isEdit)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_transaction_button")
            ) {
                Text(if (isEdit) "Update Transaction" else "Save Transaction", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
