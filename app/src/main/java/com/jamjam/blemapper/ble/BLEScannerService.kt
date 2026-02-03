// /data/data/com.termux/files/home/Blu/BleMapper/app/src/main/java/com/jamjam/blemapper/ble/BLEScannerService.kt

package com.jamjam.blemapper.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class BLEScannerService(context: Context) {

    private val bluetoothManager =
        context.getSystemService(BluetoothManager::class.java)

    private val bluetoothAdapter: BluetoothAdapter =
        bluetoothManager.adapter

    private val scanner: BluetoothLeScanner =
        bluetoothAdapter.bluetoothLeScanner

    val devices: SnapshotStateList<ScanResult> = mutableStateListOf()

    private val callback = object : ScanCallback() {
        override fun onScanResult(type: Int, result: ScanResult) {
            val index = devices.indexOfFirst { it.device.address == result.device.address }
            if (index == -1) {
                devices.add(result)
            } else {
                devices[index] = result
            }
        }
    }

    fun start() {
        scanner.startScan(callback)
    }

    fun stop() {
        scanner.stopScan(callback)
    }
}
