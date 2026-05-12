package com.dp.guitartuning.ui.fragments.settings

interface SettingsNavigationHost {
    /**
     * 打开指定的设置分区。
     */
    fun openSection(section: SettingsSection)

    /**
     * 从子设置页返回上一层。
     */
    fun goBackFromSettingsChild()
}
