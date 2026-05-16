package com.dp.guitartuning.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstrumentConfigTest {

    // ---- 1. 三种乐器的标准调弦配置是否正确 ----

    @Test
    fun guitar_standardTuning_hasCorrectStrings() {
        val preset = InstrumentConfig.GUITAR.standardTuning
        val labels = preset.strings.map { it.label }
        assertEquals(6, preset.strings.size)
        assertEquals("高音 E", labels[0])
        assertEquals("B", labels[1])
        assertEquals("G", labels[2])
        assertEquals("D", labels[3])
        assertEquals("A", labels[4])
        assertEquals("低音 E", labels[5])
    }

    @Test
    fun bass_standardTuning_hasCorrectStrings() {
        val preset = InstrumentConfig.BASS.standardTuning
        val labels = preset.strings.map { it.label }
        assertEquals(4, preset.strings.size)
        assertEquals("G", labels[0])
        assertEquals("D", labels[1])
        assertEquals("A", labels[2])
        assertEquals("低音 E", labels[3])
    }

    @Test
    fun ukulele_standardTuning_hasCorrectStrings() {
        val preset = InstrumentConfig.UKULELE.standardTuning
        val labels = preset.strings.map { it.label }
        assertEquals(4, preset.strings.size)
        assertEquals("A", labels[0])
        assertEquals("E", labels[1])
        assertEquals("C", labels[2])
        assertEquals("G", labels[3])
    }

    // ---- 2. 切换乐器后默认弦索引是否正确 ----

    @Test
    fun guitar_defaultStringIndex_isA() {
        val preset = InstrumentConfig.GUITAR.standardTuning
        val defaultString = preset.strings[preset.defaultStringIndex]
        assertEquals("A", defaultString.label)
    }

    @Test
    fun bass_defaultStringIndex_isA() {
        val preset = InstrumentConfig.BASS.standardTuning
        val defaultString = preset.strings[preset.defaultStringIndex]
        assertEquals("A", defaultString.label)
    }

    @Test
    fun ukulele_defaultStringIndex_isA() {
        val preset = InstrumentConfig.UKULELE.standardTuning
        val defaultString = preset.strings[preset.defaultStringIndex]
        assertEquals("A", defaultString.label)
    }

    // ---- 3. 当前乐器对应的目标弦集合是否正确返回 ----

    @Test
    fun forType_guitar_returnsGuitarConfig() {
        val config = InstrumentConfig.forType(InstrumentType.GUITAR)
        assertEquals(InstrumentType.GUITAR, config.instrumentType)
    }

    @Test
    fun forType_bass_returnsBassConfig() {
        val config = InstrumentConfig.forType(InstrumentType.BASS)
        assertEquals(InstrumentType.BASS, config.instrumentType)
    }

    @Test
    fun forType_ukulele_returnsUkuleleConfig() {
        val config = InstrumentConfig.forType(InstrumentType.UKULELE)
        assertEquals(InstrumentType.UKULELE, config.instrumentType)
    }

    // ---- 4. 4 弦乐器下不会返回多余的弦配置 ----

    @Test
    fun bass_stringCount_isExactlyFour() {
        assertTrue(InstrumentConfig.BASS.standardTuning.strings.size == 4)
    }

    @Test
    fun ukulele_stringCount_isExactlyFour() {
        assertTrue(InstrumentConfig.UKULELE.standardTuning.strings.size == 4)
    }

    // ---- 5. A4 参考频率变化时，目标弦频率是否仍然正确生成 ----

    @Test
    fun guitarString_frequency_scalesWithReferenceA4() {
        val guitarStrings = InstrumentConfig.GUITAR.standardTuning.strings
        val highE = guitarStrings.first { it.label == "高音 E" }
        val freq440 = highE.frequency(440)
        val freq442 = highE.frequency(442)
        // 442/440 = 1.00454... 频率应该等比例增加
        val expectedRatio = 442f / 440f
        assertEquals(freq440 * expectedRatio, freq442, 0.01f)
    }

    @Test
    fun bassString_frequency_scalesWithReferenceA4() {
        val bassStrings = InstrumentConfig.BASS.standardTuning.strings
        val lowE = bassStrings.first { it.label == "低音 E" }
        val freq440 = lowE.frequency(440)
        val freq430 = lowE.frequency(430)
        val expectedRatio = 430f / 440f
        assertEquals(freq440 * expectedRatio, freq430, 0.01f)
    }

    @Test
    fun ukuleleString_frequency_scalesWithReferenceA4() {
        val ukuleleStrings = InstrumentConfig.UKULELE.standardTuning.strings
        val aString = ukuleleStrings.first { it.label == "A" }
        // A 弦基频为 440Hz，A4=440 时 frequency 应等于 440
        assertEquals(440f, aString.frequency(440), 0.01f)
    }

    // ---- 6. 结构扩展性：每种乐器的 tunings 是集合，可追加更多预设 ----

    @Test
    fun allInstruments_tuningsIsCollection() {
        InstrumentConfig.ALL.forEach { config ->
            assertNotNull(config.tunings)
            assertTrue(config.tunings.isNotEmpty())
        }
    }

    @Test
    fun allInstruments_haveStandardTuning() {
        InstrumentConfig.ALL.forEach { config ->
            val standard = config.tunings.firstOrNull { it.id == "standard" }
            assertNotNull("${config.instrumentType} 应有 standard 预设", standard)
        }
    }

    // ---- 7. 弦索引连续且从 0 开始 ----

    @Test
    fun guitar_stringIndices_areSequential() {
        val strings = InstrumentConfig.GUITAR.standardTuning.strings
        strings.forEachIndexed { i, s -> assertEquals(i, s.index) }
    }

    @Test
    fun bass_stringIndices_areSequential() {
        val strings = InstrumentConfig.BASS.standardTuning.strings
        strings.forEachIndexed { i, s -> assertEquals(i, s.index) }
    }

    @Test
    fun ukulele_stringIndices_areSequential() {
        val strings = InstrumentConfig.UKULELE.standardTuning.strings
        strings.forEachIndexed { i, s -> assertEquals(i, s.index) }
    }
}
