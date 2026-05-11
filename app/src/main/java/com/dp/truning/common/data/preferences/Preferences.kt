package com.dp.truning.common.data.preferences

import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity

interface Preferences {

    /**
     * 获取 access token。
     */
    fun getAccessToken(): String?

    /**
     * 设置 access token。
     */
    fun setAccessToken(token: String)
    /**
     * 删除 access token。
     */
    fun deleteAccessToken()

    /**
     * 获取 refresh token。
     */
    fun getRefreshToken(): String?

    /**
     * 设置 refresh token。
     */
    fun setRefreshToken(token: String)

    /**
     * 删除 refresh token。
     */
    fun deleteRefreshToken()

    /**
     * 获取 tuner settings。
     */
    fun getTunerSettings(): TunerSettings

    /**
     * 设置 tuner reference A 4。
     */
    fun setTunerReferenceA4(referenceA4Hz: Int)

    /**
     * 设置 tuner sensitivity。
     */
    fun setTunerSensitivity(sensitivity: TuningSensitivity)

    /**
     * 设置 tuner display mode。
     */
    fun setTunerDisplayMode(displayMode: TunerDisplayMode)

}
