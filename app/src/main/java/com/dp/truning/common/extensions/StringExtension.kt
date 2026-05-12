package com.dp.truning.common.extensions

import android.util.Log
import java.util.Locale

/**
 * 将字符串首字母转换为大写。
 */
fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

/**
 * 输出调试日志。
 */
fun String.debug(message: String) {
    Log.d(this, message)
}
