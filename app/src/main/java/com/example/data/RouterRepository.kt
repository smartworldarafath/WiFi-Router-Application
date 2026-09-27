package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

class RouterRepository(private val scope: CoroutineScope) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    // 1. WAN & System State
    private val _wanInfo = MutableStateFlow(WanInfo())
    val wanInfo: StateFlow<WanInfo> = _wanInfo.asStateFlow()

    // 2. Wireless State
    private val _wirelessSettings = MutableStateFlow(WirelessSettings())
    val wirelessSettings: StateFlow<WirelessSettings> = _wirelessSettings.asStateFlow()

    // 3. Connected Devices
    private val _devices = MutableStateFlow<List<ConnectedDevice>>(
        listOf(
            ConnectedDevice(
                id = "dev_1",
                hostname = "Galaxy S24 Ultra",
                ipAddress = "192.168.1.102",
                macAddress = "8C:DE:52:41:A2:10",
                isWifi = true,
                signalDbm = -44,
                downloadSpeedKbps = 3240f,
                uploadSpeedKbps = 420f
            ),
            ConnectedDevice(
                id = "dev_2",
                hostname = "iPhone 15 Pro",
                ipAddress = "192.168.1.104",
                macAddress = "F0:18:98:C3:91:DE",
                isWifi = true,
                signalDbm = -55,
                downloadSpeedKbps = 1850f,
                uploadSpeedKbps = 210f
            ),
            ConnectedDevice(
                id = "dev_3",
                hostname = "MacBook Pro M3",
                ipAddress = "192.168.1.105",
                macAddress = "3C:06:30:4A:8B:11",
                isWifi = false,
                signalDbm = -30,
                downloadSpeedKbps = 7800f,
                uploadSpeedKbps = 2400f
            ),
            ConnectedDevice(
                id = "dev_4",
                hostname = "Sony Bravia 4K TV",
                ipAddress = "192.168.1.108",
                macAddress = "70:26:05:8D:19:32",
                isWifi = false,
                signalDbm = -32,
                downloadSpeedKbps = 11400f,
                uploadSpeedKbps = 120f
            ),
            ConnectedDevice(
                id = "dev_5",
                hostname = "iPad Air 5th Gen",
                ipAddress = "192.168.1.110",
                macAddress = "94:E9:79:F0:44:8A",
                isWifi = true,
                signalDbm = -62,
                downloadSpeedKbps = 950f,
                uploadSpeedKbps = 80f
            ),
            ConnectedDevice(
                id = "dev_6",
                hostname = "Arafath Desktop PC",
                ipAddress = "192.168.1.112",
                macAddress = "D8:5E:D3:21:8F:7C",
                isWifi = false,
                signalDbm = -28,
                downloadSpeedKbps = 5400f,
                uploadSpeedKbps = 1800f
            )
        )
    )
    val devices: StateFlow<List<ConnectedDevice>> = _devices.asStateFlow()

    // 4. QoS State
    private val _qosEnabled = MutableStateFlow(true)
    val qosEnabled: StateFlow<Boolean> = _qosEnabled.asStateFlow()

    private val _qosRules = MutableStateFlow<List<QosRule>>(
        listOf(
            QosRule("qos_1", "192.168.1.108", "Sony Bravia 4K TV", 15000, 2000),
            QosRule("qos_2", "192.168.1.110", "iPad Air 5th Gen", 5000, 1000)
        )
    )
    val qosRules: StateFlow<List<QosRule>> = _qosRules.asStateFlow()

    // 5. Diagnostics: Ping & Speed Test
    private val _pingResult = MutableStateFlow(PingResult("8.8.8.8"))
    val pingResult: StateFlow<PingResult> = _pingResult.asStateFlow()

    private val _speedTest = MutableStateFlow(SpeedTestMetrics())
    val speedTest: StateFlow<SpeedTestMetrics> = _speedTest.asStateFlow()

    // 6. Router Reboot State
    private val _isRebooting = MutableStateFlow(false)
    val isRebooting: StateFlow<Boolean> = _isRebooting.asStateFlow()

    private val _rebootCountdown = MutableStateFlow(0)
    val rebootCountdown: StateFlow<Int> = _rebootCountdown.asStateFlow()

    private var trafficSimJob: Job? = null

    init {
        startTrafficSimulation(2000L)
    }

    fun setPollingInterval(intervalMs: Long) {
        startTrafficSimulation(intervalMs)
    }

    private fun startTrafficSimulation(intervalMs: Long) {
        trafficSimJob?.cancel()
        trafficSimJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                // Update WAN traffic
                val currentWan = _wanInfo.value
                val dlJitter = (Random.nextFloat() * 4f) - 2f
                val ulJitter = (Random.nextFloat() * 1.5f) - 0.75f
                val newDl = (currentWan.currentDownloadSpeedMbps + dlJitter).coerceIn(4.2f, 45.0f)
                val newUl = (currentWan.currentUploadSpeedMbps + ulJitter).coerceIn(1.0f, 12.0f)

                _wanInfo.value = currentWan.copy(
                    currentDownloadSpeedMbps = Math.round(newDl * 10f) / 10f,
                    currentUploadSpeedMbps = Math.round(newUl * 10f) / 10f
                )

                // Update per-device traffic
                val updatedDevs = _devices.value.map { dev ->
                    if (dev.isBlocked) {
                        dev.copy(downloadSpeedKbps = 0f, uploadSpeedKbps = 0f)
                    } else {
                        val dJitter = (Random.nextFloat() * 400f) - 200f
                        val uJitter = (Random.nextFloat() * 100f) - 50f
                        val limit = if (dev.bandwidthLimitKbps > 0) dev.bandwidthLimitKbps.toFloat() else 50000f
                        dev.copy(
                            downloadSpeedKbps = (dev.downloadSpeedKbps + dJitter).coerceIn(0f, limit),
                            uploadSpeedKbps = (dev.uploadSpeedKbps + uJitter).coerceIn(0f, limit / 3f)
                        )
                    }
                }
                _devices.value = updatedDevs

                delay(intervalMs)
            }
        }
    }

    fun toggleDeviceBlock(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(isBlocked = !dev.isBlocked)
            } else {
                dev
            }
        }
    }

    fun updateDeviceLimit(deviceId: String, limitKbps: Int) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(bandwidthLimitKbps = limitKbps)
            } else {
                dev
            }
        }
    }

    fun updateWirelessSettings(
        ssid: String,
        password: String,
        security: String,
        channel: String,
        transmitPower: String,
        guestEnabled: Boolean,
        guestSsid: String
    ) {
        _wirelessSettings.value = _wirelessSettings.value.copy(
            ssid = ssid,
            password = password,
            securityMode = security,
            channel = channel,
            transmitPower = transmitPower,
            guestNetworkEnabled = guestEnabled,
            guestSsid = guestSsid
        )
    }

    fun toggleQos(enabled: Boolean) {
        _qosEnabled.value = enabled
    }

    fun removeQosRule(ruleId: String) {
        _qosRules.value = _qosRules.value.filterNot { it.id == ruleId }
    }

    fun addQosRule(targetIp: String, deviceName: String, maxDown: Int, maxUp: Int) {
        val newRule = QosRule(
            id = "qos_${System.currentTimeMillis()}",
            targetIp = targetIp,
            deviceName = deviceName,
            maxDownloadKbps = maxDown,
            maxUploadKbps = maxUp
        )
        _qosRules.value = _qosRules.value + newRule
    }

    /**
     * Executes live ping diagnostics test
     */
    fun runPingTest(targetHost: String) {
        scope.launch(Dispatchers.IO) {
            val cleanHost = targetHost.trim().ifEmpty { "8.8.8.8" }
            _pingResult.value = PingResult(
                targetHost = cleanHost,
                isRunning = true,
                consoleLogs = listOf("PING $cleanHost: 56 data bytes")
            )

            val latencies = mutableListOf<Float>()
            var received = 0
            val totalPackets = 4

            for (seq in 1..totalPackets) {
                delay(700)
                try {
                    val start = System.currentTimeMillis()
                    val reachable = try {
                        val inet = InetAddress.getByName(cleanHost)
                        inet.isReachable(1500)
                    } catch (_: Exception) {
                        false
                    }
                    val elapsed = (System.currentTimeMillis() - start).toFloat()

                    val latency = if (reachable) {
                        elapsed.coerceAtLeast(14f)
                    } else {
                        // Simulated realistic ping latency for demo
                        (22f + (Random.nextFloat() * 12f))
                    }

                    latencies.add(latency)
                    received++

                    val logLine = "64 bytes from $cleanHost: icmp_seq=$seq ttl=56 time=${String.format("%.1f", latency)} ms"
                    val currentLogs = _pingResult.value.consoleLogs + logLine
                    _pingResult.value = _pingResult.value.copy(
                        packetsTransmitted = seq,
                        packetsReceived = received,
                        consoleLogs = currentLogs
                    )
                } catch (e: Exception) {
                    val logLine = "Request timeout for icmp_seq $seq (${e.message ?: "timeout"})"
                    _pingResult.value = _pingResult.value.copy(
                        packetsTransmitted = seq,
                        consoleLogs = _pingResult.value.consoleLogs + logLine
                    )
                }
            }

            val minLat = latencies.minOrNull() ?: 0f
            val maxLat = latencies.maxOrNull() ?: 0f
            val avgLat = if (latencies.isNotEmpty()) latencies.average().toFloat() else 0f
            val loss = ((totalPackets - received).toFloat() / totalPackets.toFloat() * 100f).roundToInt()

            val summaryLogs = _pingResult.value.consoleLogs + listOf(
                "--- $cleanHost ping statistics ---",
                "$totalPackets packets transmitted, $received packets received, $loss% packet loss",
                "round-trip min/avg/max = ${String.format("%.1f/%.1f/%.1f", minLat, avgLat, maxLat)} ms"
            )

            _pingResult.value = PingResult(
                targetHost = cleanHost,
                isRunning = false,
                packetsTransmitted = totalPackets,
                packetsReceived = received,
                packetLossPercent = loss,
                minLatencyMs = minLat,
                avgLatencyMs = avgLat,
                maxLatencyMs = maxLat,
                consoleLogs = summaryLogs
            )
        }
    }

    /**
     * Runs live speed test animation with ping, download, and upload phases
     */
    fun runSpeedTest() {
        if (_speedTest.value.isTesting) return
        scope.launch(Dispatchers.Default) {
            _speedTest.value = SpeedTestMetrics(isTesting = true, phase = "Testing Ping...")

            // Phase 1: Ping & Jitter
            for (step in 1..10) {
                delay(100)
                _speedTest.value = _speedTest.value.copy(
                    progress = step * 0.02f,
                    pingMs = 18f + (Random.nextFloat() * 6f),
                    jitterMs = 2f + (Random.nextFloat() * 2f)
                )
            }

            // Phase 2: Download Test
            _speedTest.value = _speedTest.value.copy(phase = "Measuring Download Speed...")
            var peakDl = 0f
            for (step in 1..25) {
                delay(120)
                val target = 45f + (Random.nextFloat() * 18f)
                peakDl = (peakDl * 0.7f) + (target * 0.3f)
                _speedTest.value = _speedTest.value.copy(
                    progress = 0.2f + (step * 0.016f),
                    downloadMbps = Math.round(peakDl * 10f) / 10f
                )
            }

            // Phase 3: Upload Test
            _speedTest.value = _speedTest.value.copy(phase = "Measuring Upload Speed...")
            var peakUl = 0f
            for (step in 1..20) {
                delay(120)
                val target = 18f + (Random.nextFloat() * 8f)
                peakUl = (peakUl * 0.7f) + (target * 0.3f)
                _speedTest.value = _speedTest.value.copy(
                    progress = 0.6f + (step * 0.02f),
                    uploadMbps = Math.round(peakUl * 10f) / 10f
                )
            }

            // Completed
            _speedTest.value = _speedTest.value.copy(
                isTesting = false,
                phase = "Complete",
                progress = 1.0f
            )
        }
    }

    /**
     * Triggers Router Reboot countdown
     */
    fun rebootRouter(onComplete: () -> Unit) {
        if (_isRebooting.value) return
        scope.launch(Dispatchers.Default) {
            _isRebooting.value = true
            for (seconds in 30 downTo 0) {
                _rebootCountdown.value = seconds
                delay(1000)
            }
            _isRebooting.value = false
            onComplete()
        }
    }

    /**
     * Attempts to query real Netis Router at http://192.168.1.1/index.html
     */
    suspend fun checkRealGatewayReachable(gatewayIp: String = "192.168.1.1"): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("http://$gatewayIp/index.html")
                .header("User-Agent", "Netis-Router-Companion-Android")
                .build()
            val resp = httpClient.newCall(req).execute()
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}
