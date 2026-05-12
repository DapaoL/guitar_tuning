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
 * 隐藏当前视图。
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 显示一条短提示。
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 输出调试日志。
 */
fun String.debug(message: String) {
    Log.d(this, message)
}

/**
 * 更新输入框内容并把光标移到末尾。
 */
fun EditText.modifyText(numberText: String) {
    setText(numberText)
    setSelection(numberText.length)
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
