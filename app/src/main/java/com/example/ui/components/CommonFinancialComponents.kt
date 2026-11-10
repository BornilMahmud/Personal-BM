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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Park
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.DefaultSeedData
import com.example.data.model.DhakaDateUtils
import com.example.data.model.EventBreakdownUtils
import com.example.data.model.FinTransaction
import com.example.data.model.MoneyUtils
import com.example.data.model.parseHexColor
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VibrantCyan
import kotlin.math.roundToLong

fun iconForName(name: String): ImageVector = when (name.trim().lowercase()) {
    "payments", "cash" -> Icons.Default.Payments
    "account_balance", "bank" -> Icons.Default.AccountBalance
    "smartphone", "phone_android", "mobile" -> Icons.Default.PhoneAndroid
    "wallet", "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
    "work", "salary" -> Icons.Default.Work
    "laptop", "freelance" -> Icons.Default.Laptop
    "store", "business" -> Icons.Default.Store
    "movie", "confirmationnumber", "entertainment" -> Icons.Default.Movie
    "code", "development" -> Icons.Default.Code
    "trending_up", "invest" -> Icons.AutoMirrored.Filled.TrendingUp
    "redeem", "gift", "festival" -> Icons.Default.CardGiftcard
    "restaurant", "food", "gathering" -> Icons.Default.Restaurant
    "directions_bus", "transport" -> Icons.Default.DirectionsBus
    "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
    "school", "education" -> Icons.Default.School
    "home", "housing" -> Icons.Default.Home
    "apartment", "rent" -> Icons.Default.Apartment
    "bolt", "utilities" -> Icons.Default.Bolt
    "wifi", "internet" -> Icons.Default.Wifi
    "medical_services", "health", "medical" -> Icons.Default.MedicalServices
    "flight", "travel", "tour" -> Icons.Default.Flight
    "park", "picnic" -> Icons.Default.Park
    "celebration", "wedding" -> Icons.Default.Celebration
    "cake", "birthday" -> Icons.Default.Cake
    "event", "other_event" -> Icons.Default.Event
    "subscriptions" -> Icons.Default.Subscriptions
    "spa", "personal" -> Icons.Default.Spa
    "family_restroom", "family" -> Icons.Default.FamilyRestroom
    "devices", "tech" -> Icons.Default.Devices
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
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
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
 * Reusable User Avatar Badge supporting Google Auth profile photos, custom image URIs/URLs,
 * and crisp initials fallback.
 */
@Composable
fun UserAvatarBadge(
    displayName: String,
    photoUrl: String,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val initials = remember(displayName) {
        val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "BM"
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        shape = CircleShape,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = if (size >= 48.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile photo of $displayName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            }
        }
    }
}

