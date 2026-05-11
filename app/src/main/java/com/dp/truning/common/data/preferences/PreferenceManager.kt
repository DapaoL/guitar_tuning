package com.dp.truning.common.data.preferences

import android.content.Context
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity
import javax.inject.Inject

class PreferenceManager @Inject constructor(
    context: Context
) : SharedPreferences(context), Preferences {

    /**
     * 获取 pref name。
     */
    override fun getPrefName() = "WiseriaPrefs"

    /**
     * 获取 access token。
     */
    override fun getAccessToken() = preferences.getString(PreferencesConstants.KEY_ACCESS_TOKEN, "")


    /**
     * 设置 access token。
     */
    override fun setAccessToken(token: String) {
        preferences.edit().putString(PreferencesConstants.KEY_ACCESS_TOKEN, token).apply()
    }

    /**
     * 删除 access token。
     */
    override fun deleteAccessToken() {
        preferences.edit().remove(PreferencesConstants.KEY_ACCESS_TOKEN).apply()
    }

    /**
     * 获取 refresh token。
     */
    override fun getRefreshToken() =
        preferences.getString(PreferencesConstants.KEY_REFRESH_TOKEN, "")

    /**
     * 设置 refresh token。
     */
    override fun setRefreshToken(token: String) {
        preferences.edit().putString(PreferencesConstants.KEY_REFRESH_TOKEN, token).apply()
    }

    /**
     * 删除 refresh token。
     */
    override fun deleteRefreshToken() {
        preferences.edit().remove(PreferencesConstants.KEY_REFRESH_TOKEN).apply()
    }

    /**
     * 获取 tuner settings。
     */
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

    /**
     * 设置 tuner reference A 4。
     */
    override fun setTunerReferenceA4(referenceA4Hz: Int) {
        preferences.edit()
            .putInt(
                PreferencesConstants.KEY_TUNER_REFERENCE_A4,
                TunerSettings.sanitizeReferenceA4(referenceA4Hz)
            )
            .apply()
    }

    /**
     * 设置 tuner sensitivity。
     */
    override fun setTunerSensitivity(sensitivity: TuningSensitivity) {
        preferences.edit()
            .putString(PreferencesConstants.KEY_TUNER_SENSITIVITY, sensitivity.name)
            .apply()
    }

    /**
     * 设置 tuner display mode。
     */
    override fun setTunerDisplayMode(displayMode: TunerDisplayMode) {
        preferences.edit()
            .putString(PreferencesConstants.KEY_TUNER_DISPLAY_MODE, displayMode.name)
            .apply()
    }

    /**
     * 处理 safe get int 相关逻辑。
     */
    private fun safeGetInt(key: String, defaultValue: Int): Int {
        return try {
            preferences.getInt(key, defaultValue)
        } catch (_: ClassCastException) {
            defaultValue
        }
    }

    /**
     * 处理 safe get string 相关逻辑。
     */
    private fun safeGetString(key: String, defaultValue: String?): String? {
        return try {
            preferences.getString(key, defaultValue)
        } catch (_: ClassCastException) {
            defaultValue
        }
    }

}
