package com.dp.guitartuning.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunerMathTest {

    /**
     * 验证音名计算会使用当前选择的参考 A4。
     */
    @Test
    fun noteName_usesSelectedReferenceA4() {
        assertEquals("A4", TunerMath.noteName(445f, 445))
        assertEquals("A4", TunerMath.noteName(440f, 440))
    }

    /**
     * 验证标准弦频率会按参考 A4 比例缩放。
     */
    @Test
    fun standardStringAt_scalesWithReferenceA4Ratio() {
        val scaledA = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 445)
        assertEquals(111.25f, scaledA?.frequency ?: 0f, 0.01f)
    }

    /**
     * 验证音分偏差会正确反映参考频率变化后的目标频率。
     */
    @Test
    fun centsOff_reflectsNewReferenceTargetFrequency() {
        val target440 = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 440)
        val target445 = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 445)

        assertEquals(110f, target440?.frequency ?: 0f, 0.01f)
        assertEquals(111.25f, target445?.frequency ?: 0f, 0.01f)

        val cents = TunerMath.centsOff(110f, target445?.frequency ?: 0f)
        assertTrue(cents < 0f)
    }

    /**
     * 验证在没有历史值时，平滑函数会直接返回当前值。
     */
    @Test
    fun smooth_returnsNextWhenPreviousIsNull() {
        assertEquals(12.34f, TunerMath.smooth(previous = null, next = 12.34f, factor = 0.6f), 0f)
    }

    /**
     * 验证非法输入时音名会回退为占位符。
     */
    @Test
    fun noteName_returnsFallbackForInvalidInputs() {
        assertEquals("--", TunerMath.noteName(0f, 440))
        assertEquals("--", TunerMath.noteName(Float.NaN, 440))
        assertEquals("--", TunerMath.noteName(440f, 0))
        assertEquals("--", TunerMath.noteName(440f, -1))
    }

    /**
     * 验证非法输入时音分偏差会回退为 0。
     */
    @Test
    fun centsOff_returnsZeroForInvalidInputs() {
        assertEquals(0f, TunerMath.centsOff(0f, 440f), 0f)
        assertEquals(0f, TunerMath.centsOff(Float.NaN, 440f), 0f)
        assertEquals(0f, TunerMath.centsOff(440f, Float.POSITIVE_INFINITY), 0f)
        assertEquals(0f, TunerMath.centsOff(440f, 0f), 0f)
    }

    /**
     * 验证无效参考值会回退到默认频率，并保留当前中文弦名。
     */
    @Test
    fun guitarTone_usesSafeReferenceFallbackAndCurrentLabels() {
        val fallbackA = GuitarTone.standardStringAt(index = 4, referenceA4Hz = 0)
        assertEquals(110f, fallbackA?.frequency ?: 0f, 0.01f)

        val highE = GuitarTone.standardStringAt(index = 0, referenceA4Hz = 0)
        val lowE = GuitarTone.standardStringAt(index = 5, referenceA4Hz = 0)
        assertEquals("高音 E", highE?.label)
        assertEquals("低音 E", lowE?.label)
    }

    /**
     * 验证非法频率不会匹配到最近琴弦。
     */
    @Test
    fun findClosestStringIndex_returnsNullForInvalidFrequencyInput() {
        assertNull(GuitarTone.findClosestStringIndex(0f, referenceA4Hz = 445))
        assertNull(GuitarTone.findClosestStringIndex(Float.NaN, referenceA4Hz = 445))
    }
}
