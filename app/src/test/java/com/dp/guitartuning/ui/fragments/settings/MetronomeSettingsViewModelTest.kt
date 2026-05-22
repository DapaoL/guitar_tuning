package com.dp.guitartuning.ui.fragments.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dp.guitartuning.common.data.preferences.Preferences
import com.dp.guitartuning.domain.model.AppThemeMode
import com.dp.guitartuning.domain.model.GeneralSettings
import com.dp.guitartuning.domain.model.MetronomeSettings
import com.dp.guitartuning.domain.model.MetronomeSoundType
import com.dp.guitartuning.domain.model.MetronomeTimeSignature
import com.dp.guitartuning.domain.model.TunerDisplayMode
import com.dp.guitartuning.domain.model.TunerSettings
import com.dp.guitartuning.domain.model.TuningSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MetronomeSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun loadSettings_populatesUiStateFromSavedSettings() {
        val fakePreferences = FakePreferences(
            currentMetronomeSettings = MetronomeSettings(
                lastBpm = 156,
                soundType = MetronomeSoundType.DRUM,
                accentEnabled = false,
                vibrationEnabled = true,
                accentVibrationEnabled = false,
                regularVibrationEnabled = true
            )
        )
        val viewModel = MetronomeSettingsViewModel(fakePreferences)

        viewModel.loadSettings()

        assertEquals(156, viewModel.defaultBpm.value)
        assertEquals(MetronomeSoundType.DRUM, viewModel.selectedSoundType.value)
        assertFalse(viewModel.accentEnabled.value ?: true)
        assertTrue(viewModel.vibrationEnabled.value ?: false)
        assertFalse(viewModel.accentVibrationEnabled.value ?: true)
        assertTrue(viewModel.regularVibrationEnabled.value ?: false)
        assertTrue(viewModel.vibrationOptionsEnabled.value ?: false)
    }

    @Test
    fun selectSoundType_persistsAndUpdatesState() {
        val fakePreferences = FakePreferences()
        val viewModel = MetronomeSettingsViewModel(fakePreferences)

        viewModel.selectSoundType(MetronomeSoundType.BEEP)

        assertEquals(MetronomeSoundType.BEEP, fakePreferences.currentMetronomeSettings.soundType)
        assertEquals(MetronomeSoundType.BEEP, viewModel.selectedSoundType.value)
    }

    @Test
    fun setVibrationEnabled_updatesDependentUiStateAndKeepsChildValues() {
        val fakePreferences = FakePreferences(
            currentMetronomeSettings = MetronomeSettings(
                vibrationEnabled = true,
                accentVibrationEnabled = true,
                regularVibrationEnabled = true
            )
        )
        val viewModel = MetronomeSettingsViewModel(fakePreferences)
        viewModel.loadSettings()

        viewModel.setVibrationEnabled(false)

        assertFalse(fakePreferences.currentMetronomeSettings.vibrationEnabled)
        assertTrue(fakePreferences.currentMetronomeSettings.accentVibrationEnabled)
        assertTrue(fakePreferences.currentMetronomeSettings.regularVibrationEnabled)
        assertFalse(viewModel.vibrationEnabled.value ?: true)
        assertFalse(viewModel.vibrationOptionsEnabled.value ?: true)
    }

    private class FakePreferences(
        var currentMetronomeSettings: MetronomeSettings = MetronomeSettings()
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

        override fun getMetronomeSettings(): MetronomeSettings = currentMetronomeSettings

        override fun setMetronomeLastBpm(lastBpm: Int) {
            currentMetronomeSettings = currentMetronomeSettings.copy(lastBpm = lastBpm)
        }

        override fun setMetronomeSoundType(soundType: MetronomeSoundType) {
            currentMetronomeSettings = currentMetronomeSettings.copy(soundType = soundType)
        }

        override fun setMetronomeTimeSignature(timeSignature: MetronomeTimeSignature) {
            currentMetronomeSettings = currentMetronomeSettings.copy(timeSignature = timeSignature)
        }

        override fun setMetronomeAccentEnabled(enabled: Boolean) {
            currentMetronomeSettings = currentMetronomeSettings.copy(accentEnabled = enabled)
        }

        override fun setMetronomeVibrationEnabled(enabled: Boolean) {
            currentMetronomeSettings = currentMetronomeSettings.copy(vibrationEnabled = enabled)
        }

        override fun setMetronomeAccentVibrationEnabled(enabled: Boolean) {
            currentMetronomeSettings = currentMetronomeSettings.copy(accentVibrationEnabled = enabled)
        }

        override fun setMetronomeRegularVibrationEnabled(enabled: Boolean) {
            currentMetronomeSettings = currentMetronomeSettings.copy(regularVibrationEnabled = enabled)
        }

        override fun getGeneralSettings(): GeneralSettings = GeneralSettings()

        override fun setGeneralThemeMode(themeMode: AppThemeMode) = Unit

        override fun setGeneralKeepScreenOnEnabled(enabled: Boolean) = Unit

        override fun setGeneralVolumeBoostEnabled(enabled: Boolean) = Unit
    }
}
