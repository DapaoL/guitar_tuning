package com.dp.truning.ui.fragments.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.truning.common.data.preferences.Preferences
import com.dp.truning.domain.model.AppThemeMode
import com.dp.truning.domain.model.MetronomeSettings
import com.dp.truning.domain.model.MetronomeSoundType
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {

    val referenceA4Input = MutableLiveData("")
    val referenceA4HasError = MutableLiveData(false)
    val selectedSensitivity = MutableLiveData(TuningSensitivity.MEDIUM)
    val selectedDisplayMode = MutableLiveData(TunerDisplayMode.POINTER)
    val metronomeLastBpm = MutableLiveData(MetronomeSettings.DEFAULT_BPM)
    val metronomeSoundType = MutableLiveData(MetronomeSoundType.WOOD_BLOCK)
    val generalThemeMode = MutableLiveData(AppThemeMode.FOLLOW_SYSTEM)
    val generalKeepScreenOn = MutableLiveData(false)
    val generalVolumeBoost = MutableLiveData(false)

    /**
     * 加载调音器和节拍器设置。
     */
    fun loadSettings() {
        val tunerSettings = preferences.getTunerSettings()
        val metronomeSettings = preferences.getMetronomeSettings()
        val generalSettings = preferences.getGeneralSettings()

        referenceA4Input.value = tunerSettings.referenceA4Hz.toString()
        referenceA4HasError.value = false
        selectedSensitivity.value = tunerSettings.sensitivity
        selectedDisplayMode.value = tunerSettings.displayMode
        metronomeLastBpm.value = metronomeSettings.lastBpm
        metronomeSoundType.value = metronomeSettings.soundType
        generalThemeMode.value = generalSettings.themeMode
        generalKeepScreenOn.value = generalSettings.keepScreenOnEnabled
        generalVolumeBoost.value = generalSettings.volumeBoostEnabled
    }

    /**
     * 更新 A4 输入框内容。
     */
    fun onReferenceA4Changed(input: String) {
        referenceA4Input.value = input
        referenceA4HasError.value = false
    }

    /**
     * 保存 A4 参考频率。
     */
    fun saveReferenceA4() {
        val parsed = referenceA4Input.value?.trim()?.toIntOrNull()
        if (parsed == null || parsed !in TunerSettings.MIN_REFERENCE_A4_HZ..TunerSettings.MAX_REFERENCE_A4_HZ) {
            referenceA4HasError.value = true
            return
        }

        preferences.setTunerReferenceA4(parsed)
        referenceA4Input.value = parsed.toString()
        referenceA4HasError.value = false
    }

    /**
     * 保存灵敏度选择。
     */
    fun selectSensitivity(sensitivity: TuningSensitivity) {
        preferences.setTunerSensitivity(sensitivity)
        selectedSensitivity.value = sensitivity
    }

    /**
     * 保存显示模式选择。
     */
    fun selectDisplayMode(displayMode: TunerDisplayMode) {
        preferences.setTunerDisplayMode(displayMode)
        selectedDisplayMode.value = displayMode
    }
}
