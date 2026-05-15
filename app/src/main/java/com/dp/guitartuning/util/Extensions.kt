package com.dp.guitartuning.util

import android.content.Context
import android.widget.Toast

/**
 * 显示一条短提示。
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
