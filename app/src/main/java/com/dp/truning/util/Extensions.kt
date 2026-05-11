package com.dp.truning.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast

/**
 * 显示当前视图。
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * 隐藏当前视图并移除布局占位。
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 显示一条短时提示信息。
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 输出一条调试日志。
 */
fun String.debug(message: String) {
    Log.d(this, message)
}

/**
 * 处理 modify text 相关逻辑。
 */
fun EditText.modifyText(numberText: String) {
    this.setText(numberText)
    this.setSelection(numberText.length)
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