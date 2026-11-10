package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MoneyUtils
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

data class ChartBarPoint(
    val label: String,
    val incomePaisa: Long,
    val expensePaisa: Long
)

data class DonutSlice(
    val id: String,
    val label: String,
    val amountPaisa: Long,
    val color: Color
)

/**
 * Subtle decorative sparkline for the Balance Hero Card.
 */
@Composable
fun MiniBalanceSparkline(
    values: List<Long>,
    lineColor: Color = RoyalBlue,
    modifier: Modifier = Modifier
) {
    val safeValues = remember(values) {
        if (values.size >= 2 && values.any { it != values.first() }) {
            values
        } else {
            listOf(100L, 128L, 118L, 152L, 146L, 182L, 210L)
        }
    }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(safeValues) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padVert = 6.dp.toPx()
        val usableH = (h - padVert * 2f).coerceAtLeast(1f)
        val minV = safeValues.minOrNull() ?: 0L
        val maxV = max((safeValues.maxOrNull() ?: 1L), minV + 100L)
        val range = (maxV - minV).coerceAtLeast(1L).toFloat()

        val coords = safeValues.mapIndexed { idx, v ->
            val x = if (safeValues.size > 1) w * idx / (safeValues.size - 1).toFloat() else w / 2f
            val normalized = ((v - minV).toFloat() / range).coerceIn(0.12f, 0.92f) * animProgress.value
            val y = h - padVert - usableH * normalized
            Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val prev = coords[i - 1]
                val curr = coords[i]
                val midX = (prev.x + curr.x) / 2f
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(coords.last().x, h)
            lineTo(coords.first().x, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.22f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )

        val lastPt = coords.last()
        drawCircle(
            color = lineColor.copy(alpha = 0.24f),
            radius = 6.dp.toPx(),
            center = lastPt
        )
        drawCircle(
            color = lineColor,
            radius = 3.2.dp.toPx(),
            center = lastPt
        )
    }
}

/**
 * High-visibility Interactive Dual-Series Bar Chart with background track columns,
 * Y-axis scale indicators, and tap-to-inspect header pill.
 */
