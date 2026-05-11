package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dp.truning.R
import com.dp.truning.databinding.FragmentSettingsHomeBinding
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsHomeFragment : Fragment(R.layout.fragment_settings_home) {

    private var _binding: FragmentSettingsHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsHomeBinding.bind(view)

        val host = parentFragment as? SettingsNavigationHost ?: return
        bindState()

        listOf(
            binding.itemTuner to SettingsSection.TUNER,
            binding.itemMetronome to SettingsSection.METRONOME,
            binding.itemGeneral to SettingsSection.GENERAL,
            binding.itemSupport to SettingsSection.SUPPORT,
            binding.itemAbout to SettingsSection.ABOUT
        ).forEach { (itemView, section) ->
            itemView.setOnClickListener { host.openSection(section) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindState() {
        renderReferenceA4(TunerSettings.DEFAULT_REFERENCE_A4_HZ)
        renderDisplayMode(TunerDisplayMode.POINTER)
        binding.overviewSensitivityValue.text = getSensitivityLabel(TuningSensitivity.MEDIUM)

        viewModel.referenceA4Input.observe(viewLifecycleOwner) { input ->
            renderReferenceA4(input.toIntOrNull() ?: TunerSettings.DEFAULT_REFERENCE_A4_HZ)
        }

        viewModel.selectedDisplayMode.observe(viewLifecycleOwner) { displayMode ->
            renderDisplayMode(displayMode)
        }

        viewModel.selectedSensitivity.observe(viewLifecycleOwner) { sensitivity ->
            binding.overviewSensitivityValue.text = getSensitivityLabel(sensitivity)
        }
    }

    private fun renderReferenceA4(referenceA4Hz: Int) {
        val label = getString(R.string.settings_home_reference_format, referenceA4Hz)
        binding.overviewReferenceValue.text = label
        binding.itemTunerValue.text = label
    }

    private fun renderDisplayMode(displayMode: TunerDisplayMode) {
        val label = getDisplayModeLabel(displayMode)
        binding.overviewDisplayValue.text = label
        binding.itemTunerMeta.text = label
    }

    private fun getDisplayModeLabel(displayMode: TunerDisplayMode): String {
        val labelRes = when (displayMode) {
            TunerDisplayMode.GAUGE -> R.string.settings_display_mode_gauge
            TunerDisplayMode.POINTER -> R.string.settings_display_mode_pointer
            TunerDisplayMode.NUMERIC -> R.string.settings_display_mode_numeric
        }
        return getString(labelRes)
    }

    private fun getSensitivityLabel(sensitivity: TuningSensitivity): String {
        val labelRes = when (sensitivity) {
            TuningSensitivity.HIGH -> R.string.settings_sensitivity_high
            TuningSensitivity.MEDIUM -> R.string.settings_sensitivity_medium
            TuningSensitivity.LOW -> R.string.settings_sensitivity_low
        }
        return getString(labelRes)
    }
}
