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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
 * Subtle decorative blue sparkline for the Minimal Light Total Balance Hero Card.
 */
@Composable
fun MiniBalanceSparkline(
    values: List<Long>,
    lineColor: Color = RoyalBlue,
    modifier: Modifier = Modifier
) {
    val safeValues = remember(values) {
        if (values.size >= 2) values else listOf(100L, 130L, 120L, 160L, 155L, 190L, 210L)
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
            val normalized = ((v - minV).toFloat() / range).coerceIn(0.08f, 0.95f) * animProgress.value
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
                colors = listOf(lineColor.copy(alpha = 0.16f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )

        val lastPt = coords.last()
        drawCircle(
            color = lineColor.copy(alpha = 0.22f),
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
 * Interactive dual-series or single-series Bar Chart with tap-to-inspect tooltip.
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
        EmptyChartBox("No data for the selected period")
        return
    }

    var selectedIndex by remember(points) { mutableIntStateOf((points.size - 1).coerceAtLeast(0)) }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
    }

    val maxVal = remember(points, showIncomeSeries, showExpenseSeries) {
        val m = points.maxOfOrNull { pt ->
            max(
                if (showIncomeSeries) pt.incomePaisa else 0L,
                if (showExpenseSeries) pt.expensePaisa else 0L
            )
        } ?: 1L
        max(m, 100L)
    }

    val selectedPoint = points.getOrNull(selectedIndex.coerceIn(0, points.lastIndex))
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

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
                .height(188.dp)
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

            for (i in 0..3) {
                val y = chartTop + usableHeight * (i / 3f)
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
                (slotW * 0.34f).coerceIn(3.dp.toPx(), 16.dp.toPx())
            } else {
                (slotW * 0.52f).coerceIn(4.dp.toPx(), 24.dp.toPx())
            }

            points.forEachIndexed { idx, pt ->
                val centerX = slotW * idx + slotW / 2f
                val isSelected = idx == selectedIndex

                if (isSelected) {
                    drawRoundRect(
                        color = RoyalBlue.copy(alpha = 0.06f),
                        topLeft = Offset(slotW * idx + 2f, chartTop),
                        size = Size(slotW - 4f, usableHeight),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }

                if (bothSeries) {
                    val incRatio = (pt.incomePaisa.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                    val expRatio = (pt.expensePaisa.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                    val incH = (usableHeight * incRatio).coerceAtLeast(2.dp.toPx())
                    val expH = (usableHeight * expRatio).coerceAtLeast(2.dp.toPx())

                    drawRoundRect(
                        color = EmeraldSuccess,
                        topLeft = Offset(centerX - barW - 1.5.dp.toPx(), chartBottom - incH),
                        size = Size(barW, incH),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    drawRoundRect(
                        color = CrimsonDanger,
                        topLeft = Offset(centerX + 1.5.dp.toPx(), chartBottom - expH),
                        size = Size(barW, expH),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                } else {
                    val value = if (showIncomeSeries) pt.incomePaisa else pt.expensePaisa
                    val color = if (showIncomeSeries) EmeraldSuccess else CrimsonDanger
                    val ratio = (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) * animProgress.value
                    val barH = (usableHeight * ratio).coerceAtLeast(2.dp.toPx())

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(centerX - barW / 2f, chartBottom - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                }
            }
        }

        ChartXAxisLabels(points = points, selectedIndex = selectedIndex)
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
        max(m, 100L)
    }

    val selectedPoint = points.getOrNull(selectedIndex.coerceIn(0, points.lastIndex))
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

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
                .height(188.dp)
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

            for (i in 0..3) {
                val y = padTop + usableH * (i / 3f)
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
                        colors = listOf(lineColor.copy(alpha = 0.18f), Color.Transparent),
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
        color = MaterialTheme.colorScheme.surfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurface
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
                            color = EmeraldSuccess
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
                            color = CrimsonDanger
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
        EmptyChartBox("No category spending recorded yet")
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
            modifier = Modifier.size(200.dp),
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
                val strokeWidthPx = 24.dp.toPx()
                val selectedStrokePx = 30.dp.toPx()

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
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = activeSlice.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = activeSlice.color
                    )
                    Text(
                        text = MoneyUtils.formatPaisa(activeSlice.amountPaisa, currencySymbol),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEachIndexed { idx, slice ->
                val pct = (slice.amountPaisa * 100.0 / totalPaisa).toInt()
                val isSelected = idx == selectedIdx
                Surface(
                    color = if (isSelected) slice.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) slice.color.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedIdx = idx }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
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
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
