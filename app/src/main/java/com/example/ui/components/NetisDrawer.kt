package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.performance.AppIconOption
import com.example.ui.theme.NetisBlueDark
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisSuccess

enum class DrawerDestination(val title: String, val icon: ImageVector) {
    ROUTER_WEB("Router Admin", Icons.Default.Language),
    DASHBOARD("Native Dashboard", Icons.Default.Router),
    APP_ICONS("App Icons", Icons.Default.Palette),
    SETTINGS("Settings", Icons.Default.Settings),
    APP_UPDATES("App Updates", Icons.Default.SystemUpdate),
    APP_INFO("App Info", Icons.Default.Info),
    FEEDBACK("Feedback", Icons.Default.Chat)
}

@Composable
fun NetisDrawerSheet(
    selectedDestination: DrawerDestination,
    onDestinationSelected: (DrawerDestination) -> Unit,
    hasUpdateAvailable: Boolean,
    currentIcon: AppIconOption = AppIconOption.DEFAULT,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("netis_navigation_drawer"),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 16.dp)
        ) {
            // Header with Netis Gradient & Router identity
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NetisBluePrimary, NetisBlueDark)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = currentIcon.previewResId),
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(38.dp)
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Column {
                            Text(
                                text = "WiFi Router App",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Universal Gateway Manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gateway Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NetisSuccess, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WiFi Gateway • Online",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Router Web Portal Item (192.168.1.1)
            NavigationDrawerItem(
                label = {
                    Text(
                        text = DrawerDestination.ROUTER_WEB.title,
                        fontWeight = if (selectedDestination == DrawerDestination.ROUTER_WEB) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = DrawerDestination.ROUTER_WEB.icon,
                        contentDescription = null
                    )
                },
                badge = {
                    Badge(
                        containerColor = NetisSuccess,
                        contentColor = Color.Black
                    ) {
                        Text("Live", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selected = selectedDestination == DrawerDestination.ROUTER_WEB,
                onClick = { onDestinationSelected(DrawerDestination.ROUTER_WEB) },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("drawer_item_router_web"),
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // Main Native Dashboard Item
            NavigationDrawerItem(
                label = {
                    Text(
                        text = DrawerDestination.DASHBOARD.title,
                        fontWeight = if (selectedDestination == DrawerDestination.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = DrawerDestination.DASHBOARD.icon,
                        contentDescription = null
                    )
                },
                selected = selectedDestination == DrawerDestination.DASHBOARD,
                onClick = { onDestinationSelected(DrawerDestination.DASHBOARD) },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("drawer_item_dashboard"),
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Sections in Required Order:
            // 1) APP ICONS (Dedicated UI Section)
            // 2) SETTINGS
            // 3) APP UPDATES
            // 4) APP INFO
            // 5) FEEDBACK
            val orderedDestinations = listOf(
                DrawerDestination.APP_ICONS,
                DrawerDestination.SETTINGS,
                DrawerDestination.APP_UPDATES,
                DrawerDestination.APP_INFO,
                DrawerDestination.FEEDBACK
            )

            orderedDestinations.forEach { destination ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = destination.title,
                            fontWeight = if (selectedDestination == destination) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null
                        )
                    },
                    badge = {
                        if (destination == DrawerDestination.APP_UPDATES) {
                            AnimatedVisibility(visible = hasUpdateAvailable) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("New", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (destination == DrawerDestination.APP_ICONS) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ) {
                                Text("New", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    selected = selectedDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                        .testTag("drawer_item_${destination.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Footer
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                Text(
                    text = "WiFi Router App v1.0.3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "Crafted by Arafath",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
