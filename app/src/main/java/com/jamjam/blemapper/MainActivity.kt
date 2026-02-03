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

    private lateinit var scannerService: BLEScannerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerService = BLEScannerService(this)

        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
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
}

