package com.dp.guitartuning.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TunerSettingsTest {

    /**
     * 验证 defaults and bounds match task contract。
     */
    @Test
    fun defaultsAndBounds_matchTaskContract() {
        assertEquals(440, TunerSettings.DEFAULT_REFERENCE_A4_HZ)
        assertEquals(430, TunerSettings.MIN_REFERENCE_A4_HZ)
        assertEquals(450, TunerSettings.MAX_REFERENCE_A4_HZ)
    }

    /**
     * 验证 sanitize reference A 4 clamps and preserves valid value。
     */
    @Test
    fun sanitizeReferenceA4_clampsAndPreservesValidValue() {
        assertEquals(440, TunerSettings.sanitizeReferenceA4(null))
        assertEquals(440, TunerSettings.sanitizeReferenceA4(429))
        assertEquals(440, TunerSettings.sanitizeReferenceA4(451))
        assertEquals(442, TunerSettings.sanitizeReferenceA4(442))
    }

    /**
     * 验证 tuning sensitivity from storage falls back to medium。
     */
    @Test
    fun tuningSensitivity_fromStorageFallsBackToMedium() {
        assertEquals(TuningSensitivity.MEDIUM, TuningSensitivity.fromStorage("unexpected"))
    }

    /**
     * 验证 tuner display mode from storage falls back to pointer。
     */
    @Test
    fun tunerDisplayMode_fromStorageFallsBackToPointer() {
        assertEquals(TunerDisplayMode.POINTER, TunerDisplayMode.fromStorage("unexpected"))
    }
}
