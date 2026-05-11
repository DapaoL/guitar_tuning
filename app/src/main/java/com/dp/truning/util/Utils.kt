package com.dp.truning.util

import android.widget.Toast
import androidx.fragment.app.FragmentActivity

object Utils {

    fun FragmentActivity.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}