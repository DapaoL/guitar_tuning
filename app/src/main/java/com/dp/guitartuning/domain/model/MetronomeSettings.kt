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

data class MetronomeSettings(
    val lastBpm: Int = DEFAULT_BPM,
    val soundType: MetronomeSoundType = MetronomeSoundType.WOOD_BLOCK,
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
    val accentEnabled: Boolean = true,
    val vibrationEnabled: Boolean = false,
    val accentVibrationEnabled: Boolean = true,
    val regularVibrationEnabled: Boolean = false
) {
    fun beatTypeFor(beatIndex: Int): MetronomeBeatType {
        val normalizedBeatIndex = ((beatIndex % BEATS_PER_BAR) + BEATS_PER_BAR) % BEATS_PER_BAR
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
        private const val BEATS_PER_BAR = 4

        fun fromSettings(settings: MetronomeSettings): MetronomePlaybackConfig {
            return MetronomePlaybackConfig(
                bpm = settings.lastBpm,
                soundType = settings.soundType,
                accentEnabled = settings.accentEnabled,
                vibrationEnabled = settings.vibrationEnabled,
                accentVibrationEnabled = settings.accentVibrationEnabled,
                regularVibrationEnabled = settings.regularVibrationEnabled
            )
        }
    }
}
