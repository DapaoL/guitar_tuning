package com.dp.truning.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunerMathTest {

    @Test
    fun noteName_usesSelectedReferenceA4() {
        assertEquals("A4", TunerMath.noteName(445f, 445))
        assertEquals("A4", TunerMath.noteName(440f, 440))
    }

    @Test
    fun standardStringAt_scalesWithReferenceA4Ratio() {
        val scaledA = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 445)
        assertEquals(111.25f, scaledA?.frequency ?: 0f, 0.01f)
    }

    @Test
    fun centsOff_reflectsNewReferenceTargetFrequency() {
        val target440 = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 440)
        val target445 = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 445)

        assertEquals(110f, target440?.frequency ?: 0f, 0.01f)
        assertEquals(111.25f, target445?.frequency ?: 0f, 0.01f)

        val cents = TunerMath.centsOff(110f, target445?.frequency ?: 0f)
        assertTrue(cents < 0f)
    }

    @Test
    fun smooth_returnsNextWhenPreviousIsNull() {
        assertEquals(12.34f, TunerMath.smooth(previous = null, next = 12.34f, factor = 0.6f), 0f)
    }

    @Test
    fun noteName_returnsFallbackForInvalidInputs() {
        assertEquals("--", TunerMath.noteName(0f, 440))
        assertEquals("--", TunerMath.noteName(Float.NaN, 440))
        assertEquals("--", TunerMath.noteName(440f, 0))
        assertEquals("--", TunerMath.noteName(440f, -1))
    }

    @Test
    fun centsOff_returnsZeroForInvalidInputs() {
        assertEquals(0f, TunerMath.centsOff(0f, 440f), 0f)
        assertEquals(0f, TunerMath.centsOff(Float.NaN, 440f), 0f)
        assertEquals(0f, TunerMath.centsOff(440f, Float.POSITIVE_INFINITY), 0f)
        assertEquals(0f, TunerMath.centsOff(440f, 0f), 0f)
    }

    @Test
    fun guitarTone_usesSafeReferenceFallbackAndOriginalLabels() {
        val fallbackA = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 0)
        assertEquals(110f, fallbackA?.frequency ?: 0f, 0.01f)

        val highE = GuitarTone.standardStringAt(index = 0, referenceA4Hz = 0)
        val lowE = GuitarTone.standardStringAt(index = 5, referenceA4Hz = 0)
        assertEquals("高音E", highE?.label)
        assertEquals("低音E", lowE?.label)
    }

    @Test
    fun findClosestStringIndex_returnsNullForInvalidFrequencyInput() {
        assertNull(GuitarTone.findClosestStringIndex(0f, referenceA4Hz = 445))
        assertNull(GuitarTone.findClosestStringIndex(Float.NaN, referenceA4Hz = 445))
    }
}
