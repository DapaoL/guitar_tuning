package com.dp.truning.ui.fragments.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.truning.common.data.preferences.Preferences
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

    fun loadSettings() {
        val settings = preferences.getTunerSettings()
        referenceA4Input.value = settings.referenceA4Hz.toString()
        referenceA4HasError.value = false
        selectedSensitivity.value = settings.sensitivity
        selectedDisplayMode.value = settings.displayMode
    }

    fun onReferenceA4Changed(input: String) {
        referenceA4Input.value = input
        referenceA4HasError.value = false
    }

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

    fun selectSensitivity(sensitivity: TuningSensitivity) {
        preferences.setTunerSensitivity(sensitivity)
        selectedSensitivity.value = sensitivity
    }

    fun selectDisplayMode(displayMode: TunerDisplayMode) {
        preferences.setTunerDisplayMode(displayMode)
        selectedDisplayMode.value = displayMode
    }
}
