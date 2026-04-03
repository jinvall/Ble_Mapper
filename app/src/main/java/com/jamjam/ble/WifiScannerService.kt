package com.jamjam.blemapper.ble

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.BufferedReader
import java.io.InputStreamReader

data class WifiNetwork(val ssid: String, val bssid: String, val rssi: Int, val uid: String = bssid)

data class WifiAnalytics(val uid: String, val ssid: String, val bssid: String, val rssi: Int, val timestamp: Long)

class WifiScannerService(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var scannerThread: Thread? = null
    @Volatile private var running = false

    val networks: SnapshotStateList<WifiNetwork> = mutableStateListOf()

    fun start(pollIntervalMs: Long = 5000) {
        if (running) return
        running = true

        scannerThread = Thread {
            while (running) {
                try {
                    val scanned = scanWifi() ?: emptyList()
                    mainHandler.post {
                        networks.clear()
                        networks.addAll(scanned)
                    }
                } catch (e: Exception) {
                    // ignore parse errors/command failures for now
                }

                try {
                    Thread.sleep(pollIntervalMs)
                } catch (ie: InterruptedException) {
                    break
                }
            }
        }
        scannerThread?.start()
    }

    fun stop() {
        running = false
        scannerThread?.interrupt()
        scannerThread = null
    }

    private fun scanWifi(): List<WifiNetwork>? {
        val output = runRootCommand("iw dev wlan0 scan") ?: runCommand("iw dev wlan0 scan")
        if (output.isNullOrBlank()) return null

        val results = mutableListOf<WifiNetwork>()
        var currentBssid = ""
        var currentSsid = ""
        var currentRssi = Int.MIN_VALUE

        output.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("BSS ") -> {
                    if (currentBssid.isNotBlank()) {
                        // push previous
                        if (currentSsid.isNotBlank() && currentRssi > Int.MIN_VALUE) {
                            results.add(WifiNetwork(currentSsid, currentBssid, currentRssi, uid = currentBssid))
                        }
                    }
                    currentBssid = trimmed.split(' ')[1].trim()
                    currentSsid = ""
                    currentRssi = Int.MIN_VALUE
                }
                trimmed.startsWith("SSID:") -> {
                    currentSsid = trimmed.removePrefix("SSID:").trim()
                }
                trimmed.startsWith("SSID ") -> {
                    // iw output may be `SSID: <name>` or `SSID: "name"` etc
                    currentSsid = trimmed.removePrefix("SSID ").trim().trim('"')
                }
                trimmed.startsWith("signal:") -> {
                    val v = trimmed.removePrefix("signal:").trim().split(' ')[0]
                    currentRssi = v.toFloatOrNull()?.toInt() ?: Int.MIN_VALUE
                }
            }
        }

        if (currentBssid.isNotBlank() && currentSsid.isNotBlank() && currentRssi > Int.MIN_VALUE) {
            results.add(WifiNetwork(currentSsid, currentBssid, currentRssi, uid = currentBssid))
        }

        return results.distinctBy { it.bssid }
    }

    private fun runRootCommand(command: String): String? {
        return try {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use(BufferedReader::readText)
            process.waitFor()
            if (process.exitValue() == 0) output else null
        } catch (t: Throwable) {
            null
        }
    }

    private fun runCommand(command: String): String? {
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use(BufferedReader::readText)
            process.waitFor()
            if (process.exitValue() == 0) output else null
        } catch (t: Throwable) {
            null
        }
    }
}
