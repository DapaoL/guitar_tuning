package com.dp.guitartuning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.dp.guitartuning.databinding.FragmentGeneralSettingsBinding
import com.dp.guitartuning.domain.model.AppThemeMode
import com.dp.guitartuning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneralSettingsFragment :
    BaseVmFragment<FragmentGeneralSettingsBinding, GeneralSettingsViewModel>() {

    private var isUpdatingToggleFromState = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        bindObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
    }

    private fun initView() {
        binding.page = this
    }

    private fun bindObservers() {
        viewModel.selectedThemeMode.observe(viewLifecycleOwner) { themeMode ->
            renderThemeSelection(themeMode)
        }

        viewModel.keepScreenOnEnabled.observe(viewLifecycleOwner) { enabled ->
            updateToggle(binding.switchKeepScreenOn, enabled)
        }

        viewModel.volumeBoostEnabled.observe(viewLifecycleOwner) { enabled ->
            updateToggle(binding.switchVolumeBoost, enabled)
        }
    }


    fun goBack(@Suppress("UNUSED_PARAMETER") view: View) {
        (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
    }

    fun selectLightTheme(@Suppress("UNUSED_PARAMETER") view: View) {
        handleThemeSelection(AppThemeMode.LIGHT)
    }

    fun selectDarkTheme(@Suppress("UNUSED_PARAMETER") view: View) {
        handleThemeSelection(AppThemeMode.DARK)
    }

    fun toggleKeepScreenOn(view: View) {
        if (isUpdatingToggleFromState) {
            return
        }
        viewModel.setKeepScreenOnEnabled((view as SwitchCompat).isChecked)
    }

    fun toggleVolumeBoost(view: View) {
        if (isUpdatingToggleFromState) {
            return
        }
        viewModel.setVolumeBoostEnabled((view as SwitchCompat).isChecked)
    }

    private fun updateToggle(toggle: SwitchCompat, enabled: Boolean) {
        if (toggle.isChecked == enabled) {
            return
        }
        isUpdatingToggleFromState = true
        toggle.isChecked = enabled
        isUpdatingToggleFromState = false
    }

    private fun renderThemeSelection(themeMode: AppThemeMode) {
        val lightSelected = themeMode == AppThemeMode.LIGHT
        val darkSelected = themeMode == AppThemeMode.DARK
        binding.cardThemeLight.isSelected = lightSelected
        binding.cardThemeDark.isSelected = darkSelected
        binding.optionThemeLight.isChecked = lightSelected
        binding.optionThemeDark.isChecked = darkSelected
    }

    private fun handleThemeSelection(themeMode: AppThemeMode) {
        if (viewModel.selectThemeMode(themeMode)) {
            requireActivity().recreate()
        }
    }
}
