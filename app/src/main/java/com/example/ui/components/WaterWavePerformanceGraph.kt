package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.performance.PerformanceTelemetry
import com.example.ui.theme.NetisBlueDark
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisPurple
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI
import kotlin.math.sin

/**
 * Ultra-smooth, high-performance Water Waves-style CPU & GPU Monitor Graph.
 *
 * Uses pre-allocated reusable Paths and isolated telemetry state consumption
 * so that live telemetry updates and wave animations do NOT trigger recompositions
 * of the parent screen or LazyColumn, guaranteeing buttery-smooth scrolling with zero frame drops.
 */
@Composable
fun WaterWavePerformanceGraph(
    telemetryFlow: StateFlow<PerformanceTelemetry>,
    modifier: Modifier = Modifier
) {
    val telemetry by telemetryFlow.collectAsState()
    WaterWavePerformanceGraphContent(telemetry = telemetry, modifier = modifier)
}

@Composable
fun WaterWavePerformanceGraph(
    telemetry: PerformanceTelemetry,
    modifier: Modifier = Modifier
) {
    WaterWavePerformanceGraphContent(telemetry = telemetry, modifier = modifier)
}

@Composable
private fun WaterWavePerformanceGraphContent(
    telemetry: PerformanceTelemetry,
    modifier: Modifier = Modifier
) {
    // Smoothly animated liquid water levels (damping sudden spikes for liquid physics)
    val animatedCpuLevel by animateFloatAsState(
        targetValue = (telemetry.currentCpuPercent.coerceIn(5f, 98f) / 100f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "cpuWaterLevel"
    )
    val animatedGpuLevel by animateFloatAsState(
        targetValue = (telemetry.currentGpuPercent.coerceIn(5f, 98f) / 100f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "gpuWaterLevel"
    )

    // Fluid continuous wave phase offsets
    val infiniteTransition = rememberInfiniteTransition(label = "waveInfiniteTransition")

    val cpuPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cpuWavePhase"
    )

    val gpuPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpuWavePhase"
    )

    // Reusable Path instances to prevent Garbage Collection allocation in onDraw
    val cpuFillPath = remember { Path() }
    val cpuCrestPath = remember { Path() }
    val gpuFillPath = remember { Path() }
    val gpuCrestPath = remember { Path() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Live Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CPU Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NetisCyanAccent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(NetisCyanAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CPU: ${String.format("%.1f", telemetry.currentCpuPercent)}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = NetisCyanAccent
                    )
                }

                // GPU Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NetisPurple.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(NetisPurple, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPU: ${String.format("%.1f", telemetry.currentGpuPercent)}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = NetisPurple
                    )
                }

                // Frame Pacing Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.1f", telemetry.framePacingMs)}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // The Water Waves Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF090E17))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Subtle horizontal depth grid lines (25%, 50%, 75%)
                    val gridLines = 3
                    for (i in 1..gridLines) {
                        val y = h * (i.toFloat() / (gridLines + 1).toFloat())
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 2. GPU Wave (Rendered as back water layer)
                    val gpuBaseY = h - (h * 0.85f * animatedGpuLevel) - (h * 0.08f)
                    val gpuAmplitude = 9.dp.toPx()
                    val gpuWaveLength = w * 0.9f

                    gpuFillPath.reset()
                    gpuCrestPath.reset()

                    gpuFillPath.moveTo(0f, h)
                    val stepPx = 10f
                    var x = 0f

                    while (x <= w) {
                        val rad = (2 * PI * (x / gpuWaveLength) + gpuPhase).toFloat()
                        val waveY = (gpuBaseY + gpuAmplitude * sin(rad)).coerceIn(0f, h)
                        if (x == 0f) {
                            gpuFillPath.lineTo(x, waveY)
                            gpuCrestPath.moveTo(x, waveY)
                        } else {
                            gpuFillPath.lineTo(x, waveY)
                            gpuCrestPath.lineTo(x, waveY)
                        }
                        x += stepPx
                    }
                    // Complete right edge
                    val lastGpuRad = (2 * PI * (w / gpuWaveLength) + gpuPhase).toFloat()
                    val lastGpuY = (gpuBaseY + gpuAmplitude * sin(lastGpuRad)).coerceIn(0f, h)
                    gpuFillPath.lineTo(w, lastGpuY)
                    gpuCrestPath.lineTo(w, lastGpuY)
                    gpuFillPath.lineTo(w, h)
                    gpuFillPath.close()

                    // Draw GPU Water Fill Gradient
                    drawPath(
                        path = gpuFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NetisPurple.copy(alpha = 0.40f),
                                NetisPurple.copy(alpha = 0.15f),
                                Color(0xFF130924).copy(alpha = 0.45f)
                            ),
                            startY = gpuBaseY - gpuAmplitude,
                            endY = h
                        )
                    )

                    // Draw GPU Glowing Crest Line
                    drawPath(
                        path = gpuCrestPath,
                        color = NetisPurple.copy(alpha = 0.85f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. CPU Wave (Rendered as front water layer)
                    val cpuBaseY = h - (h * 0.85f * animatedCpuLevel) - (h * 0.08f)
                    val cpuAmplitude = 12.dp.toPx()
                    val cpuWaveLength = w * 0.75f

                    cpuFillPath.reset()
                    cpuCrestPath.reset()

                    cpuFillPath.moveTo(0f, h)
                    x = 0f

                    while (x <= w) {
                        val rad = (2 * PI * (x / cpuWaveLength) + cpuPhase).toFloat()
                        // Secondary micro harmonic for realistic liquid water ripples
                        val harmonicRad = (4 * PI * (x / cpuWaveLength) + cpuPhase * 1.5f).toFloat()
                        val waveY = (cpuBaseY + cpuAmplitude * sin(rad) + (cpuAmplitude * 0.25f * sin(harmonicRad))).coerceIn(0f, h)

                        if (x == 0f) {
                            cpuFillPath.lineTo(x, waveY)
                            cpuCrestPath.moveTo(x, waveY)
                        } else {
                            cpuFillPath.lineTo(x, waveY)
                            cpuCrestPath.lineTo(x, waveY)
                        }
                        x += stepPx
                    }
                    val lastCpuRad = (2 * PI * (w / cpuWaveLength) + cpuPhase).toFloat()
                    val lastHarmonic = (4 * PI * (w / cpuWaveLength) + cpuPhase * 1.5f).toFloat()
                    val lastCpuY = (cpuBaseY + cpuAmplitude * sin(lastCpuRad) + (cpuAmplitude * 0.25f * sin(lastHarmonic))).coerceIn(0f, h)
                    cpuFillPath.lineTo(w, lastCpuY)
                    cpuCrestPath.lineTo(w, lastCpuY)
                    cpuFillPath.lineTo(w, h)
                    cpuFillPath.close()

                    // Draw CPU Water Fill Gradient
                    drawPath(
                        path = cpuFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NetisCyanAccent.copy(alpha = 0.48f),
                                NetisBluePrimary.copy(alpha = 0.25f),
                                Color(0xFF041C2C).copy(alpha = 0.65f)
                            ),
                            startY = cpuBaseY - cpuAmplitude,
                            endY = h
                        )
                    )

                    // Draw CPU Glowing Crest Line
                    drawPath(
                        path = cpuCrestPath,
                        color = NetisCyanAccent,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Water Surface Subtle Indicator Overlays
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Water,
                        contentDescription = null,
                        tint = NetisCyanAccent.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dynamic Water Waves Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = "Avg: CPU ${String.format("%.0f", telemetry.averageCpu)}% • GPU ${String.format("%.0f", telemetry.averageGpu)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer statistics & status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Real-time hardware telemetry",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "Hardware Accelerated • 120Hz Fluid",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = NetisCyanAccent
                )
            }
        }
    }
}
