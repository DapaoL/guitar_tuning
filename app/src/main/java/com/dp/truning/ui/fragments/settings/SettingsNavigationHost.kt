package com.dp.truning.ui.fragments.settings

interface SettingsNavigationHost {
    /**
     * 打开 section。
     */
    fun openSection(section: SettingsSection)
    /**
     * 返回 from settings child。
     */
    fun goBackFromSettingsChild()
}
