package com.dp.truning

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
//        BRV.modelId = BR.item
        // 初始化 QMUI
        // 开启日志（可选）
    }
}
