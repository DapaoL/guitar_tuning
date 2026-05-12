package com.dp.truning.domain.model

enum class AppThemeMode {
    LIGHT,
    DARK,
    FOLLOW_SYSTEM;

    companion object {
        fun fromStorage(raw: String?): AppThemeMode {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: FOLLOW_SYSTEM
        }
    }
}

data class GeneralSettings(
    val themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val keepScreenOnEnabled: Boolean = false,
    val volumeBoostEnabled: Boolean = false
)
