package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dock Toggle-style selector for refresh rate switching: 60Hz | 120Hz | 144Hz.
 * Features a smooth sliding swipe indicator pill with spring physics.
 */
@Composable
fun RefreshRateDockToggle(
    selectedRate: Int,
    onRateSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    options: List<Int> = listOf(60, 120, 144)
) {
    val dockShape = RoundedCornerShape(22.dp)
    val thumbShape = RoundedCornerShape(18.dp)
    val itemWidth = 58.dp
    val itemHeight = 36.dp
    val spacing = 3.dp
    val innerPadding = 3.dp

    val selectedIndex = remember(selectedRate, options) {
        val idx = options.indexOf(selectedRate)
        if (idx >= 0) idx else 1 // default to 120Hz if not found
    }

    // Smooth gliding pill indicator
    val thumbOffset by animateDpAsState(
        targetValue = (selectedIndex * (itemWidth.value + spacing.value)).dp,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "refreshRateThumbOffset"
    )

    val totalWidth = (itemWidth * options.size) + (spacing * (options.size - 1)) + (innerPadding * 2)

    Box(
        modifier = modifier
            .width(totalWidth)
            .height(itemHeight + (innerPadding * 2))
            .clip(dockShape)
            .background(Color(0xFF10141D))
            .border(1.dp, Color(0xFF222B3D), dockShape)
            .padding(innerPadding)
            .testTag("refresh_rate_dock_toggle")
    ) {
        // --- Gliding Thumb Pill ---
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .clip(thumbShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0066CC),
                            Color(0xFF0099FF)
                        )
                    )
                )
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), thumbShape)
        )

        // --- Foreground Options Row ---
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { rate ->
                val isSelected = selectedRate == rate

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF8A99AD),
                    animationSpec = spring(stiffness = 600f),
                    label = "dockPillText_$rate"
                )

                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .clip(thumbShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                onRateSelected(rate)
                            }
                        }
                        .testTag("refresh_rate_option_${rate}hz"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${rate}Hz",
                        color = textColor,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
