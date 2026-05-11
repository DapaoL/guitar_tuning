package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.dp.truning.R
import com.dp.truning.databinding.FragmentSettingsContainerBinding
import com.dp.truning.ui.activitys.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsContainerFragment : Fragment(R.layout.fragment_settings_container), SettingsNavigationHost {

    private var _binding: FragmentSettingsContainerBinding? = null
    private val binding get() = _binding!!

    /**
     * 在视图创建完成后绑定界面状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsContainerBinding.bind(view)

        if (savedInstanceState == null) {
            showSettingsHome()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                (activity as? MainActivity)?.returnToPreviousPrimaryTab() ?: requireActivity().finish()
            }
        }
    }

    /**
     * 打开 section。
     */
    override fun openSection(section: SettingsSection) {
        val fragment = if (section.usesPlaceholder) {
            SettingsPlaceholderFragment.newInstance(section.titleRes)
        } else {
            SettingsFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(binding.settingsChildContainer.id, fragment)
            .addToBackStack(section.name)
            .commit()
    }

    /**
     * 返回 from settings child。
     */
    override fun goBackFromSettingsChild() {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            showSettingsHome()
        }
    }

    /**
     * 在视图销毁时释放与界面相关的资源。
     */
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 显示 settings home。
     */
    private fun showSettingsHome() {
        childFragmentManager.beginTransaction()
            .replace(binding.settingsChildContainer.id, SettingsHomeFragment())
            .commit()
    }
}
