package com.dp.truning.ui.fragments.settings

import com.dp.truning.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSectionTest {

    /**
     * 验证 top level sections keep approved order。
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
     * 验证 only tuner section is not placeholder。
     */
    @Test
    fun onlyTunerSection_isNotPlaceholder() {
        assertFalse(SettingsSection.TUNER.usesPlaceholder)
        assertTrue(SettingsSection.METRONOME.usesPlaceholder)
        assertTrue(SettingsSection.GENERAL.usesPlaceholder)
        assertTrue(SettingsSection.SUPPORT.usesPlaceholder)
        assertTrue(SettingsSection.ABOUT.usesPlaceholder)
    }

    /**
     * 验证 top level sections map to expected title resources。
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
