package com.dp.truning.common.data.preferences

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

}
