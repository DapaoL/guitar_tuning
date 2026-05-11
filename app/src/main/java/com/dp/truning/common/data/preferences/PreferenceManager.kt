package com.dp.truning.common.data.preferences

import android.content.Context
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

}
