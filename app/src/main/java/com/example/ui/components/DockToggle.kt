package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Universal Dock Toggle-style ON/OFF Switch.
 * Provides a tactile, lightweight, and fluid animated selector matching the Dock design language.
 * Ensures zero frame drops with fast hardware-accelerated color & pill state transitions.
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
    val dockShape = RoundedCornerShape(20.dp)
    val pillShape = RoundedCornerShape(16.dp)

    // Outer dock container
    Box(
        modifier = modifier
            .testTag(testTag)
            .semantics {
                role = Role.Switch
                contentDescription = if (checked) onLabel else offLabel
            }
            .clip(dockShape)
            .background(Color(0xFF10141D))
            .border(1.dp, Color(0xFF222B3D), dockShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            }
            .padding(3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- OFF Segment ---
            val isOff = !checked
            val offTextColor by animateColorAsState(
                targetValue = if (isOff) Color.White else Color(0xFF758599),
                animationSpec = spring(stiffness = 600f),
                label = "dockOffTextColor"
            )

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(
                        if (isOff) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF283242),
                                    Color(0xFF1F2836)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled
                    ) {
                        if (checked) onCheckedChange(false)
                    }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
                    .widthIn(min = 34.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = offLabel,
                    color = offTextColor,
                    fontSize = 11.5.sp,
                    fontWeight = if (isOff) FontWeight.Bold else FontWeight.Medium
                )
            }

            // --- ON Segment ---
            val isOn = checked
            val onTextColor by animateColorAsState(
                targetValue = if (isOn) Color.White else Color(0xFF758599),
                animationSpec = spring(stiffness = 600f),
                label = "dockOnTextColor"
            )

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(
                        if (isOn) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0066CC),
                                    Color(0xFF0099FF)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled
                    ) {
                        if (!checked) onCheckedChange(true)
                    }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
                    .widthIn(min = 34.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = onLabel,
                    color = onTextColor,
                    fontSize = 11.5.sp,
                    fontWeight = if (isOn) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
