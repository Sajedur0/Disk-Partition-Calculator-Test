package com.diskcalc.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalcPresetsTest {

    @Test
    fun presetsMatchTheSpec() {
        assertEquals(listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1024.0), CalcPresets.gb)
    }

    @Test
    fun oneTerabytePresetIsLabelledAsTb() {
        assertEquals("1 TB", CalcPresets.label(1024.0))
    }

    @Test
    fun wholeGigabytesHaveNoDecimals() {
        assertEquals("250 GB", CalcPresets.label(250.0))
        assertEquals("250", CalcPresets.inputText(250.0))
    }

    @Test
    fun fractionalSizesKeepTheirDecimals() {
        assertEquals("2.5 GB", CalcPresets.label(2.5))
        assertEquals("2.5", CalcPresets.inputText(2.5))
    }

    @Test
    fun everyPresetProducesAValidInput() {
        CalcPresets.gb.forEach { preset ->
            val text = CalcPresets.inputText(preset)
            val parsed = InputValidator.parseVisibleGb(text)
            assertEquals(preset, parsed.value)
        }
    }
}
