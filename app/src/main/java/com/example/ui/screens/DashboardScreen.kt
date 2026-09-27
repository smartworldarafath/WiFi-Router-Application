package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.NetisAiAssistant
import com.example.data.ConnectedDevice
import com.example.data.QosRule
import com.example.data.RouterRepository
import com.example.ui.components.DockToggle
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.NetisBlueDark
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisDanger
import com.example.ui.theme.NetisPurple
import com.example.ui.theme.NetisSuccess
import com.example.ui.theme.NetisWarning
import kotlinx.coroutines.launch

enum class DashboardTab(val label: String) {
    STATUS("Status"),
    WIRELESS("Wireless"),
    DEVICES("Devices"),
    QOS("Bandwidth"),
    TOOLS("Tools & AI")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: RouterRepository,
    modifier: Modifier = Modifier,
    onOpenRouterWeb: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val wanInfo by repository.wanInfo.collectAsState()
    val wirelessSettings by repository.wirelessSettings.collectAsState()
    val devices by repository.devices.collectAsState()
    val qosEnabled by repository.qosEnabled.collectAsState()
    val qosRules by repository.qosRules.collectAsState()
    val pingResult by repository.pingResult.collectAsState()
    val speedTest by repository.speedTest.collectAsState()
    val isRebooting by repository.isRebooting.collectAsState()
    val rebootCountdown by repository.rebootCountdown.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentTab = DashboardTab.entries[selectedTabIndex]

    // Reboot Dialog
    var showRebootConfirmDialog by remember { mutableStateOf(false) }

