package com.dp.guitartuning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.FragmentSettingsHomeBinding
import com.dp.guitartuning.domain.model.AppThemeMode
import com.dp.guitartuning.domain.model.MetronomeSettings
import com.dp.guitartuning.domain.model.MetronomeSoundType
import com.dp.guitartuning.domain.model.TunerDisplayMode
import com.dp.guitartuning.domain.model.TunerSettings
import com.dp.guitartuning.domain.model.TuningSensitivity
import com.dp.guitartuning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsHomeFragment : BaseVmFragment<FragmentSettingsHomeBinding, SettingsViewModel>() {

    /**
     * 在视图创建完成后绑定界面状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.page = this
        bindState()
    }

    /**
     * 在界面恢复可见时刷新当前设置。
     */
    fun openTunerSettings(@Suppress("UNUSED_PARAMETER") view: View) {
        openSection(SettingsSection.TUNER)
    }

    fun openMetronomeSettings(@Suppress("UNUSED_PARAMETER") view: View) {
        openSection(SettingsSection.METRONOME)
    }

    fun openGeneralSettings(@Suppress("UNUSED_PARAMETER") view: View) {
        openSection(SettingsSection.GENERAL)
    }

    fun openSupportSettings(@Suppress("UNUSED_PARAMETER") view: View) {
        openSection(SettingsSection.SUPPORT)
    }

    fun openAboutSettings(@Suppress("UNUSED_PARAMETER") view: View) {
        openSection(SettingsSection.ABOUT)
    }

    private fun openSection(section: SettingsSection) {
        (parentFragment as? SettingsNavigationHost)?.openSection(section)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
    }

    /**
     * 绑定首页概览状态。
     */
    private fun bindState() {
        renderReferenceA4(TunerSettings.DEFAULT_REFERENCE_A4_HZ)
        renderDisplayMode(TunerDisplayMode.POINTER)
        binding.overviewSensitivityValue.text = getSensitivityLabel(TuningSensitivity.MEDIUM)
        renderMetronomeBpm(MetronomeSettings.DEFAULT_BPM)
        renderMetronomeSoundType(MetronomeSoundType.WOOD_BLOCK)

        viewModel.referenceA4Input.observe(viewLifecycleOwner) { input ->
            renderReferenceA4(input.toIntOrNull() ?: TunerSettings.DEFAULT_REFERENCE_A4_HZ)
        }

        viewModel.selectedDisplayMode.observe(viewLifecycleOwner) { displayMode ->
            renderDisplayMode(displayMode)
        }

        viewModel.selectedSensitivity.observe(viewLifecycleOwner) { sensitivity ->
            binding.overviewSensitivityValue.text = getSensitivityLabel(sensitivity)
        }

        viewModel.metronomeLastBpm.observe(viewLifecycleOwner) { bpm ->
            renderMetronomeBpm(bpm)
        }

        viewModel.metronomeSoundType.observe(viewLifecycleOwner) { soundType ->
            renderMetronomeSoundType(soundType)
        }

        viewModel.generalThemeMode.observe(viewLifecycleOwner) { themeMode ->
            binding.itemGeneralValue.text = getThemeModeLabel(themeMode)
        }

        viewModel.generalKeepScreenOn.observe(viewLifecycleOwner) { keepOn ->
            updateGeneralMeta()
        }

        viewModel.generalVolumeBoost.observe(viewLifecycleOwner) { boost ->
            updateGeneralMeta()
        }
    }

    /**
     * 渲染 A4 参考频率。
     */
    private fun renderReferenceA4(referenceA4Hz: Int) {
        val label = getString(R.string.settings_home_reference_format, referenceA4Hz)
        binding.overviewReferenceValue.text = label
        binding.itemTunerValue.text = label
    }

    /**
     * 渲染调音显示模式。
     */
    private fun renderDisplayMode(displayMode: TunerDisplayMode) {
        val label = getDisplayModeLabel(displayMode)
        binding.overviewDisplayValue.text = label
        binding.itemTunerMeta.text = label
    }

    /**
     * 渲染节拍器 BPM。
     */
    private fun renderMetronomeBpm(bpm: Int) {
        binding.itemMetronomeValue.text = getString(R.string.settings_home_metronome_bpm_format, bpm)
    }

    /**
     * 渲染节拍器音色。
     */
    private fun renderMetronomeSoundType(soundType: MetronomeSoundType) {
        binding.itemMetronomeMeta.text = getMetronomeSoundTypeLabel(soundType)
    }

    /**
     * 获取显示模式文案。
     */
    private fun getDisplayModeLabel(displayMode: TunerDisplayMode): String {
        val labelRes = when (displayMode) {
            TunerDisplayMode.GAUGE -> R.string.settings_display_mode_gauge
            TunerDisplayMode.POINTER -> R.string.settings_display_mode_pointer
            TunerDisplayMode.NUMERIC -> R.string.settings_display_mode_numeric
        }
        return getString(labelRes)
    }

    /**
     * 获取灵敏度文案。
     */
    private fun getSensitivityLabel(sensitivity: TuningSensitivity): String {
        val labelRes = when (sensitivity) {
            TuningSensitivity.HIGH -> R.string.settings_sensitivity_high
            TuningSensitivity.MEDIUM -> R.string.settings_sensitivity_medium
            TuningSensitivity.LOW -> R.string.settings_sensitivity_low
        }
        return getString(labelRes)
    }

    /**
     * 获取节拍器音色文案。
     */
    private fun getMetronomeSoundTypeLabel(soundType: MetronomeSoundType): String {
        val labelRes = when (soundType) {
            MetronomeSoundType.WOOD_BLOCK -> R.string.metronome_sound_type_wood_block
            MetronomeSoundType.CLICK -> R.string.metronome_sound_type_click
            MetronomeSoundType.DRUM -> R.string.metronome_sound_type_drum
            MetronomeSoundType.BEEP -> R.string.metronome_sound_type_beep
        }
        return getString(labelRes)
    }

    /**
     * 获取主题模式文案。
     */
    private fun getThemeModeLabel(themeMode: AppThemeMode): String {
        val labelRes = when (themeMode) {
            AppThemeMode.LIGHT -> R.string.general_settings_theme_light
            AppThemeMode.DARK -> R.string.general_settings_theme_dark
        }
        return getString(labelRes)
    }

    /**
     * 刷新通用设置副文案（常亮 · 音量增强状态）。
     */
    private fun updateGeneralMeta() {
        val keepOn = viewModel.generalKeepScreenOn.value ?: false
        val boost = viewModel.generalVolumeBoost.value ?: false
        val keepOnLabel = if (keepOn) {
            getString(R.string.general_settings_keep_screen_on_on)
        } else {
            getString(R.string.general_settings_keep_screen_on_off)
        }
        val boostLabel = if (boost) {
            getString(R.string.general_settings_volume_boost_on)
        } else {
            getString(R.string.general_settings_volume_boost_off)
        }
        binding.itemGeneralMeta.text = getString(
            R.string.general_settings_home_meta_format,
            keepOnLabel,
            boostLabel
        )
    }
}
