package com.dp.guitartuning.common.extensions

import android.os.SystemClock
import android.view.View

class OnSingleClickListener(
    private val interval: Long = 1000,
    private val block: (View) -> Unit
) : View.OnClickListener {
    private var lastClickTime: Long = 0
    /**
     * 响应点击事件并执行当前回调。
     */
    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < interval) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        block(view)
    }
}

/**
 * 为视图设置防重复点击监听器。
 */
fun View.setOnSingleClickListener(block: (View) -> Unit) {
    setOnClickListener(OnSingleClickListener {
        block(it)
    })
}

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