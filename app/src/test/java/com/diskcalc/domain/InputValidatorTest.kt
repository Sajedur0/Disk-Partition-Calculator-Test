package com.diskcalc.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {
    @Test
    fun `GB accepts decimals and decimal commas without discarding bad characters`() {
        assertEquals(100.5, InputValidator.parseVisibleGb("100.5").value!!, 0.0)
        assertEquals(1.25, InputValidator.parseVisibleGb(" 1,25 ").value!!, 0.0)
        assertEquals(0.5, InputValidator.parseVisibleGb(".5").value!!, 0.0)
        assertEquals(65536.0, InputValidator.parseVisibleGb("65536").value!!, 0.0)
        assertNull(InputValidator.parseVisibleGb("").error)

        listOf("-100", "+100", "100 5", "1.2.3", "1,234", "NaN", "1e3", ".", "0", "١٠٠")
            .forEach { input ->
                assertFalse("Should reject $input", InputValidator.parseVisibleGb(input).isValid)
                assertEquals(InputValidator.INVALID_GB_MESSAGE, InputValidator.parseVisibleGb(input).error)
            }
        assertEquals("Maximum is 65536 GB", InputValidator.parseVisibleGb("65536.01").error)
    }

    @Test
    fun `unallocated MB must be a positive whole number with no separators`() {
        assertEquals(476923L, InputValidator.parseTotalMb(" 476923 ").value)
        assertEquals(PartitionConstants.MAX_TOTAL_MB, InputValidator.parseTotalMb("134217728").value)
        assertNull(InputValidator.parseTotalMb("").error)

        listOf("-100", "+100", "100.5", "1,000", "1.000", "1 000", "0", "١٠٠")
            .forEach { input ->
                assertFalse("Should reject $input", InputValidator.parseTotalMb(input).isValid)
                assertEquals(InputValidator.INVALID_TOTAL_MB_MESSAGE, InputValidator.parseTotalMb(input).error)
            }
        assertEquals("Maximum is 134217728 MB", InputValidator.parseTotalMb("134217729").error)
        assertEquals("Maximum is 134217728 MB", InputValidator.parseTotalMb("9".repeat(50)).error)
    }

    @Test
    fun `measured byte sizes are positive ungrouped integers and preserve invalid text`() {
        assertEquals(53_691_285_504L, InputValidator.parseSampleBytes(" 53691285504 ").value)
        assertEquals(PartitionConstants.MAX_SAMPLE_BYTES, InputValidator.parseSampleBytes(PartitionConstants.MAX_SAMPLE_BYTES.toString()).value)
        listOf("", " ", "0", "-4096", "+4096", "4.096", "4,096", "4 096", "١٢")
            .forEach { input ->
                assertFalse("Should reject $input", InputValidator.parseSampleBytes(input).isValid)
                assertEquals(InputValidator.INVALID_SAMPLE_BYTES_MESSAGE, InputValidator.parseSampleBytes(input).error)
            }
        val maximumMessage = "Maximum is ${PartitionConstants.MAX_SAMPLE_BYTES} bytes"
        assertEquals(maximumMessage, InputValidator.parseSampleBytes((PartitionConstants.MAX_SAMPLE_BYTES + 1).toString()).error)
        assertEquals(maximumMessage, InputValidator.parseSampleBytes("9".repeat(50)).error)
    }

    @Test
    fun `custom reserve must be a whole number in range including zero`() {
        assertEquals(0, InputValidator.parseOverheadMb("0").value)
        assertEquals(100000, InputValidator.parseOverheadMb("100000").value)
        listOf("", " ", "-100", "+100", "100.5", "1,000", "1 000")
            .forEach { input ->
                assertNull("Should reject $input", InputValidator.parseOverheadMb(input).value)
                assertEquals(InputValidator.INVALID_OVERHEAD_MESSAGE, InputValidator.parseOverheadMb(input).error)
            }
        assertEquals("Maximum is 100000 MB", InputValidator.parseOverheadMb("100001").error)
        assertEquals("Maximum is 100000 MB", InputValidator.parseOverheadMb("9".repeat(50)).error)
    }

    @Test
    fun `split validation guards count before dividing and validates reserve`() {
        assertFalse(InputValidator.isSplitReady("100", 0))
        assertFalse(InputValidator.isSplitReady("100", 9))
        assertTrue(InputValidator.isSplitReady("100", 2))
        assertEquals("Partition count must be between 1 and 8", InputValidator.validateSplit(100, 0, 0))
        assertEquals(InputValidator.INVALID_OVERHEAD_MESSAGE, InputValidator.validateSplit(100, 1, -1))
        assertEquals("Unallocated space is smaller than the reserve (100 MB)", InputValidator.validateSplit(100, 1, 100))
        assertEquals("Too small to split into 2 partitions", InputValidator.validateSplit(9, 2, 0))
        assertNull(InputValidator.validateSplit(10, 2, 0))
    }
}
