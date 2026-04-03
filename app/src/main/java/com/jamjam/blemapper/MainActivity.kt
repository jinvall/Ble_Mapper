package com.jamjam.blemapper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.bluetooth.le.ScanResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jamjam.blemapper.ble.BLEScannerService
import com.jamjam.blemapper.ui.theme.BleMapperTheme

data class SignalHistoryEntry(val uid: String, val source: String, val rssi: Int, val timestamp: Long)

class MainActivity : ComponentActivity() {

    private lateinit var bleScannerService: BLEScannerService
    private lateinit var wifiScannerService: com.jamjam.blemapper.ble.WifiScannerService

    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE
    )

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                startScanners()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bleScannerService = BLEScannerService(this)
        wifiScannerService = com.jamjam.blemapper.ble.WifiScannerService(this)

        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            startScanners()
        }

        setContent {
            BleMapperTheme {
                val showBle = remember { mutableStateOf(true) }
                val showWifi = remember { mutableStateOf(true) }
                val rssiThreshold = remember { mutableStateOf(-80) }
                val signalHistory = remember { mutableStateListOf<SignalHistoryEntry>() }

                LaunchedEffect(bleScannerService.devices, wifiScannerService.networks) {
                    val now = System.currentTimeMillis()
                    bleScannerService.devices.forEach { result ->
                        val uid = result.device.address ?: result.device.name ?: "unknown-ble"
                        signalHistory.add(SignalHistoryEntry(uid, "BLE", result.rssi, now))
                    }
                    wifiScannerService.networks.forEach { network ->
                        signalHistory.add(SignalHistoryEntry(network.uid, "WiFi", network.rssi, now))
                    }
                    if (signalHistory.size > 1000) {
                        signalHistory.removeRange(0, signalHistory.size - 1000)
                    }
                }

                val flashedBle = bleScannerService.devices.filter { it.rssi >= rssiThreshold.value }
                val flashedWifi = wifiScannerService.networks.filter { it.rssi >= rssiThreshold.value }

                Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("BLE", color = Color.Cyan)
                                    Switch(checked = showBle.value, onCheckedChange = { showBle.value = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Wi-Fi", color = Color.Magenta)
                                    Switch(checked = showWifi.value, onCheckedChange = { showWifi.value = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("RSSI >=", style = MaterialTheme.typography.bodyMedium)
                                    Text(rssiThreshold.value.toString(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Min RSSI", style = MaterialTheme.typography.bodySmall)
                                Slider(
                                    value = (rssiThreshold.value + 100) / 70f,
                                    onValueChange = {
                                        rssiThreshold.value = ((it * 70f) - 100f).toInt().coerceIn(-100, -30)
                                    },
                                    valueRange = 0f..1f,
                                    steps = 70,
                                    modifier = Modifier.weight(1f).padding(start = 16.dp, end = 16.dp)
                                )
                                Text("${rssiThreshold.value} dBm", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Text("Signal Radar", style = MaterialTheme.typography.titleLarge)
                        SignalRadar(
                            bleDevices = if (showBle.value) flashedBle else emptyList(),
                            wifiNetworks = if (showWifi.value) flashedWifi else emptyList(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .padding(vertical = 16.dp)
                        )

                        Text("History size: ${signalHistory.size}", style = MaterialTheme.typography.bodySmall)
                        Text("BLE Devices", style = MaterialTheme.typography.titleLarge)
                        DeviceList(if (showBle.value) flashedBle else emptyList(), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Wi-Fi Networks (Root scan)", style = MaterialTheme.typography.titleLarge)
                        WifiList(if (showWifi.value) flashedWifi else emptyList(), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleScannerService.stop()
        wifiScannerService.stop()
    }

    private fun startScanners() {
        bleScannerService.start()
        wifiScannerService.start()
    }

    private fun Context.hasAllPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
}

@Composable
fun WifiList(networks: List<com.jamjam.blemapper.ble.WifiNetwork>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        items(networks) { network ->
            WifiRow(network)
        }
    }
}

@Composable
fun WifiRow(network: com.jamjam.blemapper.ble.WifiNetwork) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = network.ssid.ifBlank { "Hidden SSID" }, style = MaterialTheme.typography.bodyLarge)
        Text(text = network.bssid, style = MaterialTheme.typography.bodyMedium)
        Text(text = "RSSI: ${network.rssi} dBm", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun DeviceList(devices: List<ScanResult>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        items(devices) { result ->
            DeviceRow(result)
        }
    }
}

private data class RadarSample(val id: String, val angle: Float, val ratio: Float, val color: Color)

@Composable
fun SignalRadar(
    bleDevices: List<ScanResult>,
    wifiNetworks: List<com.jamjam.blemapper.ble.WifiNetwork>,
    modifier: Modifier = Modifier
) {
    val radarEntries = buildList {
        bleDevices.forEach { result ->
            val id = result.device.address ?: result.device.name ?: "ble"
            val angle = (kotlin.math.abs(id.hashCode()) % 360).toFloat()
            val ratio = rssiToRatio(result.rssi)
            add(RadarSample(id, angle, ratio, Color.Cyan))
        }
        wifiNetworks.forEach { network ->
            val id = network.bssid.ifEmpty { network.ssid.ifEmpty { "wifi" } }
            val angle = (kotlin.math.abs(id.hashCode()) % 360).toFloat()
            val ratio = rssiToRatio(network.rssi)
            add(RadarSample(id, angle, ratio, Color.Magenta))
        }
    }

    val positionTrails = remember { mutableStateMapOf<String, MutableList<Pair<Float, Float>>>() }
    LaunchedEffect(radarEntries) {
        val keepKeys = radarEntries.map { it.id }.toSet()
        positionTrails.keys.filterNot { it in keepKeys }.forEach { positionTrails.remove(it) }

        radarEntries.forEach { sample ->
            val currentList = positionTrails.getOrPut(sample.id) { mutableListOf() }
            currentList.add(sample.angle to sample.ratio)
            while (currentList.size > 6) currentList.removeFirst()
            positionTrails[sample.id] = currentList
        }
    }

    val transition = rememberInfiniteTransition()
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = minOf(centerX, centerY) * 0.9f

        drawRect(color = Color.Black)

        val ringColor = Color(0xFF530F7A)
        val ringStroke = Stroke(width = 2f, cap = StrokeCap.Round)

        (1..4).forEach { ring ->
            drawCircle(
                color = ringColor,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                radius = maxRadius * ring / 4f,
                style = ringStroke
            )
        }

        drawCircle(
            color = Color(0xFF7B2EF6),
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            radius = maxRadius,
            style = Stroke(width = 6f)
        )

        rotate(degrees = sweepAngle, pivot = androidx.compose.ui.geometry.Offset(centerX, centerY)) {
            drawLine(
                color = Color(0xFF9C4CDE),
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(centerX, centerY - maxRadius),
                strokeWidth = 4f
            )
        }

        positionTrails.forEach { (_, points) ->
            if (points.size > 1) {
                points.windowed(2).forEachIndexed { index, pair ->
                    val (angleA, ratioA) = pair[0]
                    val (angleB, ratioB) = pair[1]
                    val xA = centerX + maxRadius * ratioA * kotlin.math.cos(Math.toRadians(angleA.toDouble())).toFloat()
                    val yA = centerY + maxRadius * ratioA * kotlin.math.sin(Math.toRadians(angleA.toDouble())).toFloat()
                    val xB = centerX + maxRadius * ratioB * kotlin.math.cos(Math.toRadians(angleB.toDouble())).toFloat()
                    val yB = centerY + maxRadius * ratioB * kotlin.math.sin(Math.toRadians(angleB.toDouble())).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = (index + 1) / (points.size.toFloat() * 2f)),
                        start = androidx.compose.ui.geometry.Offset(xA, yA),
                        end = androidx.compose.ui.geometry.Offset(xB, yB),
                        strokeWidth = 2f
                    )
                }
            }
        }

        radarEntries.forEach { sample ->
            val rad = Math.toRadians(sample.angle.toDouble())
            val x = centerX + maxRadius * sample.ratio * kotlin.math.cos(rad).toFloat()
            val y = centerY + maxRadius * sample.ratio * kotlin.math.sin(rad).toFloat()

            val trail = positionTrails[sample.id] ?: emptyList()
            val direction = if (trail.size >= 2) {
                val prevRatio = trail[trail.lastIndex - 1].second
                when {
                    sample.ratio < prevRatio - 0.02f -> "incoming"
                    sample.ratio > prevRatio + 0.02f -> "departing"
                    else -> "steady"
                }
            } else "steady"

            val pointColor = when (direction) {
                "incoming" -> Color.Green
                "departing" -> Color.Red
                else -> sample.color
            }

            val markerAngle = if (direction == "incoming") -10f else if (direction == "departing") 10f else 0f
            val markerX = x + 12f * kotlin.math.cos(Math.toRadians((sample.angle + markerAngle).toDouble())).toFloat()
            val markerY = y + 12f * kotlin.math.sin(Math.toRadians((sample.angle + markerAngle).toDouble())).toFloat()

            drawCircle(
                color = pointColor.copy(alpha = 0.9f),
                center = androidx.compose.ui.geometry.Offset(x, y),
                radius = 8f
            )
            drawLine(
                color = pointColor.copy(alpha = 0.6f),
                start = androidx.compose.ui.geometry.Offset(x, y),
                end = androidx.compose.ui.geometry.Offset(markerX, markerY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = pointColor.copy(alpha = 0.6f),
                center = androidx.compose.ui.geometry.Offset(markerX, markerY),
                radius = 3f
            )
        }
    }
}

private fun rssiToRatio(rssi: Int): Float {
    // inverts RSSI so that stronger signal appears nearer center
    val clamped = rssi.coerceIn(-100, -30)
    return (1f - ((clamped + 100) / 70f).coerceIn(0f, 1f)).coerceIn(0f, 1f)
}

private fun angularDistance(a: Float, b: Float): Float {
    val diff = ((a - b + 540f) % 360f) - 180f
    return kotlin.math.abs(diff)
}

@Composable
fun DeviceRow(result: ScanResult) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = result.device.name ?: "Unknown Device",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = result.device.address,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "RSSI: ${result.rssi}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

