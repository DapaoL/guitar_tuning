package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.dp.truning.R
import com.dp.truning.databinding.FragmentSettingsPlaceholderBinding

class SettingsPlaceholderFragment : Fragment(R.layout.fragment_settings_placeholder) {

    private var _binding: FragmentSettingsPlaceholderBinding? = null
    private val binding get() = _binding!!

    /**
     * 在视图创建完成后绑定界面状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsPlaceholderBinding.bind(view)

        binding.titleView.setText(requireArguments().getInt(ARG_TITLE_RES))
        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }
    }

    /**
     * 在视图销毁时释放与界面相关的资源。
     */
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TITLE_RES = "title_res"

        /**
         * 处理 new instance 相关逻辑。
         */
        fun newInstance(@StringRes titleRes: Int): SettingsPlaceholderFragment {
            return SettingsPlaceholderFragment().apply {
                arguments = bundleOf(ARG_TITLE_RES to titleRes)
            }
        }
    }
}
