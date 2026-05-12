package com.dp.guitartuning.ui.fragments.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.guitartuning.common.data.preferences.Preferences
import com.dp.guitartuning.domain.model.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val preferences: Preferences,
    private val systemThemeStateProvider: SystemThemeStateProvider
) : ViewModel() {

    val selectedThemeMode = MutableLiveData(AppThemeMode.LIGHT)
    val keepScreenOnEnabled = MutableLiveData(false)
    val volumeBoostEnabled = MutableLiveData(false)

    fun loadSettings() {
        val settings = preferences.getGeneralSettings()
        selectedThemeMode.value = settings.themeMode
        keepScreenOnEnabled.value = settings.keepScreenOnEnabled
        volumeBoostEnabled.value = settings.volumeBoostEnabled
    }

    fun selectThemeMode(themeMode: AppThemeMode): Boolean {
        val previousThemeMode = selectedThemeMode.value ?: preferences.getGeneralSettings().themeMode
        val shouldRecreate = shouldApplyThemeChange(
            previousThemeMode,
            themeMode,
            systemThemeStateProvider.isSystemDarkTheme()
        )

        if (previousThemeMode == themeMode) {
            return false
        }

        preferences.setGeneralThemeMode(themeMode)
        selectedThemeMode.value = themeMode
        applyTheme(themeMode)
        return shouldRecreate
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
        fun shouldApplyThemeChange(
            previousThemeMode: AppThemeMode,
            targetThemeMode: AppThemeMode,
            systemIsDark: Boolean
        ): Boolean {
            if (previousThemeMode == targetThemeMode) {
                return false
            }

            return resolvesToDarkTheme(previousThemeMode, systemIsDark) !=
                resolvesToDarkTheme(targetThemeMode, systemIsDark)
        }

        private fun resolvesToDarkTheme(
            themeMode: AppThemeMode,
            systemIsDark: Boolean
        ): Boolean {
            return when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
        }

        fun applyTheme(themeMode: AppThemeMode) {
            val mode = when (themeMode) {
                AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
