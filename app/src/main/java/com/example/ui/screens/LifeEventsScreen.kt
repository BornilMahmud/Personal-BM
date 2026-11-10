package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.DefaultSeedData
import com.example.data.model.DhakaDateUtils
import com.example.data.model.EventBreakdownItem
import com.example.data.model.EventBreakdownUtils
import com.example.data.model.FinTransaction
import com.example.data.model.LifeEventType
import com.example.data.model.MoneyUtils
import com.example.data.model.ParticipatedLifeEvent
import com.example.data.model.parseHexColor
import com.example.ui.components.DonutSlice
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.InteractiveSliceBarChart
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.iconForName
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.PersonalBmUiState

data class DraftEventCostRow(
    val reason: String = "",
    val amountText: String = ""
)

/**
 * Clean, Non-Overlapping Life Event Expense & Cost Breakdown Form.
 * Deducts from Cash Wallet by default while breaking down each individual cost & reason.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventExpenseCreatorForm(
    accounts: List<Account>,
    currencySymbol: String,
    defaultDateEpochDay: Long = DhakaDateUtils.todayEpochDay(),
    defaultTimeFormatted: String = DhakaDateUtils.currentTimeFormatted(),
    onRecordEventExpense: (
        eventType: LifeEventType,
        eventName: String,
        items: List<EventBreakdownItem>,
        accountId: String,
        accountName: String,
        extraNotes: String,
        dateEpochDay: Long,
        timeFormatted: String
    ) -> Unit
) {
    val context = LocalContext.current
    val safeAccounts = remember(accounts) {
        accounts.ifEmpty { DefaultSeedData.defaultAccounts("local_default_user").map { it.second } }
    }

    var selectedEventType by remember { mutableStateOf(LifeEventType.TOUR) }
    var eventName by remember { mutableStateOf("") }
    var selectedAccountId by remember(safeAccounts) {
        mutableStateOf(
            safeAccounts.firstOrNull { it.id == "acc_cash" }?.id
                ?: safeAccounts.firstOrNull()?.id
                ?: "acc_cash"
        )
    }
    var dateEpochDay by remember { mutableLongStateOf(defaultDateEpochDay) }
    var timeFormatted by remember { mutableStateOf(defaultTimeFormatted) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val costRows = remember {
        mutableStateListOf(
            DraftEventCostRow(reason = "Ticket / Transport", amountText = ""),
            DraftEventCostRow(reason = "Food", amountText = "")
        )
    }

    val liveParsedItems = costRows.mapNotNull { row ->
        val paisa = MoneyUtils.parseTakaToPaisa(row.amountText, allowZero = false)
        if (paisa != null && paisa > 0L) {
            EventBreakdownItem(
                reason = row.reason.trim().ifBlank { selectedEventType.shortLabel },
                amountPaisa = paisa
            )
        } else null
    }
    val liveTotalPaisa = liveParsedItems.sumOf { it.amountPaisa }
    val selectedAccount = safeAccounts.firstOrNull { it.id == selectedAccountId } ?: safeAccounts.first()
    val eventAccent = parseHexColor(selectedEventType.colorHex, RoyalBlue)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LifeEventType.entries.forEach { type ->
                val isSelected = selectedEventType == type
                val chipColor = parseHexColor(type.colorHex, RoyalBlue)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedEventType = type
                        errorMessage = null
                    },
                    label = { Text(type.shortLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = iconForName(type.iconName),
                            contentDescription = type.label,
                            tint = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("event_type_chip_${type.id}")
                )
            }
        }

        OutlinedTextField(
            value = eventName,
            onValueChange = {
                eventName = it
                errorMessage = null
            },
            label = { Text("Event Name") },
            placeholder = { Text("e.g. Cox's Bazar Tour / Wedding") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("event_name_input")
        )

        // Preset Reason Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            selectedEventType.presetReasons.forEach { preset ->
                Surface(
                    color = eventAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, eventAccent.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val emptyIdx = costRows.indexOfFirst { it.reason.isBlank() && it.amountText.isBlank() }
                            if (emptyIdx >= 0) {
                                costRows[emptyIdx] = costRows[emptyIdx].copy(reason = preset)
                            } else {
                                costRows.add(DraftEventCostRow(reason = preset, amountText = ""))
                            }
                        }
                ) {
                    Text(
                        text = "+ $preset",
                        style = MaterialTheme.typography.labelSmall,
                        color = eventAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Itemized Cost & Reason Rows
        costRows.forEachIndexed { idx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = row.reason,
                    onValueChange = { newReason ->
                        costRows[idx] = costRows[idx].copy(reason = newReason)
                        errorMessage = null
                    },
                    label = { Text("Reason #${idx + 1}") },
                    placeholder = { Text("Ticket / Food") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("event_cost_reason_$idx")
                )
                OutlinedTextField(
                    value = row.amountText,
                    onValueChange = { newAmt ->
                        costRows[idx] = costRows[idx].copy(amountText = newAmt)
                        errorMessage = null
                    },
                    label = { Text("Cost ($currencySymbol)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("event_cost_amount_$idx")
                )
                if (costRows.size > 1) {
                    IconButton(
                        onClick = { costRows.removeAt(idx) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = CrimsonDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    costRows.add(DraftEventCostRow(reason = "", amountText = ""))
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("event_add_cost_row_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item")
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "From ${selectedAccount.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-${MoneyUtils.formatPaisa(liveTotalPaisa, currencySymbol, showDecimals = true)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = CrimsonDanger
                )
            }
        }

        // Wallet Selector Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            safeAccounts.forEach { acc ->
                FilterChip(
                    selected = selectedAccountId == acc.id,
                    onClick = { selectedAccountId = acc.id },
                    label = {
                        Text("${acc.name} (${MoneyUtils.formatPaisa(acc.currentBalancePaisa, currencySymbol)})")
                    },
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

        // Clean 2-Button Date & Time Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(DhakaDateUtils.formatEpochDayShort(dateEpochDay), maxLines = 1)
            }
            OutlinedButton(
                onClick = {
                    val parts = timeFormatted.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 12
                    val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    TimePickerDialog(
                        context,
                        { _, selH, selM ->
                            timeFormatted = String.format("%02d:%02d", selH, selM)
                        },
                        h,
                        min,
                        false
                    ).show()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(DhakaDateUtils.formatTime12Hour(timeFormatted), maxLines = 1)
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonDanger
            )
        }

        Button(
            onClick = {
                if (liveParsedItems.isEmpty() || liveTotalPaisa <= 0L) {
                    errorMessage = "Enter at least one cost item greater than 0."
                    return@Button
                }
                val finalEventName = eventName.trim().ifBlank { selectedEventType.label }
                onRecordEventExpense(
                    selectedEventType,
                    finalEventName,
                    liveParsedItems,
                    selectedAccount.id,
                    selectedAccount.name,
                    "",
                    dateEpochDay,
                    timeFormatted
                )
                eventName = ""
                costRows.clear()
                costRows.add(DraftEventCostRow(reason = "Ticket / Transport", amountText = ""))
                costRows.add(DraftEventCostRow(reason = "Food", amountText = ""))
                errorMessage = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_event_expense_button")
        ) {
            Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Event (-${MoneyUtils.formatPaisa(liveTotalPaisa, currencySymbol)})",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dashboard Life Events Section with Bar Chart & Donut Chart toggle and itemized breakdown cards.
 */
