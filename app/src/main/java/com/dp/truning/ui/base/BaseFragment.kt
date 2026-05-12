package com.dp.truning.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = checkNotNull(_binding) { "Binding 只在 onCreateView 到 onDestroyView 之间可用。" }

    @Suppress("UNCHECKED_CAST")
    private val classVB: Class<VB>
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>

    private val inflateMethod by lazy(LazyThreadSafetyMode.NONE) {
        classVB.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
    }

    /**
     * 创建并返回当前 Fragment 的视图层级。
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        @Suppress("UNCHECKED_CAST")
        val viewBinding = inflateMethod.invoke(null, inflater, container, false) as VB
        _binding = viewBinding
        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
