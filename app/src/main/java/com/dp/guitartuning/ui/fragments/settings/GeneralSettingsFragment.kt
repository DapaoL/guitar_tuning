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
        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }

        binding.cardThemeLight.setOnClickListener { handleThemeSelection(AppThemeMode.LIGHT) }
        binding.cardThemeDark.setOnClickListener { handleThemeSelection(AppThemeMode.DARK) }

        bindToggle(binding.switchKeepScreenOn) { viewModel.setKeepScreenOnEnabled(it) }
        bindToggle(binding.switchVolumeBoost) { viewModel.setVolumeBoostEnabled(it) }
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

    private fun bindToggle(toggle: SwitchCompat, onChanged: (Boolean) -> Unit) {
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingToggleFromState) {
                return@setOnCheckedChangeListener
            }
            onChanged(isChecked)
        }
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
