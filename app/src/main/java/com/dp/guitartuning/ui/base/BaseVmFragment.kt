package com.dp.guitartuning.ui.base

import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseVmFragment<VB : ViewBinding, VM : ViewModel> : BaseFragment<VB>() {

    @Suppress("UNCHECKED_CAST")
    private val classVM: Class<VM>
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>

    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        createViewModelLazy(classVM.kotlin, { viewModelStore }).value
    }
}
