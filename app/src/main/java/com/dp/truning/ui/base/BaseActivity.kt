package com.dp.truning.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(private val bindingFactory: (LayoutInflater) -> VB) : AppCompatActivity() {
    private val TAG = "BaseActivity"

    protected lateinit var binding: VB
    protected lateinit var sharedPref: SharedPreferences

    /**
     * 在组件创建时完成初始化。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)



    }



}