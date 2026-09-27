package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetisBlueLight
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    speedMbps: Float,
    maxSpeedMbps: Float = 100f,
    phaseText: String = "Download",
    modifier: Modifier = Modifier
) {
    val targetRatio = (speedMbps / maxSpeedMbps).coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = spring(stiffness = 200f, dampingRatio = 0.75f),
        label = "speedRatio"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 14.dp.toPx()
            val radius = (size.width - strokeWidth * 2) / 2f

            // Start angle 135 deg, sweep 270 deg (leaving bottom open)
            val startAngle = 135f
            val totalSweep = 270f

            // Background Track Arc
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Speed Gradient Arc
            val activeSweep = totalSweep * animatedRatio
            if (activeSweep > 1f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to NetisCyanAccent,
                        0.5f to NetisBlueLight,
                        1.0f to NetisPurple
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth, strokeWidth),
                    size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Needle Dot
            val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val needleX = center.x + radius * cos(currentAngleRad).toFloat()
            val needleY = center.y + radius * sin(currentAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(needleX, needleY)
            )
            drawCircle(
                color = NetisCyanAccent,
                radius = 3.dp.toPx(),
                center = Offset(needleX, needleY)
            )
        }

        // Center Digital Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", speedMbps),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phaseText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
