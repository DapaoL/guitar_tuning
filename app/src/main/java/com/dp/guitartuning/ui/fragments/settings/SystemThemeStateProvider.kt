package com.dp.guitartuning.ui.fragments.settings

import android.content.res.Configuration
import android.content.res.Resources
import javax.inject.Inject

open class SystemThemeStateProvider @Inject constructor() {

    open fun isSystemDarkTheme(): Boolean {
        val nightModeFlags = Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
