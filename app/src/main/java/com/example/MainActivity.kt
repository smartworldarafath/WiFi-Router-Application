package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.AppPreferences
import com.example.data.RouterDetectionManager
import com.example.data.RouterRepository
import com.example.performance.AppIconOption
import com.example.performance.DisplayRefreshInfo
import com.example.performance.IconSwitchManager
import com.example.performance.PerformanceMode
import com.example.performance.PerformanceMonitor
import com.example.performance.RefreshRateManager
import com.example.ui.components.DrawerDestination
import com.example.ui.components.ForcedUpdateDialog
import com.example.ui.components.NetisDrawerSheet
import com.example.ui.screens.AppInfoDrawerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedbackDrawerScreen
import com.example.ui.screens.RouterWebScreen
import com.example.ui.screens.SettingsDrawerScreen
import com.example.ui.screens.UpdatesDrawerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NetisBluePrimary
import com.example.update.GitHubRelease
import com.example.update.GitHubReleaseService
import com.example.update.UpdateState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import com.example.data.ThemeMode
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {

    private lateinit var routerRepository: RouterRepository
    private lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var appPreferences: AppPreferences
    private val releaseService = GitHubReleaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        routerRepository = RouterRepository(lifecycleScope)
        performanceMonitor = PerformanceMonitor(lifecycleScope)
        appPreferences = AppPreferences(this)

        setContent {
            val themeMode by appPreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(
                    repository = routerRepository,
                    performanceMonitor = performanceMonitor,
                    appPreferences = appPreferences,
                    releaseService = releaseService,
                    activity = this
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        performanceMonitor.stop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    repository: RouterRepository,
    performanceMonitor: PerformanceMonitor,
    appPreferences: AppPreferences,
    releaseService: GitHubReleaseService,
    activity: ComponentActivity
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentDestination by remember { mutableStateOf(DrawerDestination.ROUTER_WEB) }
    var currentPerformanceMode by remember { mutableStateOf(PerformanceMode.MEDIUM) }
    var currentIcon by remember { mutableStateOf(AppIconOption.DEFAULT) }
    var displayInfo by remember { mutableStateOf(RefreshRateManager.getDisplayRefreshInfo(context)) }

    // Update States
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var allReleases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var forcedUpdateRelease by remember { mutableStateOf<GitHubRelease?>(null) }

    // Load persisted settings & set up display refresh rate
    LaunchedEffect(Unit) {
        val savedMode = appPreferences.performanceModeFlow.firstOrNull() ?: PerformanceMode.MEDIUM
        currentPerformanceMode = savedMode
        repository.setPollingInterval(savedMode.pollingIntervalMs)
        performanceMonitor.start(savedMode.pollingIntervalMs)

        val savedTargetRate = appPreferences.targetRefreshRateFlow.firstOrNull() ?: 120
        RefreshRateManager.applyTargetRefreshRate(activity, targetRateHz = savedTargetRate)
        displayInfo = RefreshRateManager.getDisplayRefreshInfo(context)
        performanceMonitor.setTargetRefreshRate(savedTargetRate.toFloat())

        currentIcon = IconSwitchManager.getActiveIcon(context)

        // Automatically detect connected Wi-Fi router & configure gateway address
        scope.launch {
            val detected = RouterDetectionManager.detectConnectedRouter(context)
            if (detected != null && detected.webUrl.isNotBlank()) {
                appPreferences.saveRouterWebUrl(detected.webUrl)
                appPreferences.saveRouterIp(detected.gatewayIp)
            }
        }

        // Check for updates via GitHub Releases API on app load
        updateState = UpdateState.Checking
        val result = releaseService.fetchReleases()
        result.fold(
            onSuccess = { releases ->
                allReleases = releases
                val latest = releases.firstOrNull()
                if (latest != null && releaseService.isNewerVersion(latest.tagName)) {
                    updateState = UpdateState.UpdateAvailable(latest, releases)
                    // Trigger forced update modal popup on launch (home screen)
                    forcedUpdateRelease = latest
                } else {
                    updateState = UpdateState.UpToDate(GitHubReleaseService.CURRENT_APP_VERSION, releases)
                }
            },
            onFailure = { err ->
                val fallback = releaseService.getFallbackReleases()
                allReleases = fallback
                updateState = UpdateState.Error(
                    err.message ?: "Failed to reach GitHub Releases API",
                    fallback
                )
            }
        )
    }

    // Handle Back Press when drawer is open or in sub-destination
    BackHandler(enabled = drawerState.isOpen || (currentDestination != DrawerDestination.ROUTER_WEB && currentDestination != DrawerDestination.DASHBOARD)) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentDestination = DrawerDestination.ROUTER_WEB
        }
    }

    // Non-dismissible Forced Update Dialog
    forcedUpdateRelease?.let { release ->
        ForcedUpdateDialog(
            latestRelease = release,
            onUpdateClick = {
                // Deep-link directly into Updates section and auto-start download
                forcedUpdateRelease = null
                currentDestination = DrawerDestination.APP_UPDATES
                scope.launch {
                    releaseService.downloadApk(release).collect { st ->
                        updateState = st
                    }
                }
            }
        )
    }

    // Adaptive Layout: Check window width (Tablet vs Phone)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedTablet = maxWidth >= 700.dp

        if (isExpandedTablet) {
            // Tablet Layout: Permanent Navigation Rail / Drawer
            PermanentNavigationDrawer(
                drawerContent = {
                    NetisDrawerSheet(
                        selectedDestination = currentDestination,
                        onDestinationSelected = { dest ->
                            currentDestination = dest
                        },
                        hasUpdateAvailable = updateState is UpdateState.UpdateAvailable,
                        currentIcon = currentIcon
                    )
                }
            ) {
                AppScaffoldContent(
                    currentDestination = currentDestination,
                    onNavigateDestination = { dest -> currentDestination = dest },
                    onOpenDrawer = { /* Drawer is permanently visible on tablet */ },
                    isTablet = true,
                    repository = repository,
                    telemetryFlow = performanceMonitor.telemetry,
                    currentPerformanceMode = currentPerformanceMode,
                    currentIcon = currentIcon,
                    displayInfo = displayInfo,
                    onPerformanceModeChanged = { mode ->
                        currentPerformanceMode = mode
                        repository.setPollingInterval(mode.pollingIntervalMs)
                        performanceMonitor.start(mode.pollingIntervalMs)
                    },
                    onIconChanged = { icon -> currentIcon = icon },
                    appPreferences = appPreferences,
                    updateState = updateState,
                    allReleases = allReleases,
                    onCheckForUpdates = {
                        scope.launch {
                            updateState = UpdateState.Checking
                            val res = releaseService.fetchReleases()
                            res.fold(
                                onSuccess = { rels ->
                                    allReleases = rels
                                    val top = rels.firstOrNull()
                                    if (top != null && releaseService.isNewerVersion(top.tagName)) {
                                        updateState = UpdateState.UpdateAvailable(top, rels)
                                    } else {
                                        updateState = UpdateState.UpToDate(GitHubReleaseService.CURRENT_APP_VERSION, rels)
                                    }
                                },
                                onFailure = {
                                    val fb = releaseService.getFallbackReleases()
                                    allReleases = fb
                                    updateState = UpdateState.Error("Network notice", fb)
                                }
                            )
                        }
                    },
                    onStartDownload = { rel ->
                        scope.launch {
                            releaseService.downloadApk(rel).collect { st ->
                                updateState = st
                            }
                        }
                    }
                )
            }
        } else {
            // Phone / Compact Layout: Modal Navigation Drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    NetisDrawerSheet(
                        selectedDestination = currentDestination,
                        onDestinationSelected = { dest ->
                            currentDestination = dest
                            scope.launch { drawerState.close() }
                        },
                        hasUpdateAvailable = updateState is UpdateState.UpdateAvailable,
                        currentIcon = currentIcon
                    )
                }
            ) {
                AppScaffoldContent(
                    currentDestination = currentDestination,
                    onNavigateDestination = { dest -> currentDestination = dest },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    isTablet = false,
                    repository = repository,
                    telemetryFlow = performanceMonitor.telemetry,
                    currentPerformanceMode = currentPerformanceMode,
                    currentIcon = currentIcon,
                    displayInfo = displayInfo,
                    onPerformanceModeChanged = { mode ->
                        currentPerformanceMode = mode
                        repository.setPollingInterval(mode.pollingIntervalMs)
                        performanceMonitor.start(mode.pollingIntervalMs)
                    },
                    onIconChanged = { icon -> currentIcon = icon },
                    appPreferences = appPreferences,
                    updateState = updateState,
                    allReleases = allReleases,
                    onCheckForUpdates = {
                        scope.launch {
                            updateState = UpdateState.Checking
                            val res = releaseService.fetchReleases()
                            res.fold(
                                onSuccess = { rels ->
                                    allReleases = rels
                                    val top = rels.firstOrNull()
                                    if (top != null && releaseService.isNewerVersion(top.tagName)) {
                                        updateState = UpdateState.UpdateAvailable(top, rels)
                                    } else {
                                        updateState = UpdateState.UpToDate(GitHubReleaseService.CURRENT_APP_VERSION, rels)
                                    }
                                },
                                onFailure = {
                                    val fb = releaseService.getFallbackReleases()
                                    allReleases = fb
                                    updateState = UpdateState.Error("Network notice", fb)
                                }
                            )
                        }
                    },
                    onStartDownload = { rel ->
                        scope.launch {
                            releaseService.downloadApk(rel).collect { st ->
                                updateState = st
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffoldContent(
    currentDestination: DrawerDestination,
    onNavigateDestination: (DrawerDestination) -> Unit,
    onOpenDrawer: () -> Unit,
    isTablet: Boolean,
    repository: RouterRepository,
    telemetryFlow: kotlinx.coroutines.flow.StateFlow<com.example.performance.PerformanceTelemetry>,
    currentPerformanceMode: PerformanceMode,
    currentIcon: AppIconOption,
    displayInfo: DisplayRefreshInfo,
    onPerformanceModeChanged: (PerformanceMode) -> Unit,
    onIconChanged: (AppIconOption) -> Unit,
    appPreferences: AppPreferences,
    updateState: UpdateState,
    allReleases: List<GitHubRelease>,
    onCheckForUpdates: () -> Unit,
    onStartDownload: (GitHubRelease) -> Unit
) {
    var routerWebReloadTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination) {
                            DrawerDestination.ROUTER_WEB -> "Router Admin"
                            DrawerDestination.DASHBOARD -> "Netis Router"
                            DrawerDestination.SETTINGS -> "Settings"
                            DrawerDestination.APP_UPDATES -> "App Updates"
                            DrawerDestination.APP_INFO -> "App Info"
                            DrawerDestination.FEEDBACK -> "Feedback"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!isTablet) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Drawer"
                            )
                        }
                    }
                },
                actions = {
                    if (currentDestination == DrawerDestination.ROUTER_WEB) {
                        IconButton(
                            onClick = { routerWebReloadTrigger++ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Page",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onNavigateDestination(DrawerDestination.SETTINGS) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Router Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (currentDestination == DrawerDestination.DASHBOARD) {
                        Button(
                            onClick = { onNavigateDestination(DrawerDestination.ROUTER_WEB) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NetisBluePrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Router Admin", fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fluid screen transitions
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = 400f)) togetherWith
                            fadeOut(animationSpec = spring(stiffness = 400f))
                },
                label = "screenTransition"
            ) { destination ->
                when (destination) {
                    DrawerDestination.ROUTER_WEB -> RouterWebScreen(
                        appPreferences = appPreferences,
                        reloadTrigger = routerWebReloadTrigger,
                        onNavigateToDashboard = { onNavigateDestination(DrawerDestination.DASHBOARD) },
                        onNavigateToSettings = { onNavigateDestination(DrawerDestination.SETTINGS) }
                    )
                    DrawerDestination.DASHBOARD -> DashboardScreen(
                        repository = repository,
                        onOpenRouterWeb = { onNavigateDestination(DrawerDestination.ROUTER_WEB) }
                    )
                    DrawerDestination.SETTINGS -> SettingsDrawerScreen(
                        telemetryFlow = telemetryFlow,
                        currentPerformanceMode = currentPerformanceMode,
                        currentIcon = currentIcon,
                        displayInfo = displayInfo,
                        onPerformanceModeChanged = onPerformanceModeChanged,
                        onIconChanged = onIconChanged,
                        appPreferences = appPreferences
                    )
                    DrawerDestination.APP_UPDATES -> UpdatesDrawerScreen(
                        updateState = updateState,
                        allReleases = allReleases,
                        onCheckForUpdates = onCheckForUpdates,
                        onStartDownload = onStartDownload,
                        currentIcon = currentIcon
                    )
                    DrawerDestination.APP_INFO -> AppInfoDrawerScreen(
                        currentIcon = currentIcon
                    )
                    DrawerDestination.FEEDBACK -> FeedbackDrawerScreen()
                }
            }
        }
    }
}