@Composable
fun InteractiveBarChart(
    points: List<ChartBarPoint>,
    currencySymbol: String = "৳",
    showIncomeSeries: Boolean = true,
    showExpenseSeries: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        EmptyChartBox("No chart data yet")
        return
    }

    // Default to the most recent point with data, or the last point
    val initialSelectedIdx = remember(points) {
        val lastNonZero = points.indexOfLast { it.incomePaisa > 0L || it.expensePaisa > 0L }
        if (lastNonZero >= 0) lastNonZero else (points.size - 1).coerceAtLeast(0)
    }
    var selectedIndex by remember(points) { mutableIntStateOf(initialSelectedIdx) }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(520, easing = FastOutSlowInEasing))
    }

    val maxVal = remember(points, showIncomeSeries, showExpenseSeries) {
        val m = points.maxOfOrNull { pt ->
            max(
                if (showIncomeSeries) pt.incomePaisa else 0L,
                if (showExpenseSeries) pt.expensePaisa else 0L
            )
        } ?: 0L
        max(m, 100_00L) // minimum ৳100 scale when empty
    }

    val selectedPoint = points.getOrNull(selectedIndex.coerceIn(0, points.lastIndex))
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    val activeSlotHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedPoint != null) {
            ChartInspectionPill(
                label = selectedPoint.label,
                incomePaisa = selectedPoint.incomePaisa,
                expensePaisa = selectedPoint.expensePaisa,
                currencySymbol = currencySymbol,
                showIncomeSeries = showIncomeSeries,
                showExpenseSeries = showExpenseSeries
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact Y-Axis Labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 6.dp, top = 4.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCompactAxisMoney(maxVal, currencySymbol),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCompactAxisMoney(maxVal / 2L, currencySymbol),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currencySymbol}0",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width.toFloat() / points.size.toFloat()
                            val idx = (offset.x / slotWidth).toInt().coerceIn(0, points.lastIndex)
                            selectedIndex = idx
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val chartBottom = h - 8.dp.toPx()
                val chartTop = 8.dp.toPx()
                val usableHeight = (chartBottom - chartTop).coerceAtLeast(1f)

                // Horizontal reference grid lines
                for (i in 0..2) {
                    val y = chartTop + usableHeight * (i / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val slotW = w / points.size.toFloat()
                val bothSeries = showIncomeSeries && showExpenseSeries
                val barW = if (bothSeries) {
                    (slotW * 0.34f).coerceIn(4.dp.toPx(), 16.dp.toPx())
                } else {
                    (slotW * 0.52f).coerceIn(6.dp.toPx(), 24.dp.toPx())
                }

                points.forEachIndexed { idx, pt ->
                    val centerX = slotW * idx + slotW / 2f
                    val isSelected = idx == selectedIndex

                    if (isSelected) {
                        drawRoundRect(
                            color = activeSlotHighlight,
                            topLeft = Offset(slotW * idx + 2f, chartTop),
                            size = Size((slotW - 4f).coerceAtLeast(2f), usableHeight),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }

                    if (bothSeries) {
                        val incLeft = centerX - barW - 1.5.dp.toPx()
                        val expLeft = centerX + 1.5.dp.toPx()

                        // Background Track Columns so bars are always visibly framed
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(incLeft, chartTop),
                            size = Size(barW, usableHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(expLeft, chartTop),
                            size = Size(barW, usableHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        val incRatio = (pt.incomePaisa.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                        val expRatio = (pt.expensePaisa.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value

                        val incH = if (pt.incomePaisa > 0L) {
                            (usableHeight * incRatio).coerceAtLeast(10.dp.toPx())
                        } else {
                            4.dp.toPx()
                        }
                        val expH = if (pt.expensePaisa > 0L) {
                            (usableHeight * expRatio).coerceAtLeast(10.dp.toPx())
                        } else {
                            4.dp.toPx()
                        }

                        drawRoundRect(
                            color = if (pt.incomePaisa > 0L) EmeraldSuccess else EmeraldSuccess.copy(alpha = 0.28f),
                            topLeft = Offset(incLeft, chartBottom - incH),
                            size = Size(barW, incH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (pt.expensePaisa > 0L) CrimsonDanger else CrimsonDanger.copy(alpha = 0.28f),
                            topLeft = Offset(expLeft, chartBottom - expH),
                            size = Size(barW, expH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    } else {
                        val barLeft = centerX - barW / 2f
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(barLeft, chartTop),
                            size = Size(barW, usableHeight),
                            cornerRadius = CornerRadius(5.dp.toPx())
                        )
                        val value = if (showIncomeSeries) pt.incomePaisa else pt.expensePaisa
                        val color = if (showIncomeSeries) EmeraldSuccess else CrimsonDanger
                        val ratio = (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                        val barH = if (value > 0L) {
                            (usableHeight * ratio).coerceAtLeast(10.dp.toPx())
                        } else {
                            4.dp.toPx()
                        }

                        drawRoundRect(
                            color = if (value > 0L) color else color.copy(alpha = 0.28f),
                            topLeft = Offset(barLeft, chartBottom - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(5.dp.toPx())
                        )
                    }
                }
            }
        }

        ChartXAxisLabels(points = points, selectedIndex = selectedIndex)
    }
}

/**
 * Interactive Colorful Single-Series Bar Chart for Life Events & Categories.
 */
@Composable
fun InteractiveSliceBarChart(
    slices: List<DonutSlice>,
    currencySymbol: String = "৳",
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) {
        EmptyChartBox("No event data yet")
        return
    }

    var selectedIdx by remember(slices) { mutableIntStateOf(0) }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
    }

    val displaySlices = remember(slices) { slices.take(8) }
    val maxVal = remember(displaySlices) {
        max(displaySlices.maxOfOrNull { it.amountPaisa } ?: 0L, 100_00L)
    }
    val selectedSlice = displaySlices.getOrNull(selectedIdx.coerceIn(0, displaySlices.lastIndex))
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedSlice != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(selectedSlice.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedSlice.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = MoneyUtils.formatPaisa(selectedSlice.amountPaisa, currencySymbol),
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = selectedSlice.color
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .pointerInput(displaySlices) {
                    detectTapGestures { offset ->
                        val slotW = size.width.toFloat() / displaySlices.size.toFloat()
                        val idx = (offset.x / slotW).toInt().coerceIn(0, displaySlices.lastIndex)
                        selectedIdx = idx
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val chartBottom = h - 8.dp.toPx()
            val chartTop = 8.dp.toPx()
            val usableHeight = (chartBottom - chartTop).coerceAtLeast(1f)

            for (i in 0..2) {
                val y = chartTop + usableHeight * (i / 2f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            val slotW = w / displaySlices.size.toFloat()
            val barW = (slotW * 0.52f).coerceIn(10.dp.toPx(), 36.dp.toPx())

            displaySlices.forEachIndexed { idx, slice ->
                val centerX = slotW * idx + slotW / 2f
                val barLeft = centerX - barW / 2f
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(barLeft, chartTop),
                    size = Size(barW, usableHeight),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                val ratio = (slice.amountPaisa.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                val barH = (usableHeight * ratio).coerceAtLeast(12.dp.toPx())

                drawRoundRect(
                    color = slice.color,
                    topLeft = Offset(barLeft, chartBottom - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            displaySlices.forEachIndexed { idx, slice ->
                Text(
                    text = slice.label.substringBefore(":").take(8),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (idx == selectedIdx) slice.color else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun formatCompactAxisMoney(paisa: Long, symbol: String): String {
    val taka = paisa / 100L
    return when {
        taka >= 100_000L -> "${symbol}${taka / 100_000L}L"
        taka >= 1_000L -> "${symbol}${taka / 1_000L}k"
        else -> "${symbol}$taka"
    }
}

/**
 * Interactive Smooth Line / Area Cash Flow Chart with gradient fill and tap point inspection.
 */
@Composable
fun InteractiveLineChart(
    points: List<ChartBarPoint>,
    currencySymbol: String = "৳",
    showIncomeSeries: Boolean = true,
    showExpenseSeries: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        EmptyChartBox("No trend data available")
        return
    }

    var selectedIndex by remember(points) { mutableIntStateOf((points.size - 1).coerceAtLeast(0)) }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
    }

    val maxVal = remember(points, showIncomeSeries, showExpenseSeries) {
        val m = points.maxOfOrNull { pt ->
            max(
                if (showIncomeSeries) pt.incomePaisa else 0L,
                if (showExpenseSeries) pt.expensePaisa else 0L
            )
        } ?: 1L
        max(m, 100_00L)
    }

    val selectedPoint = points.getOrNull(selectedIndex.coerceIn(0, points.lastIndex))
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedPoint != null) {
            ChartInspectionPill(
                label = selectedPoint.label,
                incomePaisa = selectedPoint.incomePaisa,
                expensePaisa = selectedPoint.expensePaisa,
                currencySymbol = currencySymbol,
                showIncomeSeries = showIncomeSeries,
                showExpenseSeries = showExpenseSeries
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val step = if (points.size > 1) size.width.toFloat() / (points.size - 1).toFloat() else size.width.toFloat()
                        val idx = ((offset.x + step / 2f) / step).toInt().coerceIn(0, points.lastIndex)
                        selectedIndex = idx
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val padTop = 12.dp.toPx()
            val padBottom = 12.dp.toPx()
            val usableH = (h - padTop - padBottom).coerceAtLeast(1f)

            for (i in 0..2) {
                val y = padTop + usableH * (i / 2f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            fun drawSeries(values: List<Long>, lineColor: Color) {
                if (values.isEmpty()) return
                val coords = values.mapIndexed { idx, v ->
                    val x = if (values.size > 1) (w * idx / (values.size - 1).toFloat()) else w / 2f
                    val ratio = (v.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                    val y = padTop + usableH * (1f - ratio)
                    Offset(x, y)
                }

                val linePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        val prev = coords[i - 1]
                        val curr = coords[i]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(coords.last().x, h - padBottom)
                    lineTo(coords.first().x, h - padBottom)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.2f), Color.Transparent),
                        startY = padTop,
                        endY = h - padBottom
                    )
                )

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                coords.forEachIndexed { idx, pt ->
                    drawCircle(
                        color = lineColor,
                        radius = if (idx == selectedIndex) 5.5.dp.toPx() else 3.dp.toPx(),
                        center = pt
                    )
                    if (idx == selectedIndex) {
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            if (showIncomeSeries) {
                drawSeries(points.map { it.incomePaisa }, EmeraldSuccess)
            }
            if (showExpenseSeries) {
                drawSeries(points.map { it.expensePaisa }, CrimsonDanger)
            }
        }

        ChartXAxisLabels(points = points, selectedIndex = selectedIndex)
    }
}

@Composable
private fun ChartInspectionPill(
    label: String,
    incomePaisa: Long,
    expensePaisa: Long,
    currencySymbol: String,
    showIncomeSeries: Boolean,
    showExpenseSeries: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (showIncomeSeries) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = MoneyUtils.formatPaisa(incomePaisa, currencySymbol),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (showExpenseSeries) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CrimsonDanger)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = MoneyUtils.formatPaisa(expensePaisa, currencySymbol),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = CrimsonDanger,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartXAxisLabels(
    points: List<ChartBarPoint>,
    selectedIndex: Int
) {
    val stride = max(1, points.size / 6)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        points.forEachIndexed { idx, pt ->
            if (idx % stride == 0 || idx == points.lastIndex) {
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (idx == selectedIndex) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (idx == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Interactive Category Distribution Donut Chart with tap segment selection and percentage breakdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveDonutChart(
    slices: List<DonutSlice>,
    currencySymbol: String = "৳",
    modifier: Modifier = Modifier
) {
    val totalPaisa = slices.sumOf { it.amountPaisa }
    if (slices.isEmpty() || totalPaisa <= 0L) {
        EmptyChartBox("No spending recorded yet")
        return
    }

    var selectedIdx by remember(slices) { mutableIntStateOf(0) }
    val activeSlice = slices.getOrNull(selectedIdx.coerceIn(0, slices.lastIndex))
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .pointerInput(slices) {
                        detectTapGestures { tap ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = tap.x - cx
                            val dy = tap.y - cy
                            val dist = hypot(dx, dy)
                            if (dist >= minOf(size.width, size.height).toFloat() * 0.22f) {
                                var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                if (deg < 0f) deg += 360f
                                var acc = 0f
                                slices.forEachIndexed { idx, s ->
                                    val sweep = (s.amountPaisa.toFloat() / totalPaisa.toFloat()) * 360f
                                    if (deg >= acc && deg <= acc + sweep) {
                                        selectedIdx = idx
                                    }
                                    acc += sweep
                                }
                            }
                        }
                    }
            ) {
                var startAngle = -90f
                val strokeWidthPx = 22.dp.toPx()
                val selectedStrokePx = 28.dp.toPx()

                slices.forEachIndexed { index, slice ->
                    val sweep = (slice.amountPaisa.toFloat() / totalPaisa.toFloat()) * 360f * animProgress.value
                    val isSelected = index == selectedIdx
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle + 1.2f,
                        sweepAngle = (sweep - 2.4f).coerceAtLeast(1f),
                        useCenter = false,
                        style = Stroke(
                            width = if (isSelected) selectedStrokePx else strokeWidthPx,
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += sweep
                }
            }

            if (activeSlice != null) {
                val pct = (activeSlice.amountPaisa * 100.0 / totalPaisa).toInt()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 108.dp)
                ) {
                    Text(
                        text = activeSlice.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = activeSlice.color
                    )
                    Text(
                        text = MoneyUtils.formatPaisa(activeSlice.amountPaisa, currencySymbol),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.forEachIndexed { idx, slice ->
                val pct = (slice.amountPaisa * 100.0 / totalPaisa).toInt()
                val isSelected = idx == selectedIdx
                Surface(
                    color = if (isSelected) slice.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) slice.color.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedIdx = idx }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${slice.label} ($pct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
