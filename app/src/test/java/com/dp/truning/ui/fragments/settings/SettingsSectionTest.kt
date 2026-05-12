package com.dp.truning.ui.fragments.settings

import com.dp.truning.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSectionTest {

    /**
     * 验证顶层设置分区顺序与设计约定一致。
     */
    @Test
    fun topLevelSections_keepApprovedOrder() {
        assertEquals(
            listOf(
                SettingsSection.TUNER,
                SettingsSection.METRONOME,
                SettingsSection.GENERAL,
                SettingsSection.SUPPORT,
                SettingsSection.ABOUT
            ),
            SettingsSection.topLevel()
        )
    }

    /**
     * 验证调音和节拍器分区不是占位页。
     */
    @Test
    fun tunerAndMetronomeSections_areNotPlaceholder() {
        assertFalse(SettingsSection.TUNER.usesPlaceholder)
        assertFalse(SettingsSection.METRONOME.usesPlaceholder)
        assertTrue(SettingsSection.GENERAL.usesPlaceholder)
        assertTrue(SettingsSection.SUPPORT.usesPlaceholder)
        assertTrue(SettingsSection.ABOUT.usesPlaceholder)
    }

    /**
     * 验证顶层设置分区使用了正确的标题资源。
     */
    @Test
    fun topLevelSections_mapToExpectedTitleResources() {
        assertEquals(R.string.settings_item_tuner, SettingsSection.TUNER.titleRes)
        assertEquals(R.string.settings_item_metronome, SettingsSection.METRONOME.titleRes)
        assertEquals(R.string.settings_item_general, SettingsSection.GENERAL.titleRes)
        assertEquals(R.string.settings_item_support, SettingsSection.SUPPORT.titleRes)
        assertEquals(R.string.settings_item_about, SettingsSection.ABOUT.titleRes)
    }
}
