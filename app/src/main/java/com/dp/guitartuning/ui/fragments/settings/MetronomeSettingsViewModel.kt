package com.dp.guitartuning.ui.fragments.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.guitartuning.common.data.preferences.Preferences
import com.dp.guitartuning.domain.model.MetronomeSettings
import com.dp.guitartuning.domain.model.MetronomeSoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MetronomeSettingsViewModel @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {

    val defaultBpm = MutableLiveData(MetronomeSettings.DEFAULT_BPM)
    val selectedSoundType = MutableLiveData(MetronomeSoundType.WOOD_BLOCK)
    val accentEnabled = MutableLiveData(true)
    val vibrationEnabled = MutableLiveData(false)
    val accentVibrationEnabled = MutableLiveData(true)
    val regularVibrationEnabled = MutableLiveData(false)
    val vibrationOptionsEnabled = MutableLiveData(false)

    fun loadSettings() {
        val settings = preferences.getMetronomeSettings()
        defaultBpm.value = settings.lastBpm
        selectedSoundType.value = settings.soundType
        accentEnabled.value = settings.accentEnabled
        vibrationEnabled.value = settings.vibrationEnabled
        accentVibrationEnabled.value = settings.accentVibrationEnabled
        regularVibrationEnabled.value = settings.regularVibrationEnabled
        vibrationOptionsEnabled.value = settings.vibrationEnabled
    }

    fun selectSoundType(soundType: MetronomeSoundType) {
        preferences.setMetronomeSoundType(soundType)
        selectedSoundType.value = soundType
    }

    fun setAccentEnabled(enabled: Boolean) {
        preferences.setMetronomeAccentEnabled(enabled)
        accentEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        preferences.setMetronomeVibrationEnabled(enabled)
        vibrationEnabled.value = enabled
        vibrationOptionsEnabled.value = enabled
    }

    fun setAccentVibrationEnabled(enabled: Boolean) {
        preferences.setMetronomeAccentVibrationEnabled(enabled)
        accentVibrationEnabled.value = enabled
    }

    fun setRegularVibrationEnabled(enabled: Boolean) {
        preferences.setMetronomeRegularVibrationEnabled(enabled)
        regularVibrationEnabled.value = enabled
    }
}
