package com.dp.truning.util

import android.widget.Toast
import androidx.fragment.app.FragmentActivity

object Utils {

    /**
     * 显示一条短时提示信息。
     */
    fun FragmentActivity.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}