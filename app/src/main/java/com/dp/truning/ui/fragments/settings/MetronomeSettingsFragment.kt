package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.dp.truning.R
import com.dp.truning.databinding.FragmentMetronomeSettingsBinding
import com.dp.truning.domain.model.MetronomeSoundType
import com.dp.truning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MetronomeSettingsFragment :
    BaseVmFragment<FragmentMetronomeSettingsBinding, MetronomeSettingsViewModel>() {

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

        binding.groupSoundType.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingSelectionFromState) {
                return@setOnCheckedChangeListener
            }

            when (checkedId) {
                R.id.optionSoundWoodBlock -> viewModel.selectSoundType(MetronomeSoundType.WOOD_BLOCK)
                R.id.optionSoundClick -> viewModel.selectSoundType(MetronomeSoundType.CLICK)
                R.id.optionSoundDrum -> viewModel.selectSoundType(MetronomeSoundType.DRUM)
                R.id.optionSoundBeep -> viewModel.selectSoundType(MetronomeSoundType.BEEP)
            }
        }

        bindToggle(binding.switchAccentEnabled) { viewModel.setAccentEnabled(it) }
        bindToggle(binding.switchVibrationEnabled) { viewModel.setVibrationEnabled(it) }
        bindToggle(binding.switchAccentVibration) { viewModel.setAccentVibrationEnabled(it) }
        bindToggle(binding.switchRegularVibration) { viewModel.setRegularVibrationEnabled(it) }
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
