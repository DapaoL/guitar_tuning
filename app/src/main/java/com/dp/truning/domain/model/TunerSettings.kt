package com.dp.truning.domain.model

enum class TuningSensitivity(
    val minProbability: Float,
    val smoothingFactor: Float
) {
    HIGH(0.60f, 0.90f),
    MEDIUM(0.70f, 0.60f),
    LOW(0.82f, 0.35f);

    companion object {
        /**
         * 将持久化存储的字符串解析为枚举值。
         */
        fun fromStorage(raw: String?): TuningSensitivity {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: MEDIUM
        }
    }
}

enum class TunerDisplayMode {
    GAUGE,
    POINTER,
    NUMERIC;

    companion object {
        /**
         * 将持久化存储的字符串解析为枚举值。
         */
        fun fromStorage(raw: String?): TunerDisplayMode {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: POINTER
        }
    }
}

data class TunerSettings(
    val referenceA4Hz: Int = DEFAULT_REFERENCE_A4_HZ,
    val sensitivity: TuningSensitivity = TuningSensitivity.MEDIUM,
    val displayMode: TunerDisplayMode = TunerDisplayMode.POINTER
) {
    companion object {
        const val DEFAULT_REFERENCE_A4_HZ = 440
        const val MIN_REFERENCE_A4_HZ = 430
        const val MAX_REFERENCE_A4_HZ = 450

        /**
         * 将 A4 参考频率校正到允许范围内。
         */
        fun sanitizeReferenceA4(raw: Int?): Int {
            if (raw == null) {
                return DEFAULT_REFERENCE_A4_HZ
            }

            return if (raw in MIN_REFERENCE_A4_HZ..MAX_REFERENCE_A4_HZ) {
                raw
            } else {
                DEFAULT_REFERENCE_A4_HZ
            }
        }
    }
}
