package com.dp.truning.domain.model

enum class AppThemeMode {
    LIGHT,
    DARK;

    companion object {
        fun fromStorage(raw: String?): AppThemeMode {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: LIGHT
        }
    }
}

data class GeneralSettings(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val keepScreenOnEnabled: Boolean = false,
    val volumeBoostEnabled: Boolean = false
)
