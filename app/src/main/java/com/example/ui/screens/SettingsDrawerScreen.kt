package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.data.RouterDetectionManager
import com.example.data.ThemeMode
import com.example.performance.AppIconOption
import com.example.performance.DisplayRefreshInfo
import com.example.performance.IconSwitchManager
import com.example.performance.PerformanceMode
import com.example.performance.PerformanceTelemetry
import com.example.performance.RefreshRateManager
import com.example.ui.components.DockToggle
import com.example.ui.components.PerformanceLineChart
import com.example.ui.components.QuickGatewaySection
import com.example.ui.components.RefreshRateDockToggle
import com.example.ui.components.WaterWavePerformanceGraph
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisSuccess
import com.example.ui.theme.NetisWarning
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun SettingsDrawerScreen(
    telemetryFlow: StateFlow<PerformanceTelemetry>,
    currentPerformanceMode: PerformanceMode,
    currentIcon: AppIconOption,
    displayInfo: DisplayRefreshInfo,
    onPerformanceModeChanged: (PerformanceMode) -> Unit,
    onIconChanged: (AppIconOption) -> Unit,
    appPreferences: AppPreferences,
    modifier: Modifier = Modifier
) {
    SettingsDrawerScreenInternal(
        telemetryFlow = telemetryFlow,
        telemetry = null,
        currentPerformanceMode = currentPerformanceMode,
        currentIcon = currentIcon,
        displayInfo = displayInfo,
        onPerformanceModeChanged = onPerformanceModeChanged,
        onIconChanged = onIconChanged,
        appPreferences = appPreferences,
        modifier = modifier
    )
}

@Composable
fun SettingsDrawerScreen(
    telemetry: PerformanceTelemetry,
    currentPerformanceMode: PerformanceMode,
    currentIcon: AppIconOption,
    displayInfo: DisplayRefreshInfo,
    onPerformanceModeChanged: (PerformanceMode) -> Unit,
    onIconChanged: (AppIconOption) -> Unit,
    appPreferences: AppPreferences,
    modifier: Modifier = Modifier
) {
    SettingsDrawerScreenInternal(
        telemetryFlow = null,
        telemetry = telemetry,
        currentPerformanceMode = currentPerformanceMode,
        currentIcon = currentIcon,
        displayInfo = displayInfo,
        onPerformanceModeChanged = onPerformanceModeChanged,
        onIconChanged = onIconChanged,
        appPreferences = appPreferences,
        modifier = modifier
    )
}

