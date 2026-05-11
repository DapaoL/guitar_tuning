package com.dp.truning.ui.fragments.settings

interface SettingsNavigationHost {
    fun openSection(section: SettingsSection)
    fun goBackFromSettingsChild()
}
