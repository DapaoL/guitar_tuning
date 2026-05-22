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

class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * 验证加载设置后会正确填充调音页面状态。
     */
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

    /**
     * 验证非法 A4 输入会被拒绝，且不会改动已保存值。
     */
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

    /**
     * 验证合法 A4 输入会被保存，并清除错误状态。
     */
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

    /**
     * 验证切换灵敏度时会同步更新持久化结果与页面状态。
     */
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

    /**
     * 验证切换显示模式时会同步更新持久化结果与页面状态。
     */
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

        /**
         * 返回访问令牌。
         */
        override fun getAccessToken(): String? = null

        /**
         * 保存访问令牌。
         */
        override fun setAccessToken(token: String) = Unit

        /**
         * 删除访问令牌。
         */
        override fun deleteAccessToken() = Unit

        /**
         * 返回刷新令牌。
         */
        override fun getRefreshToken(): String? = null

        /**
         * 保存刷新令牌。
         */
        override fun setRefreshToken(token: String) = Unit

        /**
         * 删除刷新令牌。
         */
        override fun deleteRefreshToken() = Unit

        /**
         * 返回当前调音设置。
         */
        override fun getTunerSettings(): TunerSettings = currentSettings

        /**
         * 保存 A4 参考频率。
         */
        override fun setTunerReferenceA4(referenceA4Hz: Int) {
            lastSavedReferenceA4 = referenceA4Hz
            currentSettings = currentSettings.copy(referenceA4Hz = referenceA4Hz)
        }

        /**
         * 保存灵敏度。
         */
        override fun setTunerSensitivity(sensitivity: TuningSensitivity) {
            lastSavedSensitivity = sensitivity
            currentSettings = currentSettings.copy(sensitivity = sensitivity)
        }

        /**
         * 保存显示模式。
         */
        override fun setTunerDisplayMode(displayMode: TunerDisplayMode) {
            lastSavedDisplayMode = displayMode
            currentSettings = currentSettings.copy(displayMode = displayMode)
        }

        override fun getMetronomeSettings(): MetronomeSettings = MetronomeSettings()

        override fun setMetronomeLastBpm(lastBpm: Int) = Unit

        override fun setMetronomeSoundType(soundType: MetronomeSoundType) = Unit

        override fun setMetronomeTimeSignature(timeSignature: MetronomeTimeSignature) = Unit

        override fun setMetronomeAccentEnabled(enabled: Boolean) = Unit

        override fun setMetronomeVibrationEnabled(enabled: Boolean) = Unit

        override fun setMetronomeAccentVibrationEnabled(enabled: Boolean) = Unit

        override fun setMetronomeRegularVibrationEnabled(enabled: Boolean) = Unit

        override fun getGeneralSettings(): GeneralSettings = GeneralSettings()

        override fun setGeneralThemeMode(themeMode: AppThemeMode) = Unit

        override fun setGeneralKeepScreenOnEnabled(enabled: Boolean) = Unit

        override fun setGeneralVolumeBoostEnabled(enabled: Boolean) = Unit
    }
}