@Composable
private fun SettingsDrawerScreenInternal(
    telemetryFlow: StateFlow<PerformanceTelemetry>?,
    telemetry: PerformanceTelemetry?,
    currentPerformanceMode: PerformanceMode,
    currentIcon: AppIconOption,
    displayInfo: DisplayRefreshInfo,
    onPerformanceModeChanged: (PerformanceMode) -> Unit,
    onIconChanged: (AppIconOption) -> Unit,
    appPreferences: AppPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val routerWebUrl by appPreferences.routerWebUrlFlow.collectAsState(initial = "http://192.168.1.1/login.html")
    val isDesktopMode by appPreferences.desktopModeFlow.collectAsState(initial = false)
    val currentThemeMode by appPreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val targetRefreshRate by appPreferences.targetRefreshRateFlow.collectAsState(initial = 120)
    var currentTargetRefreshRate by remember(targetRefreshRate) { mutableIntStateOf(targetRefreshRate) }
    var inputWebUrl by remember(routerWebUrl) { mutableStateOf(routerWebUrl) }
    var detectedSummary by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val detected = RouterDetectionManager.detectConnectedRouter(context)
        if (detected != null) {
            detectedSummary = detected.detectionSummary
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section 0: Router Gateway & Web Portal Settings
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Router Gateway & Web Portal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure router web admin address, view layout, and automatic gateway detection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Auto-Detect Connected Router Banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NetisCyanAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = NetisCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Auto-Detect Connected Router",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = detectedSummary ?: "Detects connected Wi-Fi router gateway automatically",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val detected = RouterDetectionManager.detectConnectedRouter(context)
                                        if (detected != null) {
                                            detectedSummary = detected.detectionSummary
                                            inputWebUrl = detected.webUrl
                                            appPreferences.saveRouterWebUrl(detected.webUrl)
                                            appPreferences.saveRouterIp(detected.gatewayIp)
                                            Toast.makeText(
                                                context,
                                                "Auto-detected: ${detected.brandName} (${detected.gatewayIp})",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No Wi-Fi router detected. Using manual URL.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NetisBluePrimary)
                            ) {
                                Text("Detect", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "Router Web Address",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = inputWebUrl,
                        onValueChange = { inputWebUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("router_address_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Language, contentDescription = null, tint = NetisCyanAccent)
                        },
                        label = { Text("Web Portal URL or IP") },
                        singleLine = true
                    )

                    // Quick Gateway section (TP-Link, Cudy, Mercusys, Tenda, Others)
                    QuickGatewaySection(
                        currentUrl = inputWebUrl,
                        onSelectGateway = { url, brandName ->
                            inputWebUrl = url
                            scope.launch {
                                appPreferences.saveRouterWebUrl(url)
                                Toast.makeText(context, "$brandName gateway address set: $url", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Button(
                        onClick = {
                            val formatted = if (!inputWebUrl.startsWith("http://") && !inputWebUrl.startsWith("https://")) {
                                "http://$inputWebUrl"
                            } else inputWebUrl
                            scope.launch {
                                appPreferences.saveRouterWebUrl(formatted)
                                Toast.makeText(context, "Router address updated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NetisBluePrimary)
                    ) {
                        Text("Save Address")
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Desktop Mode Switch - Dock Toggle Style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = NetisCyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Desktop Browser Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Display full desktop admin layout instead of mobile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DockToggle(
                            checked = isDesktopMode,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    appPreferences.saveDesktopMode(enabled)
                                }
                            },
                            testTag = "desktop_mode_dock_toggle"
                        )
                    }
                }
            }
        }

        // Section: Appearance & Theme (Dark Mode / Light Mode / System Default)
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Appearance & Theme",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select Dark Mode, Light Mode, or follow System Default theme.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // System Mode Card
                val isSystem = currentThemeMode == ThemeMode.SYSTEM
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            scope.launch {
                                appPreferences.saveThemeMode(ThemeMode.SYSTEM)
                                Toast.makeText(context, "System theme applied", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("theme_system"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        2.dp,
                        if (isSystem) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystem) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBrightness,
                            contentDescription = "System Theme",
                            tint = if (isSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "System",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto default",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Light Mode Card
                val isLight = currentThemeMode == ThemeMode.LIGHT
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            scope.launch {
                                appPreferences.saveThemeMode(ThemeMode.LIGHT)
                                Toast.makeText(context, "Light theme activated", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("theme_light"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        2.dp,
                        if (isLight) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Light Theme",
                            tint = if (isLight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Light",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bright clean",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Dark Mode Card
                val isDark = currentThemeMode == ThemeMode.DARK
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            scope.launch {
                                appPreferences.saveThemeMode(ThemeMode.DARK)
                                Toast.makeText(context, "Dark theme activated", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("theme_dark"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        2.dp,
                        if (isDark) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Dark Theme",
                            tint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dark",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Deep neon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 1: Real-time Performance & Telemetry
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "App Performance & Display",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Live Water Waves CPU/GPU utilization graph and high refresh rate telemetry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "monitor_water_wave_graph") {
            if (telemetryFlow != null) {
                WaterWavePerformanceGraph(telemetryFlow = telemetryFlow)
            } else if (telemetry != null) {
                WaterWavePerformanceGraph(telemetry = telemetry)
            }
        }

        // Display Mode Card (120Hz/144Hz/165Hz)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Refresh Rate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${displayInfo.currentRefreshRate.toInt()}Hz Active • ${displayInfo.displayModeName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Dock Toggle-Style Selector: 60Hz | 120Hz | 144Hz
                        RefreshRateDockToggle(
                            selectedRate = currentTargetRefreshRate,
                            onRateSelected = { rate ->
                                currentTargetRefreshRate = rate
                                scope.launch {
                                    appPreferences.saveTargetRefreshRate(rate)
                                    (context as? Activity)?.let { act ->
                                        RefreshRateManager.applyTargetRefreshRate(act, rate)
                                    }
                                    Toast.makeText(
                                        context,
                                        "${rate}Hz refresh rate applied",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Section 2: Performance Mode (Low, Medium, Boost)
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Performance Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Balances live router telemetry polling and UI animation framerates. Persists across restarts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PerformanceMode.entries.forEach { mode ->
                    val isSelected = currentPerformanceMode == mode
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(),
                        label = "modeBorder"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPerformanceModeChanged(mode)
                                scope.launch {
                                    appPreferences.savePerformanceMode(mode)
                                    (context as? Activity)?.let { activity ->
                                        val rate = if (mode == PerformanceMode.BOOST) 144 else currentTargetRefreshRate
                                        RefreshRateManager.applyTargetRefreshRate(
                                            activity,
                                            targetRateHz = rate
                                        )
                                    }
                                }
                            }
                            .testTag("mode_${mode.name.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, borderColor),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (mode == PerformanceMode.MEDIUM) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(Default)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: App Icon Switcher
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "UI Style & App Icon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your preferred app icon design. Preserves original visual size and proportions consistently across all screens.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "icon_selector_grid") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppIconOption.entries.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { iconOption ->
                            val isSelected = currentIcon == iconOption
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onIconChanged(iconOption)
                                        scope.launch {
                                            appPreferences.saveSelectedIcon(iconOption)
                                            val success = IconSwitchManager.setAppIcon(context, iconOption)
                                            if (success) {
                                                Toast.makeText(
                                                    context,
                                                    "Launcher icon set to ${iconOption.title}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                    .testTag("icon_${iconOption.id}"),
                                shape = RoundedCornerShape(16.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Icon Real Image & Preview with exact proportions and consistent sizing
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(iconOption.primaryColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = iconOption.previewResId),
                                            contentDescription = iconOption.title,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .aspectRatio(1f),
                                            contentScale = ContentScale.Fit
                                        )

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = iconOption.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = iconOption.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
