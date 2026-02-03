// /data/data/com.termux/files/home/Blu/BleMapper/app/src/main/java/com/jamjam/blemapper/MainActivity.kt

package com.jamjam.blemapper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jamjam.blemapper.ble.BLEScannerService
import com.jamjam.blemapper.ui.theme.BleMapperTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }

        val scannerService = BLEScannerService(this)
        scannerService.start()

        setContent {
            BleMapperTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DeviceList(scannerService.devices)
                }
            }
        }
    }

    private fun hasAllPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
}

@Composable
fun DeviceList(devices: List<android.bluetooth.le.ScanResult>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("BLE Devices", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(devices) { result ->
                DeviceRow(result)
            }
        }
    }
}

@Composable
fun DeviceRow(result: android.bluetooth.le.ScanResult) {
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
