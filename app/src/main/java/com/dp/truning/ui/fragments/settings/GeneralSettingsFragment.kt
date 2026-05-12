package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.dp.truning.R
import com.dp.truning.databinding.FragmentGeneralSettingsBinding
import com.dp.truning.domain.model.AppThemeMode
import com.dp.truning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneralSettingsFragment :
    BaseVmFragment<FragmentGeneralSettingsBinding, GeneralSettingsViewModel>() {

    private var isUpdatingSelectionFromState = false
    private var isUpdatingToggleFromState = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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

        binding.groupThemeMode.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingSelectionFromState) {
                return@setOnCheckedChangeListener
            }
            when (checkedId) {
                R.id.optionThemeLight -> viewModel.selectThemeMode(AppThemeMode.LIGHT)
                R.id.optionThemeDark -> viewModel.selectThemeMode(AppThemeMode.DARK)
                R.id.optionThemeFollowSystem -> viewModel.selectThemeMode(AppThemeMode.FOLLOW_SYSTEM)
            }
        }

        bindToggle(binding.switchKeepScreenOn) { viewModel.setKeepScreenOnEnabled(it) }
        bindToggle(binding.switchVolumeBoost) { viewModel.setVolumeBoostEnabled(it) }
    }

    private fun bindObservers() {
        viewModel.selectedThemeMode.observe(viewLifecycleOwner) { themeMode ->
            val targetId = when (themeMode) {
                AppThemeMode.LIGHT -> R.id.optionThemeLight
                AppThemeMode.DARK -> R.id.optionThemeDark
                AppThemeMode.FOLLOW_SYSTEM -> R.id.optionThemeFollowSystem
            }
            if (binding.groupThemeMode.checkedRadioButtonId != targetId) {
                isUpdatingSelectionFromState = true
                binding.groupThemeMode.check(targetId)
                isUpdatingSelectionFromState = false
            }
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
}