    if (showRebootConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRebootConfirmDialog = false },
            title = { Text("Reboot Netis Router?") },
            text = { Text("The router will reboot. Internet connectivity and WiFi will temporarily be suspended for ~30 seconds.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRebootConfirmDialog = false
                        repository.rebootRouter {
                            Toast.makeText(context, "Netis Router rebooted successfully!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NetisDanger)
                ) {
                    Text("Reboot Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reboot in Progress Overlay Modal
    if (isRebooting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Rebooting Netis WF2409E...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "System restarting. Ready in $rebootCountdown seconds",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {}
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        // Router Web Dashboard Hero Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NetisBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = wanInfo.routerModel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Gateway: 192.168.1.1 • Uptime: ${wanInfo.uptime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reboot quick button
                    IconButton(
                        onClick = { showRebootConfirmDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NetisDanger.copy(alpha = 0.12f))
                            .testTag("router_reboot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reboot Router",
                            tint = NetisDanger
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Throughput live pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download pill
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = NetisCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Downlink",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${wanInfo.currentDownloadSpeedMbps} Mbps",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NetisCyanAccent
                                )
                            }
                        }
                    }

                    // Upload pill
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = NetisPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Uplink",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${wanInfo.currentUploadSpeedMbps} Mbps",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NetisPurple
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Launcher: Live Router Web Admin (192.168.1.1/login.html)
                Surface(
                    onClick = onOpenRouterWeb,
                    shape = RoundedCornerShape(12.dp),
                    color = NetisBluePrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NetisCyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_router_web_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = NetisCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Router Admin Portal",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Open full-screen Netis router control",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp
                                    ),
                                    color = NetisCyanAccent
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Web Portal",
                            tint = NetisCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Web Dashboard Scrollable Tabs
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            edgePadding = 8.dp
        ) {
            DashboardTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = tab.label,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        // Tab Content Display
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                DashboardTab.STATUS -> StatusTabView(wanInfo = wanInfo, wirelessSettings = wirelessSettings, devicesCount = devices.size)
                DashboardTab.WIRELESS -> WirelessTabView(wirelessSettings = wirelessSettings, onSave = { s, p, sec, ch, pwr, g, gs ->
                    repository.updateWirelessSettings(s, p, sec, ch, pwr, g, gs)
                    Toast.makeText(context, "Wireless settings applied successfully!", Toast.LENGTH_SHORT).show()
                })
                DashboardTab.DEVICES -> DevicesTabView(
                    devices = devices,
                    onToggleBlock = { repository.toggleDeviceBlock(it) },
                    onSetLimit = { id, lim -> repository.updateDeviceLimit(id, lim) },
                    onCopyMac = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "MAC address copied: $it", Toast.LENGTH_SHORT).show()
                    }
                )
                DashboardTab.QOS -> QosTabView(
                    qosEnabled = qosEnabled,
                    rules = qosRules,
                    onToggleQos = { repository.toggleQos(it) },
                    onDeleteRule = { repository.removeQosRule(it) },
                    onAddRule = { ip, name, down, up -> repository.addQosRule(ip, name, down, up) }
                )
                DashboardTab.TOOLS -> ToolsAndAiTabView(
                    pingResult = pingResult,
                    speedTest = speedTest,
                    onRunPing = { repository.runPingTest(it) },
                    onRunSpeedTest = { repository.runSpeedTest() },
                    ssid = wirelessSettings.ssid,
                    channel = wirelessSettings.channel,
                    devicesCount = devices.size,
                    downSpeed = wanInfo.currentDownloadSpeedMbps,
                    upSpeed = wanInfo.currentUploadSpeedMbps
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: STATUS
// -------------------------------------------------------------
@Composable
fun StatusTabView(
    wanInfo: com.example.data.WanInfo,
    wirelessSettings: com.example.data.WirelessSettings,
    devicesCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WAN Connection Parameters Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WAN Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NetisSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = wanInfo.status,
                                color = NetisSuccess,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow("Connection Type", wanInfo.connectionType)
                    InfoRow("IP Address", wanInfo.ipAddress)
                    InfoRow("Subnet Mask", wanInfo.subnetMask)
                    InfoRow("Default Gateway", wanInfo.defaultGateway)
                    InfoRow("Primary DNS", wanInfo.primaryDns)
                    InfoRow("Secondary DNS", wanInfo.secondaryDns)
                }
            }
        }

        // Wireless Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Wireless 2.4GHz Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow("Radio Status", if (wirelessSettings.radioEnabled) "Enabled" else "Disabled")
                    InfoRow("Network Name (SSID)", wirelessSettings.ssid)
                    InfoRow("Channel Mode", wirelessSettings.channel)
                    InfoRow("Security Mode", wirelessSettings.securityMode)
                    InfoRow("Active DHCP Clients", "$devicesCount Devices Connected")
                }
            }
        }

        // System & Firmware Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hardware & Firmware Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow("Model", wanInfo.routerModel)
                    InfoRow("Hardware Version", wanInfo.hardwareVersion)
                    InfoRow("Firmware Version", wanInfo.firmwareVersion)
                    InfoRow("System Uptime", wanInfo.uptime)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// -------------------------------------------------------------
