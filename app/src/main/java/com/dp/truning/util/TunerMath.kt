package com.dp.truning.util

import kotlin.math.ln
import kotlin.math.roundToInt

object TunerMath {
    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

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

    fun centsOff(currentFreq: Float, targetFreq: Float): Float {
        if (!currentFreq.isFinite() || !targetFreq.isFinite() || currentFreq <= 0f || targetFreq <= 0f) {
            return 0f
        }
        val cents = 1200f * (ln(currentFreq / targetFreq) / ln(2.0f))
        return if (cents.isFinite()) cents else 0f
    }

    fun smooth(previous: Float?, next: Float, factor: Float): Float {
        if (previous == null) {
            return next
        }
        return previous + (next - previous) * factor
    }
}
