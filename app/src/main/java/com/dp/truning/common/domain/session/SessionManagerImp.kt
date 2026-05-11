package com.wiseria.common.domain.session

import com.dp.truning.common.data.preferences.Preferences
import javax.inject.Inject

class SessionManagerImp @Inject constructor(private val preferences: Preferences) : SessionManager {

    private var accessToken: String? = null

    private var refreshToken: String? = null

    /**
     * 获取 access token。
     */
    override fun getAccessToken(): String? {
        if (accessToken == null) {
            accessToken = preferences.getAccessToken()
        }
        return accessToken
    }

    /**
     * 设置 access token。
     */
    override fun setAccessToken(token: String) {
        accessToken = token
    }

    /**
     * 保存 access token。
     */
    override fun saveAccessToken() {
        accessToken?.let { token ->
            preferences.setAccessToken(token)
        }
    }

    /**
     * 删除 access token。
     */
    override fun deleteAccessToken() {
        accessToken = null
        preferences.deleteAccessToken()
    }

    /**
     * 获取 refresh token。
     */
    override fun getRefreshToken(): String? {
        if (refreshToken == null) {
            refreshToken = preferences.getRefreshToken()
        }
        return refreshToken
    }

    /**
     * 设置 refresh token。
     */
    override fun setRefreshToken(token: String) {
        refreshToken = token
    }

    /**
     * 保存 refresh token。
     */
    override fun saveRefreshToken() {
        refreshToken?.let { token ->
            preferences.setRefreshToken(token)
        }
    }

    /**
     * 删除 refresh token。
     */
    override fun deleteRefreshToken() {
        refreshToken = null
        preferences.deleteRefreshToken()
    }

    /**
     * 处理 log out 相关逻辑。
     */
    override fun logOut() {
        deleteAccessToken()
        deleteRefreshToken()
    }

    /**
     * 保存 tokens。
     */
    override fun saveTokens(accessToken: String, refreshToken: String) {
        setAccessToken(accessToken)
        saveAccessToken()
        setRefreshToken(refreshToken)
        saveRefreshToken()
    }
}