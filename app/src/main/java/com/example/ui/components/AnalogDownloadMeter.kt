package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisSuccess
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Real-time Analog Speedometer & Progress Gauge for App Updates.
 * Renders an authentic analog gauge with rotating needle, speed graduations,
 * and an outer progress arc.
 */
@Composable
fun AnalogDownloadMeter(
    progress: Float,
    downloadedBytes: Long,
    totalBytes: Long,
    speedBytesPerSec: Long,
    modifier: Modifier = Modifier
) {
    val speedMb = speedBytesPerSec / (1024f * 1024f)
    val dlMb = downloadedBytes / (1024f * 1024f)
    val totMb = totalBytes / (1024f * 1024f)

    // Gauge range: 0 to 10 MB/s (scale to 15 or 20 if speed is higher)
    val maxSpeedMb = 10f
    val speedRatio = (speedMb / maxSpeedMb).coerceIn(0f, 1f)

    // Arc starts from 140 degrees to 400 degrees (260 degree sweep)
    val startAngle = 140f
    val totalSweep = 260f
    val targetNeedleAngle = startAngle + (speedRatio * totalSweep)

    val animatedNeedleAngle by animateFloatAsState(
        targetValue = targetNeedleAngle,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "needleAngle"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 500f),
        label = "progressArc"
    )

    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0D1527) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Downloading Netis Update",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time stream telemetry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NetisBluePrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = NetisCyanAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analog Speedometer Canvas
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 20f

                    // 1. Outer track background arc
                    drawArc(
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )

                    // 2. Outer dynamic progress glow arc
                    if (animatedProgress > 0.005f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(NetisBluePrimary, NetisCyanAccent, NetisSuccess)
                            ),
                            startAngle = startAngle,
                            sweepAngle = totalSweep * animatedProgress,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = 14f, cap = StrokeCap.Round)
                        )
                    }

                    // 3. Dial tick marks and speed numbers
                    val tickCount = 10
                    for (i in 0..tickCount) {
                        val fraction = i / tickCount.toFloat()
                        val angleDeg = startAngle + (fraction * totalSweep)
                        val angleRad = Math.toRadians(angleDeg.toDouble())

                        val isMajor = i % 2 == 0
                        val tickLen = if (isMajor) 16f else 9f
                        val tickWidth = if (isMajor) 3.5f else 2f
                        val tickColor = if (isMajor) {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        } else {
                            if (isDark) Color(0xFF334155) else Color(0xFF94A3B8)
                        }

                        val innerR = radius - 18f - tickLen
                        val outerR = radius - 18f

                        val startP = Offset(
                            (center.x + innerR * cos(angleRad)).toFloat(),
                            (center.y + innerR * sin(angleRad)).toFloat()
                        )
                        val endP = Offset(
                            (center.x + outerR * cos(angleRad)).toFloat(),
                            (center.y + outerR * sin(angleRad)).toFloat()
                        )

                        drawLine(
                            color = tickColor,
                            start = startP,
                            end = endP,
                            strokeWidth = tickWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    // 4. Analog Needle with gradient and shadow
                    val needleLen = radius - 30f
                    val needleAngleRad = Math.toRadians(animatedNeedleAngle.toDouble())

                    val needleTip = Offset(
                        (center.x + needleLen * cos(needleAngleRad)).toFloat(),
                        (center.y + needleLen * sin(needleAngleRad)).toFloat()
                    )

                    // Draw needle line
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFF3366), Color(0xFFFF5252))
                        ),
                        start = center,
                        end = Offset(
                            (center.x + needleLen * cos(needleAngleRad)).toFloat(),
                            (center.y + needleLen * sin(needleAngleRad)).toFloat()
                        ),
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )

                    // 5. Central Hub Pivot Cap
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 18f,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFFF5252),
                        radius = 8f,
                        center = center
                    )
                }

                // Digital Center Readout
                Column(
                    modifier = Modifier.padding(top = 95.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.1f", speedMb),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF00E5FF) else NetisBluePrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MB/s",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRANSFERRED",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", dlMb)} / ${String.format("%.1f", totMb)} MB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SPEED GAUGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val speedKb = speedBytesPerSec / 1024L
                    Text(
                        text = if (speedMb >= 1f) "${String.format("%.2f", speedMb)} MB/s" else "$speedKb KB/s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NetisSuccess
                    )
                }
            }
        }
    }
}
