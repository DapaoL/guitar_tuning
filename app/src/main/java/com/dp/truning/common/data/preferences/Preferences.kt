package com.dp.truning.common.data.preferences

import com.dp.truning.domain.model.AppThemeMode
import com.dp.truning.domain.model.GeneralSettings
import com.dp.truning.domain.model.MetronomeSettings
import com.dp.truning.domain.model.MetronomeSoundType
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity

interface Preferences {

    fun getAccessToken(): String?

    fun setAccessToken(token: String)

    fun deleteAccessToken()

    fun getRefreshToken(): String?

    fun setRefreshToken(token: String)

    fun deleteRefreshToken()

    fun getTunerSettings(): TunerSettings

    fun setTunerReferenceA4(referenceA4Hz: Int)

    fun setTunerSensitivity(sensitivity: TuningSensitivity)

    fun setTunerDisplayMode(displayMode: TunerDisplayMode)

    fun getMetronomeSettings(): MetronomeSettings

    fun setMetronomeLastBpm(lastBpm: Int)

    fun setMetronomeSoundType(soundType: MetronomeSoundType)

    fun setMetronomeAccentEnabled(enabled: Boolean)

    fun setMetronomeVibrationEnabled(enabled: Boolean)

    fun setMetronomeAccentVibrationEnabled(enabled: Boolean)

    fun setMetronomeRegularVibrationEnabled(enabled: Boolean)

    fun getGeneralSettings(): GeneralSettings

    fun setGeneralThemeMode(themeMode: AppThemeMode)

    fun setGeneralKeepScreenOnEnabled(enabled: Boolean)

    fun setGeneralVolumeBoostEnabled(enabled: Boolean)
}

