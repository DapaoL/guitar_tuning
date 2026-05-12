package com.dp.truning.common.data.preferences

import android.content.Context
import com.dp.truning.domain.model.AppThemeMode
import com.dp.truning.domain.model.GeneralSettings
import com.dp.truning.domain.model.MetronomeSettings
import com.dp.truning.domain.model.MetronomeSoundType
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity
import javax.inject.Inject

class PreferenceManager @Inject constructor(
    context: Context
) : SharedPreferences(context), Preferences {

    override fun getPrefName() = "WiseriaPrefs"

    override fun getAccessToken() = preferences.getString(PreferencesConstants.KEY_ACCESS_TOKEN, "")

    override fun setAccessToken(token: String) {
        preferences.edit().putString(PreferencesConstants.KEY_ACCESS_TOKEN, token).apply()
    }

    override fun deleteAccessToken() {
        preferences.edit().remove(PreferencesConstants.KEY_ACCESS_TOKEN).apply()
    }

    override fun getRefreshToken() =
        preferences.getString(PreferencesConstants.KEY_REFRESH_TOKEN, "")

    override fun setRefreshToken(token: String) {
        preferences.edit().putString(PreferencesConstants.KEY_REFRESH_TOKEN, token).apply()
    }

    override fun deleteRefreshToken() {
        preferences.edit().remove(PreferencesConstants.KEY_REFRESH_TOKEN).apply()
    }

    override fun getTunerSettings(): TunerSettings {
        val referenceA4 = safeGetInt(
            PreferencesConstants.KEY_TUNER_REFERENCE_A4,
            TunerSettings.DEFAULT_REFERENCE_A4_HZ
        )
        val sensitivity = safeGetString(PreferencesConstants.KEY_TUNER_SENSITIVITY, null)
        val displayMode = safeGetString(PreferencesConstants.KEY_TUNER_DISPLAY_MODE, null)

        return TunerSettings(
            referenceA4Hz = TunerSettings.sanitizeReferenceA4(referenceA4),
            sensitivity = TuningSensitivity.fromStorage(sensitivity),
            displayMode = TunerDisplayMode.fromStorage(displayMode)
        )
    }

    override fun setTunerReferenceA4(referenceA4Hz: Int) {
        preferences.edit()
            .putInt(
                PreferencesConstants.KEY_TUNER_REFERENCE_A4,
                TunerSettings.sanitizeReferenceA4(referenceA4Hz)
            )
            .apply()
    }

    override fun setTunerSensitivity(sensitivity: TuningSensitivity) {
        preferences.edit()
            .putString(PreferencesConstants.KEY_TUNER_SENSITIVITY, sensitivity.name)
            .apply()
    }

    override fun setTunerDisplayMode(displayMode: TunerDisplayMode) {
        preferences.edit()
            .putString(PreferencesConstants.KEY_TUNER_DISPLAY_MODE, displayMode.name)
            .apply()
    }

    override fun getMetronomeSettings(): MetronomeSettings {
        val lastBpm = safeGetInt(
            PreferencesConstants.KEY_METRONOME_LAST_BPM,
            MetronomeSettings.DEFAULT_BPM
        )
        val soundType = safeGetString(PreferencesConstants.KEY_METRONOME_SOUND_TYPE, null)

        return MetronomeSettings(
            lastBpm = MetronomeSettings.sanitizeBpm(lastBpm),
            soundType = MetronomeSoundType.fromStorage(soundType),
            accentEnabled = safeGetBoolean(
                PreferencesConstants.KEY_METRONOME_ACCENT_ENABLED,
                true
            ),
            vibrationEnabled = safeGetBoolean(
                PreferencesConstants.KEY_METRONOME_VIBRATION_ENABLED,
                false
            ),
            accentVibrationEnabled = safeGetBoolean(
                PreferencesConstants.KEY_METRONOME_ACCENT_VIBRATION_ENABLED,
                true
            ),
            regularVibrationEnabled = safeGetBoolean(
                PreferencesConstants.KEY_METRONOME_REGULAR_VIBRATION_ENABLED,
                false
            )
        )
    }

    override fun setMetronomeLastBpm(lastBpm: Int) {
        preferences.edit()
            .putInt(
                PreferencesConstants.KEY_METRONOME_LAST_BPM,
                MetronomeSettings.sanitizeBpm(lastBpm)
            )
            .apply()
    }

    override fun setMetronomeSoundType(soundType: MetronomeSoundType) {
        preferences.edit()
            .putString(PreferencesConstants.KEY_METRONOME_SOUND_TYPE, soundType.name)
            .apply()
    }

    override fun setMetronomeAccentEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PreferencesConstants.KEY_METRONOME_ACCENT_ENABLED, enabled)
            .apply()
    }

    override fun setMetronomeVibrationEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PreferencesConstants.KEY_METRONOME_VIBRATION_ENABLED, enabled)
            .apply()
    }

    override fun setMetronomeAccentVibrationEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PreferencesConstants.KEY_METRONOME_ACCENT_VIBRATION_ENABLED, enabled)
            .apply()
    }

    override fun setMetronomeRegularVibrationEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PreferencesConstants.KEY_METRONOME_REGULAR_VIBRATION_ENABLED, enabled)
            .apply()
    }

    override fun getGeneralSettings(): GeneralSettings {
        val themeMode = safeGetString(PreferencesConstants.KEY_GENERAL_THEME_MODE, null)
        return GeneralSettings(
            themeMode = AppThemeMode.fromStorage(themeMode),
            keepScreenOnEnabled = safeGetBoolean(PreferencesConstants.KEY_GENERAL_KEEP_SCREEN_ON, false),
            volumeBoostEnabled = safeGetBoolean(PreferencesConstants.KEY_GENERAL_VOLUME_BOOST, false)
        )
    }

    override fun setGeneralThemeMode(themeMode: AppThemeMode) {
        preferences.edit()
            .putString(PreferencesConstants.KEY_GENERAL_THEME_MODE, themeMode.name)
            .apply()
    }

    override fun setGeneralKeepScreenOnEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PreferencesConstants.KEY_GENERAL_KEEP_SCREEN_ON, enabled)
            .apply()
    }

    override fun setGeneralVolumeBoostEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PreferencesConstants.KEY_GENERAL_VOLUME_BOOST, enabled)
            .apply()
    }

    private fun safeGetInt(key: String, defaultValue: Int): Int {
        return try {
            preferences.getInt(key, defaultValue)
        } catch (_: ClassCastException) {
            defaultValue
        }
    }

    private fun safeGetString(key: String, defaultValue: String?): String? {
        return try {
            preferences.getString(key, defaultValue)
        } catch (_: ClassCastException) {
            defaultValue
        }
    }

    private fun safeGetBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            preferences.getBoolean(key, defaultValue)
        } catch (_: ClassCastException) {
            defaultValue
        }
    }
}
