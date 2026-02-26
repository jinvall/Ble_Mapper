package com.jamjam.blemapper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.bluetooth.le.ScanResult
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

    private lateinit var scannerService: BLEScannerService

    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                scannerService.start()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerService = BLEScannerService(this)

        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            scannerService.start()
        }

        setContent {
            BleMapperTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DeviceList(scannerService.devices)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerService.stop()
    }

    private fun Context.hasAllPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
}

@Composable
fun DeviceList(devices: List<ScanResult>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(devices) { result ->
            DeviceRow(result)
        }
    }
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

