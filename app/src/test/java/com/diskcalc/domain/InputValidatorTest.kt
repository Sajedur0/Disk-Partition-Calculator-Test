package com.diskcalc.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The exact wording from APP_LOGIC.md §8 must survive refactors. */
class InputValidatorTest {

    @Test
    fun emptyGbUsesTheDedicatedMessage() {
        assertEquals("Enter a visible partition size in GB", InputValidator.parseVisibleGb("").error)
        assertEquals("Enter a visible partition size in GB", InputValidator.parseVisibleGb("   ").error)
    }

    @Test
    fun invalidGbMessages() {
        listOf("abc", "0", "-4", "NaN").forEach { raw ->
            assertEquals("Enter a valid positive GB value", InputValidator.parseVisibleGb(raw).error)
        }
    }

    @Test
    fun decimalCommaIsAccepted() {
        assertEquals(2.5, InputValidator.parseVisibleGb("2,5").value)
        assertEquals(2.5, InputValidator.parseVisibleGb("2.5").value)
        assertNull(InputValidator.parseVisibleGb("12").error)
    }

    @Test
    fun emptyMbMessage() {
        assertEquals("Enter a total disk size in MB", InputValidator.parseTotalMb("").error)
        assertEquals("Enter a total disk size in MB", InputValidator.parseTotalMb(" ".repeat(3)).error)
    }

    @Test
    fun fractionalMbIsRejected() {
        assertEquals("Enter a valid positive MB value", InputValidator.parseTotalMb("12.5").error)
    }

    @Test
    fun thousandsSeparatorsAreAccepted() {
        assertEquals(476923L, InputValidator.parseTotalMb("476,923").value)
        assertEquals(476923L, InputValidator.parseTotalMb("476 923").value)
        assertEquals(476923L, InputValidator.parseTotalMb("476923").value)
    }

    @Test
    fun partitionCountRangeMessage() {
        assertEquals(
            "Partition count must be between 1 and 8",
            InputValidator.parsePartitionCount(0).error
        )
        assertEquals(
            "Partition count must be between 1 and 8",
            InputValidator.parsePartitionCount(9).error
        )
        assertTrue((1..8).all { InputValidator.parsePartitionCount(it).isValid })
    }

    @Test
    fun overheadValidation() {
        assertEquals("Enter the Windows Setup overhead in MB", InputValidator.parseOverheadMb("").error)
        assertEquals("Overhead cannot be negative", InputValidator.parseOverheadMb("-1").error)
        assertEquals(566, InputValidator.parseOverheadMb("566").value)
        assertEquals(0, InputValidator.parseOverheadMb("0").value)
    }

    @Test
    fun readyGates() {
        assertTrue(InputValidator.isCalcReady("100"))
        assertTrue(!InputValidator.isCalcReady("0"))
        assertTrue(InputValidator.isSplitReady("476923", 4))
        assertTrue(!InputValidator.isSplitReady("476923", 12))
    }
}
