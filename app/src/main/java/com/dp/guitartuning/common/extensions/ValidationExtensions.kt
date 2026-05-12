package com.dp.guitartuning.common.extensions

import android.util.Patterns
import android.webkit.URLUtil
import java.net.MalformedURLException
import java.net.URL

/**
 * 判断是否为有效手机号。
 */
fun String.isValidPhoneNumber(): Boolean {
    return all { it.isDigit() }
}

/**
 * 判断是否为有效邮箱。
 */
fun String.isValidEmail(): Boolean {
    if (isEmpty()) return false
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * 判断是否为有效密码。
 */
fun String.isValidPassword(): Boolean {
    if (isEmpty() || length < 6) return false
    return true
}

/**
 * 判断是否为有效链接。
 */
fun String.isValidUrl(): Boolean {
    try {
        URL(this)
        return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this).matches()
    } catch (_: MalformedURLException) {
    }
    return false
}
