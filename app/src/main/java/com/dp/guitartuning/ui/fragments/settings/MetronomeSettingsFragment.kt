package com.dp.guitartuning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.FragmentMetronomeSettingsBinding
import com.dp.guitartuning.domain.model.MetronomeSoundType
import com.dp.guitartuning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MetronomeSettingsFragment :
    BaseVmFragment<FragmentMetronomeSettingsBinding, MetronomeSettingsViewModel>() {

    private var isUpdatingSelectionFromState = false
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
        viewModel.defaultBpm.observe(viewLifecycleOwner) { bpm ->
            binding.defaultBpmValue.text = getString(R.string.settings_home_metronome_bpm_format, bpm)
        }

        viewModel.selectedSoundType.observe(viewLifecycleOwner) { soundType ->
            val targetId = when (soundType) {
                MetronomeSoundType.WOOD_BLOCK -> R.id.optionSoundWoodBlock
                MetronomeSoundType.CLICK -> R.id.optionSoundClick
                MetronomeSoundType.DRUM -> R.id.optionSoundDrum
                MetronomeSoundType.BEEP -> R.id.optionSoundBeep
            }
            if (binding.groupSoundType.checkedRadioButtonId != targetId) {
                isUpdatingSelectionFromState = true
                binding.groupSoundType.check(targetId)
                isUpdatingSelectionFromState = false
            }
        }

        viewModel.accentEnabled.observe(viewLifecycleOwner) { enabled ->
            updateToggle(binding.switchAccentEnabled, enabled)
        }

        viewModel.vibrationEnabled.observe(viewLifecycleOwner) { enabled ->
            updateToggle(binding.switchVibrationEnabled, enabled)
        }

        viewModel.accentVibrationEnabled.observe(viewLifecycleOwner) { enabled ->
            updateToggle(binding.switchAccentVibration, enabled)
        }

        viewModel.regularVibrationEnabled.observe(viewLifecycleOwner) { enabled ->
            updateToggle(binding.switchRegularVibration, enabled)
        }

        viewModel.vibrationOptionsEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.vibrationOptionsContainer.alpha = if (enabled) 1f else 0.48f
            binding.switchAccentVibration.isEnabled = enabled
            binding.switchRegularVibration.isEnabled = enabled
        }
    }


    fun goBack(@Suppress("UNUSED_PARAMETER") view: View) {
        (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
    }

    fun selectWoodBlockSound(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSoundType(MetronomeSoundType.WOOD_BLOCK)
    }

    fun selectClickSound(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSoundType(MetronomeSoundType.CLICK)
    }

    fun selectDrumSound(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSoundType(MetronomeSoundType.DRUM)
    }

    fun selectBeepSound(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSoundType(MetronomeSoundType.BEEP)
    }

    fun toggleAccent(view: View) {
        if (isUpdatingToggleFromState) {
            return
        }
        viewModel.setAccentEnabled((view as SwitchCompat).isChecked)
    }

    fun toggleVibration(view: View) {
        if (isUpdatingToggleFromState) {
            return
        }
        viewModel.setVibrationEnabled((view as SwitchCompat).isChecked)
    }

    fun toggleAccentVibration(view: View) {
        if (isUpdatingToggleFromState) {
            return
        }
        viewModel.setAccentVibrationEnabled((view as SwitchCompat).isChecked)
    }

    fun toggleRegularVibration(view: View) {
        if (isUpdatingToggleFromState) {
            return
        }
        viewModel.setRegularVibrationEnabled((view as SwitchCompat).isChecked)
    }

    private fun selectSoundType(soundType: MetronomeSoundType) {
        if (isUpdatingSelectionFromState) {
            return
        }
        viewModel.selectSoundType(soundType)
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
