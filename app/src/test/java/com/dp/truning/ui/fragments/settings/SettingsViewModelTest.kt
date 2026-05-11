package com.dp.truning.ui.fragments.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dp.truning.common.data.preferences.Preferences
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun loadSettings_populatesUiStateFromSavedSettings() {
        val fakePreferences = FakePreferences(
            initialSettings = TunerSettings(
                referenceA4Hz = 442,
                sensitivity = TuningSensitivity.LOW,
                displayMode = TunerDisplayMode.NUMERIC
            )
        )
        val viewModel = SettingsViewModel(fakePreferences)

        viewModel.loadSettings()

        assertEquals("442", viewModel.referenceA4Input.value)
        assertFalse(viewModel.referenceA4HasError.value ?: true)
        assertEquals(TuningSensitivity.LOW, viewModel.selectedSensitivity.value)
        assertEquals(TunerDisplayMode.NUMERIC, viewModel.selectedDisplayMode.value)
    }

    @Test
    fun saveReferenceA4_invalidValue_rejectedAndPersistedValueUnchanged() {
        val fakePreferences = FakePreferences(
            initialSettings = TunerSettings(referenceA4Hz = 440)
        )
        val viewModel = SettingsViewModel(fakePreferences)
        viewModel.loadSettings()

        viewModel.onReferenceA4Changed("499")
        viewModel.saveReferenceA4()

        assertTrue(viewModel.referenceA4HasError.value ?: false)
        assertEquals(440, fakePreferences.currentSettings.referenceA4Hz)
        assertEquals(null, fakePreferences.lastSavedReferenceA4)
    }

    @Test
    fun saveReferenceA4_validValue_persistsAndClearsError() {
        val fakePreferences = FakePreferences(
            initialSettings = TunerSettings(referenceA4Hz = 440)
        )
        val viewModel = SettingsViewModel(fakePreferences)
        viewModel.loadSettings()
        viewModel.onReferenceA4Changed("445")

        viewModel.saveReferenceA4()

        assertEquals(445, fakePreferences.currentSettings.referenceA4Hz)
        assertEquals(445, fakePreferences.lastSavedReferenceA4)
        assertEquals("445", viewModel.referenceA4Input.value)
        assertFalse(viewModel.referenceA4HasError.value ?: true)
    }

    @Test
    fun selectSensitivity_persistsAndUpdatesState() {
        val fakePreferences = FakePreferences(
            initialSettings = TunerSettings(sensitivity = TuningSensitivity.MEDIUM)
        )
        val viewModel = SettingsViewModel(fakePreferences)

        viewModel.selectSensitivity(TuningSensitivity.HIGH)

        assertEquals(TuningSensitivity.HIGH, fakePreferences.currentSettings.sensitivity)
        assertEquals(TuningSensitivity.HIGH, fakePreferences.lastSavedSensitivity)
        assertEquals(TuningSensitivity.HIGH, viewModel.selectedSensitivity.value)
    }

    @Test
    fun selectDisplayMode_persistsAndUpdatesState() {
        val fakePreferences = FakePreferences(
            initialSettings = TunerSettings(displayMode = TunerDisplayMode.POINTER)
        )
        val viewModel = SettingsViewModel(fakePreferences)

        viewModel.selectDisplayMode(TunerDisplayMode.GAUGE)

        assertEquals(TunerDisplayMode.GAUGE, fakePreferences.currentSettings.displayMode)
        assertEquals(TunerDisplayMode.GAUGE, fakePreferences.lastSavedDisplayMode)
        assertEquals(TunerDisplayMode.GAUGE, viewModel.selectedDisplayMode.value)
    }

    private class FakePreferences(
        initialSettings: TunerSettings
    ) : Preferences {
        var currentSettings: TunerSettings = initialSettings
        var lastSavedReferenceA4: Int? = null
        var lastSavedSensitivity: TuningSensitivity? = null
        var lastSavedDisplayMode: TunerDisplayMode? = null

        override fun getAccessToken(): String? = null

        override fun setAccessToken(token: String) = Unit

        override fun deleteAccessToken() = Unit

        override fun getRefreshToken(): String? = null

        override fun setRefreshToken(token: String) = Unit

        override fun deleteRefreshToken() = Unit

        override fun getTunerSettings(): TunerSettings = currentSettings

        override fun setTunerReferenceA4(referenceA4Hz: Int) {
            lastSavedReferenceA4 = referenceA4Hz
            currentSettings = currentSettings.copy(referenceA4Hz = referenceA4Hz)
        }

        override fun setTunerSensitivity(sensitivity: TuningSensitivity) {
            lastSavedSensitivity = sensitivity
            currentSettings = currentSettings.copy(sensitivity = sensitivity)
        }

        override fun setTunerDisplayMode(displayMode: TunerDisplayMode) {
            lastSavedDisplayMode = displayMode
            currentSettings = currentSettings.copy(displayMode = displayMode)
        }
    }
}
