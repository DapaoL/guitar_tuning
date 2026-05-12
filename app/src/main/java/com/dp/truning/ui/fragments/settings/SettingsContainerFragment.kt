package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import com.dp.truning.databinding.FragmentSettingsContainerBinding
import com.dp.truning.ui.activitys.MainActivity
import com.dp.truning.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsContainerFragment :
    BaseFragment<FragmentSettingsContainerBinding>(),
    SettingsNavigationHost {

    /**
     * 在视图创建完成后绑定界面状态与返回逻辑。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            showSettingsHome()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                (activity as? MainActivity)?.returnToPreviousPrimaryTab()
                    ?: requireActivity().finish()
            }
        }
    }

    /**
     * 打开指定设置分区。
     */
    override fun openSection(section: SettingsSection) {
        val fragment = when (section) {
            SettingsSection.TUNER -> SettingsFragment()
            SettingsSection.METRONOME -> MetronomeSettingsFragment()
            SettingsSection.GENERAL -> GeneralSettingsFragment()
            SettingsSection.SUPPORT -> SupportFeedbackFragment()
            SettingsSection.ABOUT -> AboutSettingsFragment()
            SettingsSection.PERMISSIONS -> PermissionsFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(binding.settingsChildContainer.id, fragment)
            .addToBackStack(section.name)
            .commit()
    }

    /**
     * 从设置子页面返回上一层。
     */
    override fun goBackFromSettingsChild() {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            showSettingsHome()
        }
    }

    /**
     * 显示设置首页。
     */
    private fun showSettingsHome() {
        childFragmentManager.beginTransaction()
            .replace(binding.settingsChildContainer.id, SettingsHomeFragment())
            .commit()
    }
}
