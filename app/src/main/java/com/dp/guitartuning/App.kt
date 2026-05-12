package com.dp.guitartuning

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    /**
     * 在组件创建时完成初始化。
     */
    override fun onCreate() {
        super.onCreate()
//        BRV.modelId = BR.item
        // 初始化 QMUI
        // 开启日志（可选）
    }
}
