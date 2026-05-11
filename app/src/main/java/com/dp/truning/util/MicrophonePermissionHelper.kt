package com.dp.truning.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

object MicrophonePermissionHelper {

    fun hasPermission(activity: FragmentActivity): Boolean {
        return XXPermissions.isGranted(activity, Permission.RECORD_AUDIO)
    }

    fun hasPermission(fragment: Fragment): Boolean {
        return XXPermissions.isGranted(fragment.requireContext(), Permission.RECORD_AUDIO)
    }

    fun request(
        activity: FragmentActivity,
        onGranted: () -> Unit,
        onDenied: ((doNotAskAgain: Boolean) -> Unit)? = null
    ) {
        if (hasPermission(activity)) {
            onGranted()
            return
        }

        XXPermissions.with(activity)
            .permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        onGranted()
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    onDenied?.invoke(doNotAskAgain)
                }
            })
    }

    fun request(
        fragment: Fragment,
        onGranted: () -> Unit,
        onDenied: ((doNotAskAgain: Boolean) -> Unit)? = null
    ) {
        if (hasPermission(fragment)) {
            onGranted()
            return
        }

        XXPermissions.with(fragment)
            .permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        onGranted()
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    onDenied?.invoke(doNotAskAgain)
                }
            })
    }

    fun openAppSettings(activity: FragmentActivity) {
        XXPermissions.startPermissionActivity(activity, listOf(Permission.RECORD_AUDIO))
    }

    fun openAppSettings(fragment: Fragment) {
        XXPermissions.startPermissionActivity(fragment, listOf(Permission.RECORD_AUDIO))
    }
}
