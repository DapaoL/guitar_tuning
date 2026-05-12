package com.dp.guitartuning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.dp.guitartuning.databinding.FragmentSettingsPlaceholderBinding
import com.dp.guitartuning.ui.base.BaseFragment

class SettingsPlaceholderFragment : BaseFragment<FragmentSettingsPlaceholderBinding>() {

    /**
     * 在视图创建完成后绑定占位页文案与返回事件。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleView.setText(requireArguments().getInt(ARG_TITLE_RES))
        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }
    }

    companion object {
        private const val ARG_TITLE_RES = "title_res"

        /**
         * 使用标题资源创建新的占位页实例。
         */
        fun newInstance(@StringRes titleRes: Int): SettingsPlaceholderFragment {
            return SettingsPlaceholderFragment().apply {
                arguments = bundleOf(ARG_TITLE_RES to titleRes)
            }
        }
    }
}
