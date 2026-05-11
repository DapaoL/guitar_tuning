package com.dp.truning.util

import kotlin.math.ln
import kotlin.math.roundToInt

object TunerMath {
    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    /**
     * 根据频率和参考 A4 计算音名。
     */
    fun noteName(freq: Float, referenceA4Hz: Int): String {
        if (!freq.isFinite() || freq <= 0f || referenceA4Hz <= 0) {
            return "--"
        }

        val semitoneOffset = (12f * (ln(freq / referenceA4Hz) / ln(2.0f))).roundToInt()
        val midi = 69 + semitoneOffset
        val noteIndex = ((midi % 12) + 12) % 12
        val octave = (midi / 12) - 1
        return "${noteNames[noteIndex]}$octave"
    }

    /**
     * 计算当前频率相对目标频率的音分偏差。
     */
    fun centsOff(currentFreq: Float, targetFreq: Float): Float {
        if (!currentFreq.isFinite() || !targetFreq.isFinite() || currentFreq <= 0f || targetFreq <= 0f) {
            return 0f
        }
        val cents = 1200f * (ln(currentFreq / targetFreq) / ln(2.0f))
        return if (cents.isFinite()) cents else 0f
    }

    /**
     * 对连续结果进行平滑处理。
     */
    fun smooth(previous: Float?, next: Float, factor: Float): Float {
        if (previous == null) {
            return next
        }
        return previous + (next - previous) * factor
    }
}
