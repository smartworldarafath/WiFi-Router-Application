package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AppPreferences
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.ui.theme.NetisDanger
import com.example.ui.theme.NetisWarning

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RouterWebScreen(
    appPreferences: AppPreferences,
    modifier: Modifier = Modifier,
    reloadTrigger: Int = 0,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current

    val configuredUrl by appPreferences.routerWebUrlFlow.collectAsState(initial = "http://192.168.1.1/login.html")
    val isDesktopMode by appPreferences.desktopModeFlow.collectAsState(initial = false)

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(configuredUrl) }
    var pageTitle by remember { mutableStateOf("Router Admin") }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Sync if URL in preferences changed
    LaunchedEffect(configuredUrl) {
        if (configuredUrl != currentUrl) {
            currentUrl = configuredUrl
            hasError = false
            errorMessage = null
            webViewInstance?.loadUrl(configuredUrl)
        }
    }

    // Sync if desktop mode changed
    LaunchedEffect(isDesktopMode) {
        webViewInstance?.let { wv ->
            val ws = wv.settings
            if (isDesktopMode) {
                ws.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                ws.useWideViewPort = true
                ws.loadWithOverviewMode = true
            } else {
                ws.userAgentString = null
            }
            wv.reload()
        }
    }

    // External reload trigger (e.g. from top bar refresh action)
    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0) {
            hasError = false
            errorMessage = null
            webViewInstance?.reload()
        }
    }

    // In-app Back button handling (keeps user within web history)
    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Native Android WebView filling the full screen like an app
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("router_web_view"),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                        allowFileAccess = true
                        allowContentAccess = true
                        if (isDesktopMode) {
                            userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            url?.let { currentUrl = it }
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            url?.let { currentUrl = it }
                            pageTitle = view?.title ?: "Router Admin"
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                errorMessage = error?.description?.toString()
                                    ?: "Cannot connect to Router Gateway at $currentUrl"
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progress = newProgress / 100f
                            if (newProgress >= 100) {
                                isLoading = false
                            }
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            if (!title.isNullOrBlank()) {
                                pageTitle = title
                            }
                        }
                    }

                    loadUrl(currentUrl)
                    webViewInstance = this
                }
            },
            update = { wv ->
                webViewInstance = wv
            }
        )

        // Subtle loading indicator at the very top edge
        if (isLoading && progress < 1.0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.TopCenter),
                color = NetisCyanAccent,
                trackColor = Color.Transparent
            )
        }

        // Connection Error / Offline Netis Helper Overlay
        if (hasError) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(NetisWarning.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = NetisWarning,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cannot Reach Router Gateway",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Target: $currentUrl\n\nTo view this router page live, your phone must be connected to the router's WiFi network.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notice: $errorMessage",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = NetisDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Retry Button
                        Button(
                            onClick = {
                                hasError = false
                                errorMessage = null
                                webViewInstance?.loadUrl(currentUrl)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NetisBluePrimary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }

                        // Open Phone WiFi Settings
                        OutlinedButton(
                            onClick = {
                                try {
                                    val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                    context.startActivity(wifiIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Please open WiFi settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WiFi Settings")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Configure in Settings
                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configure Gateway in Settings")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Load Interactive Router Web Preview in WebView
                    OutlinedButton(
                        onClick = {
                            hasError = false
                            errorMessage = null
                            val simulatedHtml = generateRouterLoginHtml()
                            webViewInstance?.loadDataWithBaseURL(
                                "http://192.168.1.1/",
                                simulatedHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Router, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Router Web Interface Simulation")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onNavigateToDashboard
                    ) {
                        Text("Switch to Native App Dashboard")
                    }
                }
            }
        }
    }
}

/**
 * Returns an accurate simulated WiFi Router Web Dashboard & Login HTML
 * with CSS and interactive JavaScript. This ensures that even when offline
 * or testing in an emulator, the router web interface runs 100% inside the app!
 */
private fun generateRouterLoginHtml(): String {
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=2.0">
        <title>WiFi Router Admin - Login</title>
        <style>
            * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
            body {
                background: linear-gradient(135deg, #0d223a 0%, #071526 100%);
                color: #ffffff;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                padding: 20px;
            }
            .container {
                width: 100%;
                max-width: 440px;
                background: #112845;
                border-radius: 16px;
                box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(0, 198, 255, 0.2);
                overflow: hidden;
            }
            .header {
                background: linear-gradient(90deg, #0072CE 0%, #00C6FF 100%);
                padding: 24px;
                text-align: center;
            }
            .logo {
                font-size: 32px;
                font-weight: 900;
                letter-spacing: 2px;
                text-transform: lowercase;
                color: #ffffff;
            }
            .sub-logo {
                font-size: 13px;
                color: rgba(255, 255, 255, 0.9);
                margin-top: 4px;
                letter-spacing: 1px;
            }
            .card-body {
                padding: 28px;
            }
            .ip-badge {
                display: inline-flex;
                align-items: center;
                background: rgba(0, 198, 255, 0.12);
                color: #00C6FF;
                padding: 6px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
                margin-bottom: 20px;
            }
            .ip-badge .dot {
                width: 8px;
                height: 8px;
                background: #00E676;
                border-radius: 50%;
                margin-right: 6px;
            }
            .form-group {
                margin-bottom: 20px;
                text-align: left;
            }
            label {
                display: block;
                font-size: 13px;
                font-weight: 600;
                color: #90CAF9;
                margin-bottom: 8px;
            }
            input[type="text"], input[type="password"], select {
                width: 100%;
                padding: 12px 16px;
                background: #0b1a2d;
                border: 1px solid rgba(255, 255, 255, 0.15);
                border-radius: 10px;
                color: #ffffff;
                font-size: 15px;
                outline: none;
                transition: border-color 0.2s, box-shadow 0.2s;
            }
            input:focus, select:focus {
                border-color: #00C6FF;
                box-shadow: 0 0 0 3px rgba(0, 198, 255, 0.25);
            }
            .btn-login {
                width: 100%;
                padding: 14px;
                background: linear-gradient(90deg, #0072CE 0%, #0099FF 100%);
                border: none;
                border-radius: 10px;
                color: white;
                font-size: 16px;
                font-weight: 700;
                cursor: pointer;
                transition: transform 0.1s, background 0.2s;
                margin-top: 10px;
            }
            .btn-login:active {
                transform: scale(0.98);
            }
            .login-success {
                display: none;
                background: rgba(0, 230, 118, 0.15);
                border: 1px solid #00E676;
                color: #00E676;
                padding: 14px;
                border-radius: 10px;
                margin-top: 16px;
                font-size: 14px;
                text-align: center;
            }
            .footer {
                text-align: center;
                padding: 16px;
                border-top: 1px solid rgba(255, 255, 255, 0.08);
                font-size: 12px;
                color: rgba(255, 255, 255, 0.5);
            }
            .quick-fill {
                display: flex;
                gap: 8px;
                margin-top: 10px;
            }
            .quick-btn {
                background: rgba(255, 255, 255, 0.08);
                border: 1px solid rgba(255, 255, 255, 0.15);
                color: #90CAF9;
                font-size: 11px;
                padding: 4px 8px;
                border-radius: 6px;
                cursor: pointer;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <div class="logo">WiFi Router</div>
                <div class="sub-logo">Web Management Portal</div>
            </div>
            <div class="card-body">
                <div class="ip-badge">
                    <span class="dot"></span> 192.168.1.1 Web Console
                </div>
                <form id="loginForm" onsubmit="handleLogin(event)">
                    <div class="form-group">
                        <label for="language">Language</label>
                        <select id="language">
                            <option value="en">English</option>
                            <option value="bn">বাংলা (Bengali)</option>
                            <option value="es">Español</option>
                            <option value="ru">Русский</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" value="admin" required autocomplete="username">
                        <div class="quick-fill">
                            <button type="button" class="quick-btn" onclick="fillCreds('admin', 'admin')">Admin/Admin</button>
                            <button type="button" class="quick-btn" onclick="fillCreds('user', 'user')">User/User</button>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" value="admin" required autocomplete="current-password">
                    </div>
                    <button type="submit" class="btn-login">Login to Router Web</button>
                </form>

                <div id="loginSuccess" class="loginSuccess">
                    ✓ Authenticated! Connected to Router Web Gateway (192.168.1.1).
                </div>
            </div>
            <div class="footer">
                WiFi Router App • Firmware v1.0.3
            </div>
        </div>

        <script>
            function fillCreds(user, pass) {
                document.getElementById('username').value = user;
                document.getElementById('password').value = pass;
            }
            function handleLogin(e) {
                e.preventDefault();
                var user = document.getElementById('username').value;
                var pass = document.getElementById('password').value;
                var box = document.getElementById('loginSuccess');
                box.style.display = 'block';
                box.innerHTML = '✓ Welcome, ' + user + '! Logged in to Router Web at 192.168.1.1/index.html';
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}
