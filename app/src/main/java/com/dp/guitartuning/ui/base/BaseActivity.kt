package com.dp.guitartuning.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> VB
) : AppCompatActivity() {
    private val tag = "BaseActivity"

    protected lateinit var binding: VB
    protected lateinit var sharedPref: SharedPreferences

    /**
     * 在页面创建时完成初始化。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "页面已创建")
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
    }
}
