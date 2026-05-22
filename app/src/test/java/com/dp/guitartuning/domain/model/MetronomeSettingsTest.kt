package com.dp.guitartuning.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetronomeSettingsTest {

    @Test
    fun defaultsAndBounds_matchTaskContract() {
        val defaults = MetronomeSettings()

        assertEquals(120, MetronomeSettings.DEFAULT_BPM)
        assertEquals(30, MetronomeSettings.MIN_BPM)
        assertEquals(300, MetronomeSettings.MAX_BPM)
        assertEquals(120, defaults.lastBpm)
        assertEquals(MetronomeSoundType.WOOD_BLOCK, defaults.soundType)
        assertEquals(MetronomeTimeSignature.FOUR_FOUR, defaults.timeSignature)
        assertTrue(defaults.accentEnabled)
        assertFalse(defaults.vibrationEnabled)
        assertTrue(defaults.accentVibrationEnabled)
        assertFalse(defaults.regularVibrationEnabled)
    }

    @Test
    fun sanitizeBpm_fallsBackWhenValueIsMissingOrOutOfRange() {
        assertEquals(120, MetronomeSettings.sanitizeBpm(null))
        assertEquals(120, MetronomeSettings.sanitizeBpm(29))
        assertEquals(120, MetronomeSettings.sanitizeBpm(301))
        assertEquals(144, MetronomeSettings.sanitizeBpm(144))
    }

    @Test
    fun soundType_fromStorageFallsBackToWoodBlock() {
        assertEquals(MetronomeSoundType.WOOD_BLOCK, MetronomeSoundType.fromStorage("unknown"))
        assertEquals(MetronomeSoundType.DRUM, MetronomeSoundType.fromStorage("drum"))
    }

    @Test
    fun timeSignature_fromStorageFallsBackToFourFour() {
        assertEquals(MetronomeTimeSignature.FOUR_FOUR, MetronomeTimeSignature.fromStorage(null))
        assertEquals(MetronomeTimeSignature.FOUR_FOUR, MetronomeTimeSignature.fromStorage("unknown"))
        assertEquals(MetronomeTimeSignature.THREE_FOUR, MetronomeTimeSignature.fromStorage("three_four"))
    }

    @Test
    fun timeSignature_presetsContainCommonPracticeMeters() {
        assertEquals(
            listOf(
                MetronomeTimeSignature.FOUR_FOUR,
                MetronomeTimeSignature.THREE_FOUR,
                MetronomeTimeSignature.TWO_FOUR,
                MetronomeTimeSignature.SIX_EIGHT,
                MetronomeTimeSignature.FIVE_FOUR,
                MetronomeTimeSignature.SEVEN_EIGHT,
                MetronomeTimeSignature.TWELVE_EIGHT
            ),
            MetronomeTimeSignature.PRESETS
        )
    }

    @Test
    fun playbackConfig_usesAccentAndVibrationRules() {
        val accentConfig = MetronomePlaybackConfig(
            accentEnabled = true,
            vibrationEnabled = true,
            accentVibrationEnabled = true,
            regularVibrationEnabled = false
        )
        val regularConfig = accentConfig.copy(accentEnabled = false)
        val noVibrationConfig = accentConfig.copy(vibrationEnabled = false)

        assertEquals(MetronomeBeatType.ACCENT, accentConfig.beatTypeFor(0))
        assertEquals(MetronomeBeatType.REGULAR, accentConfig.beatTypeFor(1))
        assertTrue(accentConfig.shouldVibrate(0))
        assertFalse(accentConfig.shouldVibrate(1))

        assertEquals(MetronomeBeatType.REGULAR, regularConfig.beatTypeFor(0))
        assertFalse(regularConfig.shouldVibrate(0))
        assertFalse(noVibrationConfig.shouldVibrate(0))
    }

    @Test
    fun playbackConfig_usesSelectedTimeSignatureForAccentCycle() {
        val threeFourConfig = MetronomePlaybackConfig(
            timeSignature = MetronomeTimeSignature.THREE_FOUR,
            accentEnabled = true
        )
        val sixEightConfig = threeFourConfig.copy(timeSignature = MetronomeTimeSignature.SIX_EIGHT)

        assertEquals(MetronomeBeatType.ACCENT, threeFourConfig.beatTypeFor(0))
        assertEquals(MetronomeBeatType.REGULAR, threeFourConfig.beatTypeFor(1))
        assertEquals(MetronomeBeatType.REGULAR, threeFourConfig.beatTypeFor(2))
        assertEquals(MetronomeBeatType.ACCENT, threeFourConfig.beatTypeFor(3))

        assertEquals(MetronomeBeatType.ACCENT, sixEightConfig.beatTypeFor(0))
        assertEquals(MetronomeBeatType.REGULAR, sixEightConfig.beatTypeFor(5))
        assertEquals(MetronomeBeatType.ACCENT, sixEightConfig.beatTypeFor(6))
    }
}