// TAB 2: WIRELESS SETTINGS
// -------------------------------------------------------------
@Composable
fun WirelessTabView(
    wirelessSettings: com.example.data.WirelessSettings,
    onSave: (String, String, String, String, String, Boolean, String) -> Unit
) {
    var ssid by remember(wirelessSettings) { mutableStateOf(wirelessSettings.ssid) }
    var password by remember(wirelessSettings) { mutableStateOf(wirelessSettings.password) }
    var showPassword by remember { mutableStateOf(false) }
    var selectedChannel by remember(wirelessSettings) { mutableStateOf(wirelessSettings.channel) }
    var selectedSecurity by remember(wirelessSettings) { mutableStateOf(wirelessSettings.securityMode) }
    var selectedPower by remember(wirelessSettings) { mutableStateOf(wirelessSettings.transmitPower) }
    var guestEnabled by remember(wirelessSettings) { mutableStateOf(wirelessSettings.guestNetworkEnabled) }
    var guestSsid by remember(wirelessSettings) { mutableStateOf(wirelessSettings.guestSsid) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Wireless 2.4GHz Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // SSID Field
                    Text("Network Name (SSID)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    Text("WiFi Password", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Security Mode Picker
                    Text("Security Protocol", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WPA2-PSK (AES)", "WPA3-SAE", "Open").forEach { sec ->
                            val selected = selectedSecurity == sec
                            OutlinedButton(
                                onClick = { selectedSecurity = sec },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                                )
                            ) {
                                Text(sec.take(7), maxLines = 1, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Channel & Power
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Channel Selection", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = selectedChannel,
                                onValueChange = { selectedChannel = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Transmit Power", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = selectedPower,
                                onValueChange = { selectedPower = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Guest Network Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Guest Network",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Isolate visitors from your private LAN devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DockToggle(
                            checked = guestEnabled,
                            onCheckedChange = { guestEnabled = it },
                            testTag = "guest_network_dock_toggle"
                        )
                    }

                    if (guestEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Guest SSID", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = guestSsid,
                            onValueChange = { guestSsid = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Apply Button
        item {
            Button(
                onClick = {
                    onSave(ssid, password, selectedSecurity, selectedChannel, selectedPower, guestEnabled, guestSsid)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_wireless_settings_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Apply Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: CONNECTED DEVICES (DHCP CLIENTS)
// -------------------------------------------------------------
@Composable
fun DevicesTabView(
    devices: List<ConnectedDevice>,
    onToggleBlock: (String) -> Unit,
    onSetLimit: (String, Int) -> Unit,
    onCopyMac: (String) -> Unit
) {
    var limitDialogDevice by remember { mutableStateOf<ConnectedDevice?>(null) }
    var limitSliderValue by remember { mutableStateOf(5000f) }

    limitDialogDevice?.let { dev ->
        AlertDialog(
            onDismissRequest = { limitDialogDevice = null },
            title = { Text("Bandwidth Limit: ${dev.hostname}") },
            text = {
                Column {
                    Text("Set maximum download speed for this client:")
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (limitSliderValue.toInt() == 0) "Unlimited" else "${(limitSliderValue / 1000).toInt()} Mbps",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = limitSliderValue,
                        onValueChange = { limitSliderValue = it },
                        valueRange = 0f..50000f,
                        steps = 9
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetLimit(dev.id, limitSliderValue.toInt())
                        limitDialogDevice = null
                    }
                ) {
                    Text("Apply Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { limitDialogDevice = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connected DHCP Clients (${devices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live Speed Telemetry",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(devices, key = { it.id }) { device ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (device.isBlocked) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (device.isBlocked) NetisDanger.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (device.isWifi) Icons.Default.Wifi else Icons.Default.Lan,
                                    contentDescription = null,
                                    tint = if (device.isBlocked) NetisDanger else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = device.hostname,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${device.ipAddress} • ${if (device.isWifi) "${device.signalDbm} dBm" else "Gigabit LAN"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (device.isBlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NetisDanger.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "BLOCKED",
                                    color = NetisDanger,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // MAC and live traffic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onCopyMac(device.macAddress) }
                        ) {
                            Text(
                                text = "MAC: ${device.macAddress}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy MAC",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Speeds
                        Text(
                            text = "↓ ${String.format("%.1f", device.downloadSpeedKbps / 1024f)} MB/s  ↑ ${String.format("%.1f", device.uploadSpeedKbps / 1024f)} MB/s",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions: Block/Unblock & QoS Limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onToggleBlock(device.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (device.isBlocked) NetisSuccess else NetisDanger
                            )
                        ) {
                            Icon(
                                imageVector = if (device.isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (device.isBlocked) "Unblock" else "Block Device")
                        }

                        OutlinedButton(
                            onClick = {
                                limitSliderValue = if (device.bandwidthLimitKbps > 0) device.bandwidthLimitKbps.toFloat() else 5000f
                                limitDialogDevice = device
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QoS Limit")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: BANDWIDTH CONTROL (QoS)
// -------------------------------------------------------------
@Composable
fun QosTabView(
    qosEnabled: Boolean,
    rules: List<QosRule>,
    onToggleQos: (Boolean) -> Unit,
    onDeleteRule: (String) -> Unit,
    onAddRule: (String, String, Int, Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newIp by remember { mutableStateOf("192.168.1.115") }
    var newDeviceName by remember { mutableStateOf("Smart TV") }
    var maxDown by remember { mutableStateOf("8000") }
    var maxUp by remember { mutableStateOf("2000") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add QoS Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newIp,
                        onValueChange = { newIp = it },
                        label = { Text("Target IP Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDeviceName,
                        onValueChange = { newDeviceName = it },
                        label = { Text("Device Label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxDown,
                        onValueChange = { maxDown = it },
                        label = { Text("Max Download (Kbps)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxUp,
                        onValueChange = { maxUp = it },
                        label = { Text("Max Upload (Kbps)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val down = maxDown.toIntOrNull() ?: 5000
                        val up = maxUp.toIntOrNull() ?: 1000
                        onAddRule(newIp, newDeviceName, down, up)
                        showAddDialog = false
                    }
                ) {
                    Text("Add Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master QoS Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bandwidth Control (QoS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prioritize traffic and allocate bandwidth caps across clients.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DockToggle(
                        checked = qosEnabled,
                        onCheckedChange = onToggleQos,
                        testTag = "qos_master_dock_toggle"
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QoS Rules List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Rule")
                }
            }
        }

        items(rules, key = { it.id }) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = rule.deviceName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "IP: ${rule.targetIp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Max Down: ${rule.maxDownloadKbps} Kbps | Max Up: ${rule.maxUploadKbps} Kbps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = { onDeleteRule(rule.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rule",
                            tint = NetisDanger
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 5: TOOLS & NETIS AI DIAGNOSTICS
// -------------------------------------------------------------
@Composable
fun ToolsAndAiTabView(
    pingResult: com.example.data.PingResult,
    speedTest: com.example.data.SpeedTestMetrics,
    onRunPing: (String) -> Unit,
    onRunSpeedTest: () -> Unit,
    ssid: String,
    channel: String,
    devicesCount: Int,
    downSpeed: Float,
    upSpeed: Float
) {
    val scope = rememberCoroutineScope()
    val aiAssistant = remember { NetisAiAssistant() }

    var targetPingHost by remember { mutableStateOf("8.8.8.8") }
    var aiAnalysisText by remember { mutableStateOf<String?>(null) }
    var isAiAnalyzing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section 1: Netis AI Diagnostics (Gemini Powered)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NetisCyanAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Netis AI Diagnostics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Powered by Gemini 3.1 Pro (Thinking Mode)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isAiAnalyzing = true
                                scope.launch {
                                    val result = aiAssistant.analyzeNetwork(
                                        ssid = ssid,
                                        currentChannel = channel,
                                        connectedDevicesCount = devicesCount,
                                        downSpeedMbps = downSpeed,
                                        upSpeedMbps = upSpeed
                                    )
                                    aiAnalysisText = result.getOrNull()
                                    isAiAnalyzing = false
                                }
                            },
                            enabled = !isAiAnalyzing,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isAiAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Analyze")
                            }
                        }
                    }

                    aiAnalysisText?.let { analysis ->
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = analysis,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section 2: Live Speedometer Test
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Internet Speed Test",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeDisplaySpeed = if (speedTest.phase.contains("Upload")) speedTest.uploadMbps else speedTest.downloadMbps

                    SpeedometerGauge(
                        speedMbps = activeDisplaySpeed,
                        maxSpeedMbps = 100f,
                        phaseText = speedTest.phase
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Download", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${speedTest.downloadMbps} Mbps",
                                fontWeight = FontWeight.Bold,
                                color = NetisCyanAccent
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upload", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${speedTest.uploadMbps} Mbps",
                                fontWeight = FontWeight.Bold,
                                color = NetisPurple
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ping / Jitter", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${speedTest.pingMs.toInt()} ms",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onRunSpeedTest,
                        enabled = !speedTest.isTesting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (speedTest.isTesting) "Testing Network Speed..." else "Start Speed Test")
                    }
                }
            }
        }

        // Section 3: Ping Diagnostics Tool
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ping & Latency Diagnostics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = targetPingHost,
                            onValueChange = { targetPingHost = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text("Target IP / Domain") }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { onRunPing(targetPingHost) },
                            enabled = !pingResult.isRunning,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (pingResult.isRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Ping")
                            }
                        }
                    }

                    if (pingResult.consoleLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp)
                        ) {
                            Column {
                                pingResult.consoleLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        color = Color(0xFF38BDF8),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
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
