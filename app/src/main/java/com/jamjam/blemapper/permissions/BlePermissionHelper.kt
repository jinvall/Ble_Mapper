// /data/data/com.termux/files/home/Blu/BleMapper/app/src/main/java/com/jamjam/blemapper/permissions/BlePermissionHelper.kt

package com.jamjam.blemapper.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object BlePermissionHelper {

    val requiredPermissions: Array<String>
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        }

    fun hasAllPermissions(activity: Activity): Boolean {
        return requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(activity, perm) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestAllPermissions(
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        launcher.launch(requiredPermissions)
    }
}
