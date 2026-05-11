package com.dp.truning.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TunerSettingsTest {

    @Test
    fun defaultsAndBounds_matchTaskContract() {
        assertEquals(440, TunerSettings.DEFAULT_REFERENCE_A4_HZ)
        assertEquals(430, TunerSettings.MIN_REFERENCE_A4_HZ)
        assertEquals(450, TunerSettings.MAX_REFERENCE_A4_HZ)
    }

    @Test
    fun sanitizeReferenceA4_clampsAndPreservesValidValue() {
        assertEquals(440, TunerSettings.sanitizeReferenceA4(null))
        assertEquals(440, TunerSettings.sanitizeReferenceA4(429))
        assertEquals(440, TunerSettings.sanitizeReferenceA4(451))
        assertEquals(442, TunerSettings.sanitizeReferenceA4(442))
    }

    @Test
    fun tuningSensitivity_fromStorageFallsBackToMedium() {
        assertEquals(TuningSensitivity.MEDIUM, TuningSensitivity.fromStorage("unexpected"))
    }

    @Test
    fun tunerDisplayMode_fromStorageFallsBackToPointer() {
        assertEquals(TunerDisplayMode.POINTER, TunerDisplayMode.fromStorage("unexpected"))
    }
}
