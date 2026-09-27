package com.example.data

data class WanInfo(
    val connectionType: String = "DHCP (Dynamic IP)",
    val ipAddress: String = "103.145.72.18",
    val subnetMask: String = "255.255.255.0",
    val defaultGateway: String = "103.145.72.1",
    val primaryDns: String = "8.8.8.8",
    val secondaryDns: String = "1.1.1.1",
    val status: String = "Connected",
    val uptime: String = "4d 18h 32m",
    val routerModel: String = "Netis WF2409E 300Mbps Wireless N",
    val hardwareVersion: String = "V1.0",
    val firmwareVersion: String = "V2.4.42116",
    val currentDownloadSpeedMbps: Float = 14.8f,
    val currentUploadSpeedMbps: Float = 3.6f
)

data class WirelessSettings(
    val radioEnabled: Boolean = true,
    val ssid: String = "netis_2.4G_E8F0",
    val securityMode: String = "WPA2-PSK (AES)",
    val password: String = "netis@wifi2026",
    val channel: String = "Auto (Current: Ch 6)",
    val channelWidth: String = "40 MHz",
    val transmitPower: String = "100% (High)",
    val guestNetworkEnabled: Boolean = false,
    val guestSsid: String = "netis_Guest",
    val guestPassword: String = "guest@1234",
    val broadcastSsid: Boolean = true
)

data class ConnectedDevice(
    val id: String,
    val hostname: String,
    val ipAddress: String,
    val macAddress: String,
    val isWifi: Boolean,
    val signalDbm: Int, // -30 to -90
    val downloadSpeedKbps: Float,
    val uploadSpeedKbps: Float,
    val isBlocked: Boolean = false,
    val bandwidthLimitKbps: Int = 0 // 0 = unlimited
)

data class QosRule(
    val id: String,
    val targetIp: String,
    val deviceName: String,
    val maxDownloadKbps: Int,
    val maxUploadKbps: Int,
    val isEnabled: Boolean = true
)

data class PingResult(
    val targetHost: String,
    val isRunning: Boolean = false,
    val packetsTransmitted: Int = 0,
    val packetsReceived: Int = 0,
    val packetLossPercent: Int = 0,
    val minLatencyMs: Float = 0f,
    val avgLatencyMs: Float = 0f,
    val maxLatencyMs: Float = 0f,
    val consoleLogs: List<String> = emptyList()
)

data class SpeedTestMetrics(
    val isTesting: Boolean = false,
    val phase: String = "Idle", // Idle, Ping, Download, Upload, Complete
    val downloadMbps: Float = 0f,
    val uploadMbps: Float = 0f,
    val pingMs: Float = 0f,
    val jitterMs: Float = 0f,
    val progress: Float = 0f
)
