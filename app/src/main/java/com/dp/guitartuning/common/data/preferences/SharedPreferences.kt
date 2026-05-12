package com.dp.guitartuning.common.data.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

abstract class SharedPreferences(context: Context) {

    /**
     * 获取 pref name。
     */
    abstract fun getPrefName(): String

    protected val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PreferencesConstants.KEYSTORE_NAME,MODE_PRIVATE)
    }
}