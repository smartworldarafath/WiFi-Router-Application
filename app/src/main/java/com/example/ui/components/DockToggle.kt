package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Universal Dock Toggle Switch with an authentic physical swipe / sliding thumb animation.
 * Features a hardware-accelerated gliding thumb pill that smoothly swipes between OFF and ON states.
 * Supports continuous real-time horizontal finger dragging / swiping as well as responsive click-to-toggle.
 */
@Composable
fun DockToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    offLabel: String = "OFF",
    onLabel: String = "ON",
    enabled: Boolean = true,
    testTag: String = "dock_toggle"
) {
    val density = LocalDensity.current
    val totalWidth = 86.dp
    val totalHeight = 36.dp
    val innerPadding = 3.dp
    val dockShape = RoundedCornerShape(20.dp)
    val thumbShape = RoundedCornerShape(16.dp)

    // Calculate thumb width and max slide offset
    val thumbWidth = (totalWidth - (innerPadding * 2)) / 2
    val maxSlideOffset = thumbWidth

    // Drag offset in pixels during interactive swipe
    var dragAccumulatorPx by remember { mutableFloatStateOf(0f) }

    // Physical sliding swipe animation driven by fluid spring physics
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxSlideOffset else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dockSwipeThumbOffset"
    )

    // Animated track background color
    val trackBgColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF0F1E33) else Color(0xFF0F131C),
        animationSpec = spring(stiffness = 500f),
        label = "dockTrackBg"
    )
    val trackBorderColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF1E406A) else Color(0xFF1F2837),
        animationSpec = spring(stiffness = 500f),
        label = "dockTrackBorder"
    )

    // Animated text colors
    val offTextColor by animateColorAsState(
        targetValue = if (!checked) Color.White else Color(0xFF6B7C91),
        animationSpec = spring(stiffness = 500f),
        label = "dockOffTextColor"
    )
    val onTextColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color(0xFF6B7C91),
        animationSpec = spring(stiffness = 500f),
        label = "dockOnTextColor"
    )

    // Animated thumb gradient colors
    val thumbColorStart by animateColorAsState(
        targetValue = if (checked) Color(0xFF0066CC) else Color(0xFF333E50),
        animationSpec = spring(stiffness = 500f),
        label = "thumbStart"
    )
    val thumbColorEnd by animateColorAsState(
        targetValue = if (checked) Color(0xFF0099FF) else Color(0xFF242C3A),
        animationSpec = spring(stiffness = 500f),
        label = "thumbEnd"
    )
    val thumbBorderColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF38BDF8).copy(alpha = 0.65f) else Color(0xFF475569),
        animationSpec = spring(stiffness = 500f),
        label = "thumbBorder"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .semantics {
                role = Role.Switch
                contentDescription = if (checked) onLabel else offLabel
            }
            .width(totalWidth)
            .height(totalHeight)
            .clip(dockShape)
            .background(trackBgColor)
            .border(1.dp, trackBorderColor, dockShape)
            .pointerInput(checked, enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulatorPx = 0f },
                    onDragEnd = {
                        val maxSlidePx = with(density) { maxSlideOffset.toPx() }
                        if (checked && dragAccumulatorPx < -maxSlidePx * 0.25f) {
                            onCheckedChange(false)
                        } else if (!checked && dragAccumulatorPx > maxSlidePx * 0.25f) {
                            onCheckedChange(true)
                        }
                        dragAccumulatorPx = 0f
                    },
                    onDragCancel = { dragAccumulatorPx = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulatorPx += dragAmount
                        val maxSlidePx = with(density) { maxSlideOffset.toPx() }
                        if (checked && dragAccumulatorPx < -maxSlidePx * 0.45f) {
                            onCheckedChange(false)
                            dragAccumulatorPx = 0f
                        } else if (!checked && dragAccumulatorPx > maxSlidePx * 0.45f) {
                            onCheckedChange(true)
                            dragAccumulatorPx = 0f
                        }
                    }
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            }
            .padding(innerPadding)
    ) {
        // --- Gliding Swipe Thumb Pill ---
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(thumbWidth)
                .fillMaxHeight()
                .shadow(elevation = 3.dp, shape = thumbShape, clip = false)
                .clip(thumbShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(thumbColorStart, thumbColorEnd)
                    )
                )
                .border(1.dp, thumbBorderColor, thumbShape),
            contentAlignment = Alignment.Center
        ) {
            // Subtle grip accent bar in the center of the sliding thumb
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (checked) 0.5f else 0.25f))
            )
        }

        // --- Foreground Label Tracks ---
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // OFF Label Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled
                    ) {
                        if (checked) onCheckedChange(false)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = offLabel,
                    color = offTextColor,
                    fontSize = 11.5.sp,
                    fontWeight = if (!checked) FontWeight.Bold else FontWeight.Medium
                )
            }

            // ON Label Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled
                    ) {
                        if (!checked) onCheckedChange(true)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = onLabel,
                    color = onTextColor,
                    fontSize = 11.5.sp,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
