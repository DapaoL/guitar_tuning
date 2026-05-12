package com.dp.guitartuning.common.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * 显示一条短提示。
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 打开指定链接。
 */
fun Context.goURL(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        toast("当前设备没有可用的浏览器，请先安装浏览器。")
        error.printStackTrace()
    }
}
