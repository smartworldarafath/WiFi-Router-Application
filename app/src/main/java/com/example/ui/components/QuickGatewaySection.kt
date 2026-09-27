package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent

data class RouterGatewayItem(
    val name: String,
    val url: String,
    val ipBadge: String,
    val description: String = ""
)

/**
 * The 4 dedicated top primary router gateways required:
 * TP-Link, Cudy, Mercusys, Tenda.
 */
val PRIMARY_GATEWAYS = listOf(
    RouterGatewayItem(
        name = "TP-Link",
        url = "http://tplinkwifi.net",
        ipBadge = "192.168.0.1",
        description = "tplinkwifi.net"
    ),
    RouterGatewayItem(
        name = "Cudy",
        url = "http://cudy.net",
        ipBadge = "192.168.10.1",
        description = "cudy.net"
    ),
    RouterGatewayItem(
        name = "Mercusys",
        url = "http://mwlogin.net",
        ipBadge = "192.168.1.1",
        description = "mwlogin.net"
    ),
    RouterGatewayItem(
        name = "Tenda",
        url = "http://tendawifi.com",
        ipBadge = "192.168.0.1",
        description = "tendawifi.com"
    )
)

/**
 * Rich list of other router brands (strictly excludes TP-Link, Cudy, Mercusys, Tenda).
 */
val OTHER_ROUTER_GATEWAYS = listOf(
    RouterGatewayItem("Netis", "http://netis.cc", "192.168.1.1", "Wireless N & AC Routers"),
    RouterGatewayItem("D-Link", "http://dlinkrouter.local", "192.168.0.1", "DIR & EXO Series"),
    RouterGatewayItem("ASUS", "http://router.asus.com", "192.168.50.1", "ASUSWRT / RT-Series"),
    RouterGatewayItem("Xiaomi / Mi", "http://miwifi.com", "192.168.31.1", "Mi AIoT WiFi Routers"),
    RouterGatewayItem("Huawei", "http://192.168.8.1", "192.168.8.1", "WS & AX Series / HiLink"),
    RouterGatewayItem("Netgear", "http://routerlogin.net", "192.168.1.1", "Nighthawk & Orbi"),
    RouterGatewayItem("Linksys", "http://myrouter.local", "192.168.1.1", "Velop & Smart WiFi"),
    RouterGatewayItem("ZTE", "http://192.168.0.1", "192.168.0.1", "ZTE Link Broadband"),
    RouterGatewayItem("Totolink", "http://itotolink.net", "192.168.0.1", "Smart Wireless Router"),
    RouterGatewayItem("MikroTik", "http://192.168.88.1", "192.168.88.1", "RouterOS WebFig Console"),
    RouterGatewayItem("Google Nest / Wifi", "http://192.168.86.1", "192.168.86.1", "Google Nest Gateway"),
    RouterGatewayItem("Ubiquiti UniFi", "http://192.168.1.1", "192.168.1.1", "UniFi OS Gateway"),
    RouterGatewayItem("Synology", "http://router.synology.com", "192.168.1.1", "SRM Synology Router"),
    RouterGatewayItem("AVM FRITZ!Box", "http://fritz.box", "192.168.178.1", "FRITZ!OS Web GUI"),
    RouterGatewayItem("DrayTek", "http://192.168.1.1", "192.168.1.1", "Vigor Dual-WAN Series"),
    RouterGatewayItem("GL.iNet", "http://192.168.8.1", "192.168.8.1", "OpenWrt Mini Router"),
    RouterGatewayItem("Keenetic", "http://my.keenetic.net", "192.168.1.1", "KeeneticOS Gateway"),
    RouterGatewayItem("Belkin", "http://192.168.2.1", "192.168.2.1", "Belkin Router Setup"),
    RouterGatewayItem("ZyXEL", "http://192.168.1.1", "192.168.1.1", "ZyXEL Nebula & Armor"),
    RouterGatewayItem("FiberHome", "http://192.168.1.1", "192.168.1.1", "GPON Fiber Router")
)

/**
 * Quick Gateway section replacing all existing presets with:
 * TP-Link | Cudy | Mercusys | Tenda | Others
 *
 * Smooth animated card popup for "Others" with no frame drops.
 */