/**
 * Clean, Non-Overlapping Total Balance Hero Card.
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
    val primaryAccent = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_total_balance_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryAccent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "bn") "মোট ব্যালেন্স" else "Total Balance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                MiniBalanceSparkline(
                    values = sparklineValues,
                    lineColor = primaryAccent,
                    modifier = Modifier
                        .width(88.dp)
                        .height(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedMoneyText(
                amountPaisa = totalBalancePaisa,
                currencySymbol = currencySymbol,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                showDecimals = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isPositive = currentMonthNetPaisa >= 0L
                Surface(
                    color = if (isPositive) EmeraldSuccess.copy(alpha = 0.14f) else CrimsonDanger.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val sign = if (momChangePct > 0) "+" else ""
                    Text(
                        text = "${sign}$momChangePct% MoM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) EmeraldSuccess else CrimsonDanger,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Monthly Net: ${MoneyUtils.formatPaisa(currentMonthNetPaisa, currencySymbol, showPlusSign = true)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wallets (${accounts.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Manage →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryAccent,
                        modifier = Modifier.clickable { onWalletsClick() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.take(5).forEach { acc ->
                        val accColor = parseHexColor(acc.colorHex, primaryAccent)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = iconForName(acc.iconName),
                                    contentDescription = acc.name,
                                    tint = accColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
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
 * Clean 4-Action Quick Strip with prominent icons and short 1-word labels.
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
            label = "Expense",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            accentColor = CrimsonDanger,
            softBg = CrimsonDanger.copy(alpha = 0.14f),
            onClick = onAddExpense,
            testTag = "quick_action_expense",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Income",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = EmeraldSuccess,
            softBg = EmeraldSuccess.copy(alpha = 0.14f),
            onClick = onAddIncome,
            testTag = "quick_action_income",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Transfer",
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            accentColor = RoyalBlue,
            softBg = RoyalBlue.copy(alpha = 0.14f),
            onClick = onTransferMoney,
            testTag = "quick_action_transfer",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Reports",
            icon = Icons.Default.Assessment,
            accentColor = VibrantCyan,
            softBg = VibrantCyan.copy(alpha = 0.14f),
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
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
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

/**
 * Non-overlapping KPI Summary Metric Card with guaranteed visible icon badge.
 */
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
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
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMoneyText(
                amountPaisa = amountPaisa,
                currencySymbol = currencySymbol,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                showDecimals = true
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ITEM",
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
                            text = "WALLET",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.2f)
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
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(modifier = Modifier.width(88.dp))
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
    val parsedEvent = remember(tx.id, tx.notes, tx.categoryId) {
        EventBreakdownUtils.parseEventTransaction(tx)
    }
    val effectiveColorHex = parsedEvent?.eventType?.colorHex ?: categoryColorHex
    val effectiveIconName = parsedEvent?.eventType?.iconName ?: categoryIconName

    val badgeColor = when (tx.type) {
        "INCOME" -> EmeraldSuccess
        "TRANSFER" -> RoyalBlue
        else -> parseHexColor(effectiveColorHex, CrimsonDanger)
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
        else -> iconForName(effectiveIconName)
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
            modifier = Modifier
                .weight(2.2f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.14f)),
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
                if (parsedEvent != null && parsedEvent.items.isNotEmpty()) {
                    Text(
                        text = parsedEvent.items.joinToString(" • ") {
                            "${it.reason}: ${MoneyUtils.formatPaisa(it.amountPaisa, currencySymbol)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (tx.notes.isNotBlank()) {
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
                color = badgeColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = parsedEvent?.eventType?.shortLabel ?: tx.categoryName.ifBlank { tx.type },
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
            modifier = Modifier.weight(1.2f)
        )

        Text(
            text = DhakaDateUtils.formatEpochDayShort(tx.dateEpochDay),
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
            modifier = Modifier.weight(1.2f)
        )

        Row(
            modifier = Modifier.width(88.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("edit_tx_${tx.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("duplicate_tx_${tx.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_tx_${tx.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = CrimsonDanger,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    val parsedEvent = remember(tx.id, tx.notes, tx.categoryId) {
        EventBreakdownUtils.parseEventTransaction(tx)
    }
    val effectiveColorHex = parsedEvent?.eventType?.colorHex ?: categoryColorHex
    val effectiveIconName = parsedEvent?.eventType?.iconName ?: categoryIconName

    val badgeColor = when (tx.type) {
        "INCOME" -> EmeraldSuccess
        "TRANSFER" -> RoyalBlue
        else -> parseHexColor(effectiveColorHex, CrimsonDanger)
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
        else -> iconForName(effectiveIconName)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("tx_item_${tx.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVec,
                        contentDescription = tx.categoryName,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val accountSummary = if (tx.type == "TRANSFER") {
                        "${tx.accountName} → ${tx.toAccountName}"
                    } else {
                        "${parsedEvent?.eventType?.shortLabel ?: tx.categoryName} • ${tx.accountName}"
                    }
                    Text(
                        text = "$accountSummary • ${DhakaDateUtils.formatEpochDayShort(tx.dateEpochDay)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$signPrefix${MoneyUtils.formatPaisa(tx.amountPaisa, currencySymbol, showDecimals = true)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        maxLines = 1
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("edit_tx_${tx.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = onDuplicate,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("duplicate_tx_${tx.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("delete_tx_${tx.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = CrimsonDanger,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (parsedEvent != null && parsedEvent.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    parsedEvent.items.forEach { item ->
                        Surface(
                            color = badgeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = "${item.reason}: ${MoneyUtils.formatPaisa(item.amountPaisa, currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            } else if (tx.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
    defaultDateEpochDay: Long? = null,
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

    val safeAccounts = remember(accounts) {
        accounts.ifEmpty { DefaultSeedData.defaultAccounts("local_default_user").map { it.second } }
    }
    val safeCategories = remember(categories) {
        categories.ifEmpty { DefaultSeedData.defaultCategories("local_default_user").map { it.second } }
    }

    val filteredCategories = remember(safeCategories, txType) {
        safeCategories.filter { it.type == txType }
    }
    var selectedCategoryId by remember(txType, safeCategories) {
        mutableStateOf(
            initialTransaction?.categoryId?.takeIf { it.isNotBlank() }
                ?: filteredCategories.firstOrNull()?.id
                ?: if (txType == "INCOME") "cat_inc_allowance" else "cat_exp_food"
        )
    }

    var selectedAccountId by remember(safeAccounts) {
        mutableStateOf(
            initialTransaction?.accountId?.takeIf { it.isNotBlank() }
                ?: safeAccounts.firstOrNull { it.id == defaultAccountId }?.id
                ?: safeAccounts.firstOrNull()?.id
                ?: "acc_cash"
        )
    }

    var selectedToAccountId by remember(safeAccounts, selectedAccountId) {
        mutableStateOf(
            initialTransaction?.toAccountId?.takeIf { it.isNotBlank() }
                ?: safeAccounts.firstOrNull { it.id != selectedAccountId }?.id
                ?: "acc_bank"
        )
    }

    var dateEpochDay by remember {
        mutableLongStateOf(initialTransaction?.dateEpochDay ?: defaultDateEpochDay ?: DhakaDateUtils.todayEpochDay())
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

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it
                    validationError = null
                },
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("0.00") },
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
                label = {
                    Text(
                        when (txType) {
                            "EXPENSE" -> "Where / Item"
                            "INCOME" -> "Source"
                            else -> "Note"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (txType) {
                            "INCOME" -> "Monthly Allowance / Salary"
                            "TRANSFER" -> "Bank to bKash"
                            else -> "Lunch / Transport / Shopping"
                        }
                    )
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (txType) {
                    "TRANSFER" -> "From Wallet"
                    "INCOME" -> "Deposit Wallet"
                    else -> "Wallet"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                safeAccounts.forEach { acc ->
                    val selected = acc.id == selectedAccountId
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedAccountId = acc.id
                            paymentMethod = acc.name
                            if (txType == "TRANSFER" && selectedToAccountId == acc.id) {
                                selectedToAccountId = safeAccounts.firstOrNull { it.id != acc.id }?.id ?: ""
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "To Wallet",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    safeAccounts.filter { it.id != selectedAccountId }.forEach { acc ->
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
                Spacer(modifier = Modifier.height(10.dp))
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
                            leadingIcon = {
                                Icon(
                                    imageVector = iconForName(cat.iconName),
                                    contentDescription = null,
                                    tint = parseHexColor(cat.colorHex, RoyalBlue),
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = parseHexColor(cat.colorHex, RoyalBlue).copy(alpha = 0.16f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
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
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Date", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(DhakaDateUtils.formatEpochDay(dateEpochDay), maxLines = 1)
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
                            false
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(DhakaDateUtils.formatTime12Hour(timeFormatted), maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Receipt", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (attachmentUri.isBlank()) "Receipt Photo" else "Change Receipt")
                }
                if (attachmentUri.isNotBlank()) {
                    TextButton(onClick = { attachmentUri = "" }) {
                        Text("Remove", color = CrimsonDanger)
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountPaisa = MoneyUtils.parseTakaToPaisa(amountInput, allowZero = false)
                    if (amountPaisa == null) {
                        validationError = "Enter a valid amount."
                        return@Button
                    }
                    val trimmedTitle = title.trim().ifBlank {
                        when (txType) {
                            "TRANSFER" -> "Transfer"
                            else -> filteredCategories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Transaction"
                        }
                    }
                    val fromAcc = safeAccounts.firstOrNull { it.id == selectedAccountId } ?: safeAccounts.first()
                    if (txType == "TRANSFER") {
                        val toAcc = safeAccounts.firstOrNull { it.id == selectedToAccountId }
                        if (toAcc == null || toAcc.id == fromAcc.id) {
                            validationError = "Select a different destination wallet."
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_transaction_button")
            ) {
                Text(if (isEdit) "Update" else "Save", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
