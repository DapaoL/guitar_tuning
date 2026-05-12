package com.dp.truning.ui.fragments.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.truning.common.data.preferences.Preferences
import com.dp.truning.domain.model.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {

    val selectedThemeMode = MutableLiveData(AppThemeMode.FOLLOW_SYSTEM)
    val keepScreenOnEnabled = MutableLiveData(false)
    val volumeBoostEnabled = MutableLiveData(false)

    fun loadSettings() {
        val settings = preferences.getGeneralSettings()
        selectedThemeMode.value = settings.themeMode
        keepScreenOnEnabled.value = settings.keepScreenOnEnabled
        volumeBoostEnabled.value = settings.volumeBoostEnabled
    }

    fun selectThemeMode(themeMode: AppThemeMode) {
        preferences.setGeneralThemeMode(themeMode)
        selectedThemeMode.value = themeMode
        applyTheme(themeMode)
    }

    fun setKeepScreenOnEnabled(enabled: Boolean) {
        preferences.setGeneralKeepScreenOnEnabled(enabled)
        keepScreenOnEnabled.value = enabled
    }

    fun setVolumeBoostEnabled(enabled: Boolean) {
        preferences.setGeneralVolumeBoostEnabled(enabled)
        volumeBoostEnabled.value = enabled
    }

    companion object {
        fun applyTheme(themeMode: AppThemeMode) {
            val mode = when (themeMode) {
                AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                AppThemeMode.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
