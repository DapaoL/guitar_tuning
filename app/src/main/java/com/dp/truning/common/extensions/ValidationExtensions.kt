package com.dp.truning.common.extensions

import android.util.Patterns
import android.webkit.URLUtil
import java.net.MalformedURLException
import java.net.URL

/**
 * 判断是否 valid phone number。
 */
fun String.isValidPhoneNumber(): Boolean {
    return this.all { it.isDigit() }
}

/**
 * 判断是否 valid email。
 */
fun String.isValidEmail(): Boolean {
    if (isEmpty()) return false
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * 判断是否 valid password。
 */
fun String.isValidPassword(): Boolean {
    if (isEmpty() || this.length < 6) return false
    return true
}

/**
 * 判断是否 valid url。
 */
fun String.isValidUrl(): Boolean {
    try {
        URL(this)
        return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this).matches()
    } catch (ignored: MalformedURLException) {
    }
    return false
}