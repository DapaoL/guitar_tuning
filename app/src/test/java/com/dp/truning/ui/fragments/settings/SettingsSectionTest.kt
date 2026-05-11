package com.dp.truning.ui.fragments.settings

import com.dp.truning.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSectionTest {

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

    @Test
    fun onlyTunerSection_isNotPlaceholder() {
        assertFalse(SettingsSection.TUNER.usesPlaceholder)
        assertTrue(SettingsSection.METRONOME.usesPlaceholder)
        assertTrue(SettingsSection.GENERAL.usesPlaceholder)
        assertTrue(SettingsSection.SUPPORT.usesPlaceholder)
        assertTrue(SettingsSection.ABOUT.usesPlaceholder)
    }

    @Test
    fun topLevelSections_mapToExpectedTitleResources() {
        assertEquals(R.string.settings_item_tuner, SettingsSection.TUNER.titleRes)
        assertEquals(R.string.settings_item_metronome, SettingsSection.METRONOME.titleRes)
        assertEquals(R.string.settings_item_general, SettingsSection.GENERAL.titleRes)
        assertEquals(R.string.settings_item_support, SettingsSection.SUPPORT.titleRes)
        assertEquals(R.string.settings_item_about, SettingsSection.ABOUT.titleRes)
    }
}
