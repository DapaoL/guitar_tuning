package com.dp.guitartuning.util

import com.dp.guitartuning.domain.model.TunerSettings
import kotlin.math.abs

object GuitarTone {
    const val E6 = 82.41f
    const val A5 = 110.00f
    const val D4 = 146.83f
    const val G3 = 196.00f
    const val B2 = 246.94f
    const val E1 = 329.63f

    data class GuitarString(
        val index: Int,
        val number: String,
        val label: String,
        val frequency: Float
    )

    private val canonicalStandardStrings =
        listOf(
            GuitarString(index = 0, number = "1", label = "高音 E", frequency = E1),
            GuitarString(index = 1, number = "2", label = "B", frequency = B2),
            GuitarString(index = 2, number = "3", label = "G", frequency = G3),
            GuitarString(index = 3, number = "4", label = "D", frequency = D4),
            GuitarString(index = 4, number = "5", label = "A", frequency = A5),
            GuitarString(index = 5, number = "6", label = "低音 E", frequency = E6)
        )

    val standardStrings: List<GuitarString>
        get() = standardStrings(TunerSettings.DEFAULT_REFERENCE_A4_HZ)

    /**
     * 返回按当前 A4 参考频率换算后的标准弦列表。
     */
    fun standardStrings(referenceA4Hz: Int = TunerSettings.DEFAULT_REFERENCE_A4_HZ): List<GuitarString> {
        val safeReferenceA4 = TunerSettings.sanitizeReferenceA4(referenceA4Hz)
        val ratio = safeReferenceA4 / 440f
        return canonicalStandardStrings.map { string ->
            string.copy(frequency = string.frequency * ratio)
        }
    }

    /**
     * 返回指定索引对应的标准弦信息。
     */
    fun standardStringAt(
        index: Int,
        referenceA4Hz: Int = TunerSettings.DEFAULT_REFERENCE_A4_HZ
    ): GuitarString? = standardStrings(referenceA4Hz).getOrNull(index)

    /**
     * 查找与当前频率最接近的琴弦索引。
     */
    fun findClosestStringIndex(
        freq: Float,
        referenceA4Hz: Int = TunerSettings.DEFAULT_REFERENCE_A4_HZ,
        maxCentOffset: Float = 250f
    ): Int? {
        if (!freq.isFinite() || freq <= 0f || !maxCentOffset.isFinite() || maxCentOffset < 0f) {
            return null
        }

        val closest = standardStrings(referenceA4Hz).minByOrNull { string ->
            abs(TunerMath.centsOff(freq, string.frequency))
        } ?: return null

        if (!closest.frequency.isFinite() || closest.frequency <= 0f) {
            return null
        }

        val centOffset = abs(TunerMath.centsOff(freq, closest.frequency))
        return closest.index.takeIf { centOffset <= maxCentOffset }
    }
}