@Composable
fun QuickGatewaySection(
    currentUrl: String,
    onSelectGateway: (url: String, brandName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOthersPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Gateway Presets",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap to auto-fill address",
                style = MaterialTheme.typography.labelSmall,
                color = NetisCyanAccent.copy(alpha = 0.8f)
            )
        }

        // Dock-styled Quick Gateway Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. TP-Link, 2. Cudy, 3. Mercusys, 4. Tenda
            PRIMARY_GATEWAYS.forEach { item ->
                val isSelected = currentUrl == item.url || currentUrl.startsWith(item.url)
                val pillShape = RoundedCornerShape(12.dp)

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0066CC),
                                        Color(0xFF0099FF)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF131A26),
                                        Color(0xFF0E141E)
                                    )
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) NetisCyanAccent.copy(alpha = 0.7f) else Color(0xFF222C3E),
                            pillShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectGateway(item.url, item.name)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("quick_gateway_${item.name.lowercase().replace("-", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name,
                        color = if (isSelected) Color.White else Color(0xFFC7D3E3),
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }

            // 5. Others Pill
            val isOtherSelected = OTHER_ROUTER_GATEWAYS.any { currentUrl == it.url || currentUrl.startsWith(it.url) }
            val othersPillShape = RoundedCornerShape(12.dp)

            Box(
                modifier = Modifier
                    .clip(othersPillShape)
                    .background(
                        if (isOtherSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0066CC),
                                    Color(0xFF0099FF)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E2838),
                                    Color(0xFF151E2B)
                                )
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isOtherSelected) NetisCyanAccent.copy(alpha = 0.7f) else Color(0xFF324159),
                        othersPillShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showOthersPopup = true
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("quick_gateway_others"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Others",
                        color = if (isOtherSelected) Color.White else Color(0xFFD6E3F5),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Show other brands",
                        tint = if (isOtherSelected) Color.White else NetisCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // --- Others Mini Popup Window (Polished animated card modal) ---
    if (showOthersPopup) {
        OthersGatewayDialog(
            onDismiss = { showOthersPopup = false },
            onSelect = { url, brandName ->
                onSelectGateway(url, brandName)
                showOthersPopup = false
            }
        )
    }
}

/**
 * Animated Card-style Mini Popup Window displaying other router brands.
 * Purely hardware-accelerated Compose animation for zero frame drops.
 */
@Composable
private fun OthersGatewayDialog(
    onDismiss: () -> Unit,
    onSelect: (url: String, brandName: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            OTHER_ROUTER_GATEWAYS
        } else {
            val q = searchQuery.trim().lowercase()
            OTHER_ROUTER_GATEWAYS.filter {
                it.name.lowercase().contains(q) ||
                it.url.lowercase().contains(q) ||
                it.ipBadge.contains(q) ||
                it.description.lowercase().contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.90f, animationSpec = spring(stiffness = 500f)) + fadeIn(),
            exit = scaleOut(targetScale = 0.90f) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, Color(0xFF2B3A52), RoundedCornerShape(22.dp))
                    .testTag("others_gateway_popup"),
                color = Color(0xFF0F1522),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NetisBluePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = NetisCyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Other Router Brands",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${OTHER_ROUTER_GATEWAYS.size} verified gateway presets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF869AB5),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B2433))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFA0B2C9),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_router_brand_input"),
                        placeholder = {
                            Text("Search brand, IP or address...", fontSize = 13.sp, color = Color(0xFF6B7E98))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NetisCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF141C2A),
                            unfocusedContainerColor = Color(0xFF141C2A),
                            focusedBorderColor = NetisCyanAccent,
                            unfocusedBorderColor = Color(0xFF263347)
                        ),
                        singleLine = true
                    )

                    HorizontalDivider(color = Color(0xFF212B3B))

                    // Brand list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList, key = { it.name }) { brand ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(brand.url, brand.name)
                                    }
                                    .testTag("brand_item_${brand.name.lowercase().replace(" ", "_")}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF151D2C)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243144))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NetisBluePrimary.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = brand.name.take(2).uppercase(),
                                                color = NetisCyanAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = brand.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = brand.url,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp
                                                ),
                                                color = NetisCyanAccent
                                            )
                                        }
                                    }

                                    // IP Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF212B3B))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = brand.ipBadge,
                                            fontSize = 10.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF8EA4C0),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredList.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color(0xFF51637C),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No router brand found for '$searchQuery'",
                                        color = Color(0xFF869AB5),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
