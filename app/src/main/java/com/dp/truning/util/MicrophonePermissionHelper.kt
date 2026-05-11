package com.dp.truning.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

object MicrophonePermissionHelper {

    /**
     * 检查是否已经授予所需权限。
     */
    fun hasPermission(activity: FragmentActivity): Boolean {
        return XXPermissions.isGranted(activity, Permission.RECORD_AUDIO)
    }

    /**
     * 检查是否已经授予所需权限。
     */
    fun hasPermission(fragment: Fragment): Boolean {
        return XXPermissions.isGranted(fragment.requireContext(), Permission.RECORD_AUDIO)
    }

    /**
     * 请求当前所需权限。
     */
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
                /**
                 * 在权限授予后继续后续流程。
                 */
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        onGranted()
                    }
                }

                /**
                 * 在权限被拒绝后处理降级逻辑。
                 */
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    onDenied?.invoke(doNotAskAgain)
                }
            })
    }

    /**
     * 请求当前所需权限。
     */
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
                /**
                 * 在权限授予后继续后续流程。
                 */
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        onGranted()
                    }
                }

                /**
                 * 在权限被拒绝后处理降级逻辑。
                 */
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    onDenied?.invoke(doNotAskAgain)
                }
            })
    }

    /**
     * 打开应用权限设置页面。
     */
    fun openAppSettings(activity: FragmentActivity) {
        XXPermissions.startPermissionActivity(activity, listOf(Permission.RECORD_AUDIO))
    }

    /**
     * 打开应用权限设置页面。
     */
    fun openAppSettings(fragment: Fragment) {
        XXPermissions.startPermissionActivity(fragment, listOf(Permission.RECORD_AUDIO))
    }
}
