package com.dp.guitartuning.ui.fragments.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dp.guitartuning.common.data.preferences.Preferences
import com.dp.guitartuning.domain.model.AppThemeMode
import com.dp.guitartuning.domain.model.GeneralSettings
import com.dp.guitartuning.domain.model.MetronomeSettings
import com.dp.guitartuning.domain.model.MetronomeSoundType
import com.dp.guitartuning.domain.model.TunerDisplayMode
import com.dp.guitartuning.domain.model.TunerSettings
import com.dp.guitartuning.domain.model.TuningSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GeneralSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun loadSettings_populatesUiStateFromSavedSettings() {
        val fakePreferences = FakePreferences(
            currentGeneralSettings = GeneralSettings(
                themeMode = AppThemeMode.DARK,
                keepScreenOnEnabled = true,
                volumeBoostEnabled = false
            )
        )
        val viewModel = GeneralSettingsViewModel(fakePreferences, FakeSystemThemeStateProvider())

        viewModel.loadSettings()

        assertEquals(AppThemeMode.DARK, viewModel.selectedThemeMode.value)
        assertTrue(viewModel.keepScreenOnEnabled.value ?: false)
        assertFalse(viewModel.volumeBoostEnabled.value ?: true)
    }

    @Test
    fun selectThemeMode_persistsAndUpdatesState() {
        val fakePreferences = FakePreferences()
        val viewModel = GeneralSettingsViewModel(fakePreferences, FakeSystemThemeStateProvider())

        val shouldRecreate = viewModel.selectThemeMode(AppThemeMode.LIGHT)

        assertEquals(AppThemeMode.LIGHT, fakePreferences.currentGeneralSettings.themeMode)
        assertEquals(AppThemeMode.LIGHT, viewModel.selectedThemeMode.value)
        assertFalse(shouldRecreate)
    }

    @Test
    fun setKeepScreenOnEnabled_persistsAndUpdatesState() {
        val fakePreferences = FakePreferences()
        val viewModel = GeneralSettingsViewModel(fakePreferences, FakeSystemThemeStateProvider())

        viewModel.setKeepScreenOnEnabled(true)

        assertTrue(fakePreferences.currentGeneralSettings.keepScreenOnEnabled)
        assertTrue(viewModel.keepScreenOnEnabled.value ?: false)
    }

    @Test
    fun setVolumeBoostEnabled_persistsAndUpdatesState() {
        val fakePreferences = FakePreferences()
        val viewModel = GeneralSettingsViewModel(fakePreferences, FakeSystemThemeStateProvider())

        viewModel.setVolumeBoostEnabled(true)

        assertTrue(fakePreferences.currentGeneralSettings.volumeBoostEnabled)
        assertTrue(viewModel.volumeBoostEnabled.value ?: false)
    }

    @Test
    fun defaultSettings_hasCorrectDefaults() {
        val settings = GeneralSettings()

        assertEquals(AppThemeMode.LIGHT, settings.themeMode)
        assertFalse(settings.keepScreenOnEnabled)
        assertFalse(settings.volumeBoostEnabled)
    }

    @Test
    fun appThemeMode_fromStorage_invalidValueFallsBackToFollowSystem() {
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage(null))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage(""))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage("INVALID"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage("unknown"))
    }

    @Test
    fun appThemeMode_fromStorage_validValuesParsedCorrectly() {
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage("LIGHT"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStorage("DARK"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage("FOLLOW_SYSTEM"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorage("light"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStorage("dark"))
    }

    @Test
    fun shouldApplyThemeChange_returnsFalseWhenEffectiveThemeStaysLight() {
        val shouldApply = GeneralSettingsViewModel.shouldApplyThemeChange(
            previousThemeMode = AppThemeMode.LIGHT,
            targetThemeMode = AppThemeMode.LIGHT,
            systemIsDark = false
        )

        assertFalse(shouldApply)
    }

    @Test
    fun shouldApplyThemeChange_returnsTrueWhenEffectiveThemeChanges() {
        val shouldApply = GeneralSettingsViewModel.shouldApplyThemeChange(
            previousThemeMode = AppThemeMode.DARK,
            targetThemeMode = AppThemeMode.LIGHT,
            systemIsDark = true
        )

        assertTrue(shouldApply)
    }

    private class FakePreferences(
        var currentGeneralSettings: GeneralSettings = GeneralSettings()
    ) : Preferences {
        override fun getAccessToken(): String? = null
        override fun setAccessToken(token: String) = Unit
        override fun deleteAccessToken() = Unit
        override fun getRefreshToken(): String? = null
        override fun setRefreshToken(token: String) = Unit
        override fun deleteRefreshToken() = Unit
        override fun getTunerSettings(): TunerSettings = TunerSettings()
        override fun setTunerReferenceA4(referenceA4Hz: Int) = Unit
        override fun setTunerSensitivity(sensitivity: TuningSensitivity) = Unit
        override fun setTunerDisplayMode(displayMode: TunerDisplayMode) = Unit
        override fun getMetronomeSettings(): MetronomeSettings = MetronomeSettings()
        override fun setMetronomeLastBpm(lastBpm: Int) = Unit
        override fun setMetronomeSoundType(soundType: MetronomeSoundType) = Unit
        override fun setMetronomeAccentEnabled(enabled: Boolean) = Unit
        override fun setMetronomeVibrationEnabled(enabled: Boolean) = Unit
        override fun setMetronomeAccentVibrationEnabled(enabled: Boolean) = Unit
        override fun setMetronomeRegularVibrationEnabled(enabled: Boolean) = Unit

        override fun getGeneralSettings(): GeneralSettings = currentGeneralSettings

        override fun setGeneralThemeMode(themeMode: AppThemeMode) {
            currentGeneralSettings = currentGeneralSettings.copy(themeMode = themeMode)
        }

        override fun setGeneralKeepScreenOnEnabled(enabled: Boolean) {
            currentGeneralSettings = currentGeneralSettings.copy(keepScreenOnEnabled = enabled)
        }

        override fun setGeneralVolumeBoostEnabled(enabled: Boolean) {
            currentGeneralSettings = currentGeneralSettings.copy(volumeBoostEnabled = enabled)
        }
    }

    private class FakeSystemThemeStateProvider(
        private val systemIsDark: Boolean = false
    ) : SystemThemeStateProvider() {
        override fun isSystemDarkTheme(): Boolean = systemIsDark
    }
}