@Composable
fun DashboardLifeEventsSection(
    transactions: List<FinTransaction>,
    accounts: List<Account>,
    currencySymbol: String,
    onAddEventCostItems: (
        eventType: LifeEventType,
        eventName: String,
        items: List<EventBreakdownItem>,
        accountId: String,
        accountName: String
    ) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit,
    onOpenEventsModule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val participatedEvents = remember(transactions) {
        EventBreakdownUtils.aggregateParticipatedEvents(transactions)
    }
    val totalEventSpendingPaisa = remember(participatedEvents) {
        participatedEvents.sumOf { it.totalAmountPaisa }
    }
    val eventSlices = remember(participatedEvents) {
        participatedEvents.map { ev ->
            DonutSlice(
                id = ev.eventKey,
                label = "${ev.eventType.shortLabel}: ${ev.eventName}",
                amountPaisa = ev.totalAmountPaisa,
                color = parseHexColor(ev.eventType.colorHex, RoyalBlue)
            )
        }
    }

    var chartMode by remember { mutableStateOf("BAR") } // "BAR" or "DONUT"
    var addingToEvent by remember { mutableStateOf<ParticipatedLifeEvent?>(null) }

    if (addingToEvent != null) {
        AddCostToExistingEventDialog(
            event = addingToEvent!!,
            accounts = accounts,
            currencySymbol = currencySymbol,
            onDismiss = { addingToEvent = null },
            onSave = { items, accId, accName ->
                val target = addingToEvent!!
                onAddEventCostItems(target.eventType, target.eventName, items, accId, accName)
                addingToEvent = null
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_life_events_section")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = "Life Events",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Life Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${participatedEvents.size} events • -${MoneyUtils.formatPaisa(totalEventSpendingPaisa, currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = CrimsonDanger,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { chartMode = if (chartMode == "BAR") "DONUT" else "BAR" },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (chartMode == "BAR") Icons.Default.DonutLarge else Icons.Default.BarChart,
                            contentDescription = "Switch Chart",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    TextButton(onClick = onOpenEventsModule) {
                        Text("Events →", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (participatedEvents.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Track Tours, Picnics & Weddings",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Deducted from Cash with itemized cost breakdown",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenEventsModule,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
            } else {
                if (chartMode == "BAR") {
                    InteractiveSliceBarChart(
                        slices = eventSlices,
                        currencySymbol = currencySymbol
                    )
                } else {
                    InteractiveDonutChart(
                        slices = eventSlices,
                        currencySymbol = currencySymbol
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

                participatedEvents.take(4).forEach { event ->
                    ParticipatedEventBreakdownCard(
                        event = event,
                        currencySymbol = currencySymbol,
                        onAddMoreCost = { addingToEvent = event },
                        onDeleteLatestTx = {
                            event.transactions.firstOrNull()?.let { onDeleteTransaction(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipatedEventBreakdownCard(
    event: ParticipatedLifeEvent,
    currencySymbol: String,
    onAddMoreCost: () -> Unit,
    onDeleteLatestTx: () -> Unit
) {
    val eventColor = parseHexColor(event.eventType.colorHex, RoyalBlue)
    var expanded by remember { mutableStateOf(true) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, eventColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("participated_event_card_${event.eventKey}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Surface(
                        color = eventColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconForName(event.eventType.iconName),
                                contentDescription = event.eventType.label,
                                tint = eventColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.eventName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${event.eventType.shortLabel} • ${event.accountName} • ${DhakaDateUtils.formatEpochDayShort(event.dateEpochDay)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "-${MoneyUtils.formatPaisa(event.totalAmountPaisa, currencySymbol, showDecimals = true)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = CrimsonDanger
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

                    event.breakdownItems.forEachIndexed { idx, item ->
                        val ratio = if (event.totalAmountPaisa > 0L) {
                            (item.amountPaisa.toFloat() / event.totalAmountPaisa.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        val animatedRatio by animateFloatAsState(
                            targetValue = ratio,
                            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                            label = "event_item_ratio_$idx"
                        )
                        val pct = (ratio * 100).toInt()

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "${MoneyUtils.formatPaisa(item.amountPaisa, currencySymbol)} ($pct%)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonDanger
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { animatedRatio },
                                    color = eventColor,
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onAddMoreCost) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Cost", color = eventColor)
                        }
                        IconButton(
                            onClick = onDeleteLatestTx,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCostToExistingEventDialog(
    event: ParticipatedLifeEvent,
    accounts: List<Account>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (List<EventBreakdownItem>, String, String) -> Unit
) {
    val safeAccounts = remember(accounts) {
        accounts.ifEmpty { DefaultSeedData.defaultAccounts("local_default_user").map { it.second } }
    }
    var reason by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedAccountId by remember(event.accountId) {
        mutableStateOf(event.accountId.ifBlank { "acc_cash" })
    }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Cost — ${event.eventName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    event.eventType.presetReasons.forEach { preset ->
                        FilterChip(
                            selected = reason == preset,
                            onClick = { reason = preset },
                            label = { Text(preset) }
                        )
                    }
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        err = null
                    },
                    label = { Text("Reason") },
                    placeholder = { Text("e.g. Ticket, Food, Gift") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        err = null
                    },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    safeAccounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name) }
                        )
                    }
                }
                if (err != null) {
                    Text(err!!, color = CrimsonDanger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paisa = MoneyUtils.parseTakaToPaisa(amountStr, allowZero = false)
                    if (paisa == null || paisa <= 0L) {
                        err = "Enter a valid amount."
                        return@Button
                    }
                    val acc = safeAccounts.firstOrNull { it.id == selectedAccountId } ?: safeAccounts.first()
                    onSave(
                        listOf(
                            EventBreakdownItem(
                                reason = reason.trim().ifBlank { "Event Cost" },
                                amountPaisa = paisa
                            )
                        ),
                        acc.id,
                        acc.name
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Add Cost")
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
 * Dedicated Full-Screen Life Events Module (NavSection.EVENTS).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LifeEventsScreen(
    uiState: PersonalBmUiState,
    onRecordEventExpense: (
        eventType: LifeEventType,
        eventName: String,
        items: List<EventBreakdownItem>,
        accountId: String,
        accountName: String,
        extraNotes: String,
        dateEpochDay: Long,
        timeFormatted: String
    ) -> Unit,
    onDeleteTransaction: (FinTransaction) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val allEvents = remember(uiState.transactions) {
        EventBreakdownUtils.aggregateParticipatedEvents(uiState.transactions)
    }
    val totalEventCost = remember(allEvents) {
        allEvents.sumOf { it.totalAmountPaisa }
    }
    val cashBalance = remember(uiState.accounts) {
        uiState.accounts.firstOrNull { it.id == "acc_cash" }?.currentBalancePaisa
            ?: uiState.accounts.sumOf { it.currentBalancePaisa }
    }

    var selectedFilterType by remember { mutableStateOf("ALL") }
    var chartMode by remember { mutableStateOf("BAR") }
    var addingToEvent by remember { mutableStateOf<ParticipatedLifeEvent?>(null) }

    val filteredEvents = remember(allEvents, selectedFilterType) {
        if (selectedFilterType == "ALL") allEvents
        else allEvents.filter { it.eventType.id == selectedFilterType }
    }
    val eventSlices = remember(filteredEvents) {
        filteredEvents.map { ev ->
            DonutSlice(
                id = ev.eventKey,
                label = "${ev.eventType.shortLabel}: ${ev.eventName}",
                amountPaisa = ev.totalAmountPaisa,
                color = parseHexColor(ev.eventType.colorHex, RoyalBlue)
            )
        }
    }

    if (addingToEvent != null) {
        AddCostToExistingEventDialog(
            event = addingToEvent!!,
            accounts = uiState.accounts,
            currencySymbol = symbol,
            onDismiss = { addingToEvent = null },
            onSave = { items, accId, accName ->
                val target = addingToEvent!!
                onRecordEventExpense(
                    target.eventType,
                    target.eventName,
                    items,
                    accId,
                    accName,
                    "",
                    DhakaDateUtils.todayEpochDay(),
                    DhakaDateUtils.currentTimeFormatted()
                )
                addingToEvent = null
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
                Text(
                    text = "Life Events",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // KPI Summary Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricCard(
                    title = "Events Cost",
                    amountPaisa = totalEventCost,
                    currencySymbol = symbol,
                    subtitle = "${allEvents.size} events",
                    accentColor = CrimsonDanger,
                    icon = Icons.Default.Celebration,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = "Cash Wallet",
                    amountPaisa = cashBalance,
                    currencySymbol = symbol,
                    subtitle = "Available Cash",
                    accentColor = EmeraldSuccess,
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Event Creator Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "New Life Event",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    EventExpenseCreatorForm(
                        accounts = uiState.accounts,
                        currencySymbol = symbol,
                        onRecordEventExpense = onRecordEventExpense
                    )
                }
            }
        }

        // Event Bar & Donut Chart Card
        if (allEvents.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1280.dp)
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
                                text = "Event Spending Chart",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = chartMode == "BAR",
                                    onClick = { chartMode = "BAR" },
                                    label = { Text("Bars") },
                                    leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                FilterChip(
                                    selected = chartMode == "DONUT",
                                    onClick = { chartMode = "DONUT" },
                                    label = { Text("Donut") },
                                    leadingIcon = { Icon(Icons.Default.DonutLarge, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }

                        if (chartMode == "BAR") {
                            InteractiveSliceBarChart(
                                slices = eventSlices,
                                currencySymbol = symbol
                            )
                        } else {
                            InteractiveDonutChart(
                                slices = eventSlices,
                                currencySymbol = symbol
                            )
                        }
                    }
                }
            }

            // Event Type Filter Chips
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1280.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterType == "ALL",
                        onClick = { selectedFilterType = "ALL" },
                        label = { Text("All (${allEvents.size})") }
                    )
                    LifeEventType.entries.forEach { type ->
                        val count = allEvents.count { it.eventType == type }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedFilterType == type.id,
                                onClick = { selectedFilterType = type.id },
                                label = { Text("${type.shortLabel} ($count)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = iconForName(type.iconName),
                                        contentDescription = null,
                                        tint = parseHexColor(type.colorHex, RoyalBlue),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        items(filteredEvents, key = { it.eventKey }) { ev ->
            Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1280.dp)) {
                ParticipatedEventBreakdownCard(
                    event = ev,
                    currencySymbol = symbol,
                    onAddMoreCost = { addingToEvent = ev },
                    onDeleteLatestTx = {
                        ev.transactions.firstOrNull()?.let { onDeleteTransaction(it) }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
