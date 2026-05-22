package com.dp.guitartuning.domain.model

import androidx.annotation.DrawableRes
import com.dp.guitartuning.R

/**
 * 支持的乐器类型。
 */
enum class InstrumentType {
    GUITAR,
    BASS,
    UKULELE;

    companion object {
        fun fromStorage(raw: String?): InstrumentType {
            val normalized = raw?.trim()?.uppercase()
            return entries.firstOrNull { it.name == normalized } ?: GUITAR
        }
    }
}

/**
 * 单根目标弦的定义。
 *
 * @param index        弦在当前乐器中的顺序索引（从 0 开始）
 * @param number       弦编号显示文案，例如 "1"、"2"
 * @param label        音名显示文案，例如 "高音 E"、"A"
 * @param baseFrequency 基于标准 A4=440Hz 计算出的目标频率
 */
data class InstrumentString(
    val index: Int,
    val number: String,
    val label: String,
    val baseFrequency: Float
) {
    /**
     * 根据当前 A4 参考频率换算目标频率。
     */
    fun frequency(referenceA4Hz: Int): Float {
        val safeRef = TunerSettings.sanitizeReferenceA4(referenceA4Hz)
        return baseFrequency * (safeRef / 440f)
    }
}

/**
 * 调弦预设：包含一个乐器的一套完整调弦方案。
 *
 * @param id           预设唯一标识，例如 "standard"、"drop_d"
 * @param displayName  预设展示名
 * @param strings      该预设下的弦列表（按 index 升序排列，index 0 为最高音弦）
 * @param defaultStringIndex 切换到该预设后的默认选中弦索引
 */
data class TuningPreset(
    val id: String,
    val displayName: String,
    val strings: List<InstrumentString>,
    val defaultStringIndex: Int = 0
)

/**
 * 乐器配置：描述一种乐器的所有元信息和调弦预设。
 *
 * @param instrumentType    乐器类型
 * @param displayNameRes    乐器名称字符串资源 ID
 * @param imageRes          乐器主视觉图片资源 ID
 * @param tunings           该乐器所有可用的调弦预设（至少一个标准调弦）
 * @param standardTuningId  标准调弦预设的 ID，用于默认选取
 */
data class InstrumentConfig(
    val instrumentType: InstrumentType,
    val displayNameRes: Int,
    @DrawableRes val imageRes: Int,
    val tunings: List<TuningPreset>,
    val standardTuningId: String = "standard"
) {
    /**
     * 返回标准调弦预设；若找不到则返回第一个预设。
     */
    val standardTuning: TuningPreset
        get() = tunings.firstOrNull { it.id == standardTuningId }
            ?: tunings.first()

    companion object {

        /**
         * 吉他标准调弦：E A D G B E（高音弦 index=0 到低音弦 index=5）
         */
        val GUITAR = InstrumentConfig(
            instrumentType = InstrumentType.GUITAR,
            displayNameRes = R.string.home_guitar,
            imageRes = R.drawable.guitar_ic,
            tunings = listOf(
                TuningPreset(
                    id = "standard",
                    displayName = "标准调弦",
                    strings = listOf(
                        InstrumentString(index = 0, number = "1", label = "E", baseFrequency = 329.63f),
                        InstrumentString(index = 1, number = "2", label = "B", baseFrequency = 246.94f),
                        InstrumentString(index = 2, number = "3", label = "G", baseFrequency = 196.00f),
                        InstrumentString(index = 3, number = "4", label = "D", baseFrequency = 146.83f),
                        InstrumentString(index = 4, number = "5", label = "A", baseFrequency = 110.00f),
                        InstrumentString(index = 5, number = "6", label = "E", baseFrequency = 82.41f)
                    ),
                    defaultStringIndex = 4
                )
            )
        )

        /**
         * 贝斯标准调弦：E A D G（低音弦 index=0 到高音弦 index=3）
         */
        val BASS = InstrumentConfig(
            instrumentType = InstrumentType.BASS,
            displayNameRes = R.string.home_bass,
            imageRes = R.drawable.bass_ic,
            tunings = listOf(
                TuningPreset(
                    id = "standard",
                    displayName = "标准调弦",
                    strings = listOf(
                        InstrumentString(index = 0, number = "1", label = "G", baseFrequency = 98.00f),
                        InstrumentString(index = 1, number = "2", label = "D", baseFrequency = 73.42f),
                        InstrumentString(index = 2, number = "3", label = "A", baseFrequency = 55.00f),
                        InstrumentString(index = 3, number = "4", label = "E", baseFrequency = 41.20f)
                    ),
                    defaultStringIndex = 2
                )
            )
        )

        /**
         * 尤克里里标准调弦：G C E A（index=0 到 index=3）
         */
        val UKULELE = InstrumentConfig(
            instrumentType = InstrumentType.UKULELE,
            displayNameRes = R.string.home_ukulele,
            imageRes = R.drawable.ukulele_ic,
            tunings = listOf(
                TuningPreset(
                    id = "standard",
                    displayName = "标准调弦",
                    strings = listOf(
                        InstrumentString(index = 0, number = "1", label = "A", baseFrequency = 440.00f),
                        InstrumentString(index = 1, number = "2", label = "E", baseFrequency = 329.63f),
                        InstrumentString(index = 2, number = "3", label = "C", baseFrequency = 261.63f),
                        InstrumentString(index = 3, number = "4", label = "G", baseFrequency = 392.00f)
                    ),
                    defaultStringIndex = 0
                )
            )
        )

        /**
         * 所有支持的乐器配置列表。
         */
        val ALL = listOf(GUITAR, BASS, UKULELE)

        /**
         * 根据乐器类型获取配置；找不到时回退到吉他。
         */
        fun forType(type: InstrumentType): InstrumentConfig =
            ALL.firstOrNull { it.instrumentType == type } ?: GUITAR
    }
}
