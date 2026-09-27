package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class DetectedRouterInfo(
    val isWifiConnected: Boolean,
    val ssid: String?,
    val gatewayIp: String,
    val webUrl: String,
    val brandName: String,
    val detectionSummary: String
)

/**
 * High-performance, crash-safe router gateway detector.
 * Automatically identifies connected Wi-Fi networks, default router gateways,
 * and router brands without freezing the UI or dropping frames.
 */
object RouterDetectionManager {

    suspend fun detectConnectedRouter(context: Context): DetectedRouterInfo? = withContext(Dispatchers.IO) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return@withContext null

            val activeNetwork = cm.activeNetwork ?: return@withContext null
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return@withContext null
            val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

            // Gracefully ignore if not on Wi-Fi (e.g. mobile data only)
            if (!isWifi) {
                return@withContext null
            }

            val linkProperties = cm.getLinkProperties(activeNetwork)
            var gatewayIp: String? = null

            // 1. Modern Android LinkProperties default route gateway
            try {
                gatewayIp = linkProperties?.routes?.firstOrNull {
                    it.isDefaultRoute && it.hasGateway()
                }?.gateway?.hostAddress
            } catch (_: Exception) {}

            // 2. WifiManager DHCP fallback
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (gatewayIp.isNullOrBlank() || gatewayIp == "0.0.0.0") {
                try {
                    val dhcp = wifiManager?.dhcpInfo?.gateway
                    if (dhcp != null && dhcp != 0) {
                        gatewayIp = intToIp(dhcp)
                    }
                } catch (_: Exception) {}
            }

            // 3. Fallback standard local gateway if connected to Wi-Fi but IP was masked
            if (gatewayIp.isNullOrBlank() || gatewayIp == "0.0.0.0") {
                gatewayIp = "192.168.1.1"
            }

            // Safely retrieve SSID if available
            var ssid: String? = null
            try {
                val connectionInfo = wifiManager?.connectionInfo
                val rawSsid = connectionInfo?.ssid
                if (!rawSsid.isNullOrBlank() &&
                    rawSsid != "<unknown ssid>" &&
                    rawSsid != "\"<unknown ssid>\""
                ) {
                    ssid = rawSsid.trim().trim('"')
                }
            } catch (_: Exception) {}

            // Resolve Brand and Login Web Address
            val (brand, targetUrl) = resolveBrandAndUrl(ssid, gatewayIp)

            val summary = if (!ssid.isNullOrBlank()) {
                "Wi-Fi: $ssid • Gateway: $gatewayIp"
            } else {
                "Connected Gateway: $gatewayIp"
            }

            DetectedRouterInfo(
                isWifiConnected = true,
                ssid = ssid,
                gatewayIp = gatewayIp,
                webUrl = targetUrl,
                brandName = brand,
                detectionSummary = summary
            )
        } catch (_: Exception) {
            // Never crash the app under any unexpected network state
            null
        }
    }

    private fun resolveBrandAndUrl(ssid: String?, gatewayIp: String): Pair<String, String> {
        val s = ssid?.lowercase() ?: ""
        return when {
            s.contains("tp-link") || s.contains("tplink") || s.contains("archer") -> {
                "TP-Link" to "http://tplinkwifi.net"
            }
            s.contains("cudy") -> {
                "Cudy" to "http://cudy.net"
            }
            s.contains("mercusys") -> {
                "Mercusys" to "http://mwlogin.net"
            }
            s.contains("tenda") -> {
                "Tenda" to "http://tendawifi.com"
            }
            s.contains("netis") -> {
                "Netis" to "http://$gatewayIp/login.html"
            }
            s.contains("d-link") || s.contains("dlink") || s.contains("dir-") -> {
                "D-Link" to "http://dlinkrouter.local"
            }
            s.contains("asus") || s.contains("rt-ax") || s.contains("rt-ac") -> {
                "ASUS" to "http://router.asus.com"
            }
            s.contains("xiaomi") || s.contains("miwifi") || s.contains("redmi") -> {
                "Xiaomi" to "http://miwifi.com"
            }
            s.contains("netgear") || s.contains("orbi") || s.contains("nighthawk") -> {
                "Netgear" to "http://routerlogin.net"
            }
            s.contains("linksys") || s.contains("velop") -> {
                "Linksys" to "http://myrouter.local"
            }
            s.contains("huawei") || s.contains("hilink") -> {
                "Huawei" to "http://$gatewayIp"
            }
            s.contains("totolink") -> {
                "Totolink" to "http://itotolink.net"
            }
            s.contains("fritz") -> {
                "AVM FRITZ!Box" to "http://fritz.box"
            }
            else -> {
                // If brand is not in SSID, infer from standard gateway IP
                val brand = when (gatewayIp) {
                    "192.168.1.1" -> "Netis / Router Gateway"
                    "192.168.0.1" -> "Router Gateway (192.168.0.1)"
                    "192.168.10.1" -> "Cudy / Router Gateway"
                    "192.168.31.1" -> "Xiaomi Gateway"
                    "192.168.50.1" -> "ASUS Gateway"
                    "192.168.88.1" -> "MikroTik Gateway"
                    "10.0.0.1" -> "Gateway (10.0.0.1)"
                    else -> "Router Gateway"
                }
                val url = if (gatewayIp == "192.168.1.1") {
                    "http://192.168.1.1/login.html"
                } else {
                    "http://$gatewayIp/"
                }
                brand to url
            }
        }
    }

    private fun intToIp(ipInt: Int): String {
        return (ipInt and 0xFF).toString() + "." +
                ((ipInt shr 8) and 0xFF) + "." +
                ((ipInt shr 16) and 0xFF) + "." +
                ((ipInt shr 24) and 0xFF)
    }
}
