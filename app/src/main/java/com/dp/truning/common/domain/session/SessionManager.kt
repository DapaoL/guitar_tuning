package com.wiseria.common.domain.session

interface SessionManager {

    /**
     * 获取 access token。
     */
    fun getAccessToken(): String?

    /**
     * 设置 access token。
     */
    fun setAccessToken(token: String)

    /**
     * 保存 access token。
     */
    fun saveAccessToken()

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
     * 保存 refresh token。
     */
    fun saveRefreshToken()

    /**
     * 删除 refresh token。
     */
    fun deleteRefreshToken()

    /**
     * 处理 log out 相关逻辑。
     */
    fun logOut()

    /**
     * 保存 tokens。
     */
    fun saveTokens(accessToken: String, refreshToken: String)

}