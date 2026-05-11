package com.dp.truning.common.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * 显示一条短时提示信息。
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 处理 go URL 相关逻辑。
 */
fun Context.goURL(url: String) {
    try {
        val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(myIntent)
    } catch (e: ActivityNotFoundException) {
        this.toast("No application can handle this request. Please install a webbrowser")
        e.printStackTrace()
    }
}
