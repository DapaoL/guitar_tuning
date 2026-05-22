package com.dp.guitartuning.domain.model

enum class MetronomeSoundType {
    WOOD_BLOCK,
    CLICK,
    DRUM,
    BEEP;

    companion object {
        fun fromStorage(raw: String?): MetronomeSoundType {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: WOOD_BLOCK
        }
    }
}

enum class MetronomeBeatType {
    ACCENT,
    REGULAR
}

enum class MetronomeTimeSignature(
    val beatsPerBar: Int,
    val beatUnit: Int,
    val label: String
) {
    FOUR_FOUR(beatsPerBar = 4, beatUnit = 4, label = "4/4 拍"),
    THREE_FOUR(beatsPerBar = 3, beatUnit = 4, label = "3/4 拍"),
    TWO_FOUR(beatsPerBar = 2, beatUnit = 4, label = "2/4 拍"),
    SIX_EIGHT(beatsPerBar = 6, beatUnit = 8, label = "6/8 拍"),
    FIVE_FOUR(beatsPerBar = 5, beatUnit = 4, label = "5/4 拍"),
    SEVEN_EIGHT(beatsPerBar = 7, beatUnit = 8, label = "7/8 拍"),
    TWELVE_EIGHT(beatsPerBar = 12, beatUnit = 8, label = "12/8 拍");

    companion object {
        val DEFAULT = FOUR_FOUR
        val PRESETS = listOf(
            FOUR_FOUR,
            THREE_FOUR,
            TWO_FOUR,
            SIX_EIGHT,
            FIVE_FOUR,
            SEVEN_EIGHT,
            TWELVE_EIGHT
        )

        fun fromStorage(raw: String?): MetronomeTimeSignature {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: DEFAULT
        }
    }
}

data class MetronomeSettings(
    val lastBpm: Int = DEFAULT_BPM,
    val soundType: MetronomeSoundType = MetronomeSoundType.WOOD_BLOCK,
    val timeSignature: MetronomeTimeSignature = MetronomeTimeSignature.DEFAULT,
    val accentEnabled: Boolean = true,
    val vibrationEnabled: Boolean = false,
    val accentVibrationEnabled: Boolean = true,
    val regularVibrationEnabled: Boolean = false
) {
    companion object {
        const val DEFAULT_BPM = 120
        const val MIN_BPM = 30
        const val MAX_BPM = 300

        fun sanitizeBpm(raw: Int?): Int {
            if (raw == null) {
                return DEFAULT_BPM
            }

            return if (raw in MIN_BPM..MAX_BPM) {
                raw
            } else {
                DEFAULT_BPM
            }
        }
    }
}

data class MetronomePlaybackConfig(
    val bpm: Int = MetronomeSettings.DEFAULT_BPM,
    val soundType: MetronomeSoundType = MetronomeSoundType.WOOD_BLOCK,
    val timeSignature: MetronomeTimeSignature = MetronomeTimeSignature.DEFAULT,
    val accentEnabled: Boolean = true,
    val vibrationEnabled: Boolean = false,
    val accentVibrationEnabled: Boolean = true,
    val regularVibrationEnabled: Boolean = false
) {
    fun beatTypeFor(beatIndex: Int): MetronomeBeatType {
        val beatsPerBar = timeSignature.beatsPerBar.coerceAtLeast(1)
        val normalizedBeatIndex = ((beatIndex % beatsPerBar) + beatsPerBar) % beatsPerBar
        return if (normalizedBeatIndex == 0 && accentEnabled) {
            MetronomeBeatType.ACCENT
        } else {
            MetronomeBeatType.REGULAR
        }
    }

    fun shouldVibrate(beatIndex: Int): Boolean {
        if (!vibrationEnabled) {
            return false
        }

        return when (beatTypeFor(beatIndex)) {
            MetronomeBeatType.ACCENT -> accentVibrationEnabled
            MetronomeBeatType.REGULAR -> regularVibrationEnabled
        }
    }

    companion object {
        fun fromSettings(settings: MetronomeSettings): MetronomePlaybackConfig {
            return MetronomePlaybackConfig(
                bpm = settings.lastBpm,
                soundType = settings.soundType,
                timeSignature = settings.timeSignature,
                accentEnabled = settings.accentEnabled,
                vibrationEnabled = settings.vibrationEnabled,
                accentVibrationEnabled = settings.accentVibrationEnabled,
                regularVibrationEnabled = settings.regularVibrationEnabled
            )
        }
    }
}
