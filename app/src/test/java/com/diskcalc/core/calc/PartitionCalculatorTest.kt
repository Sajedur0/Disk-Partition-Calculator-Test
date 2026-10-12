package com.diskcalc.core.calc

import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.PartitionCalculationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ported from the original Dart test suite — the numbers are the contract. */
class PartitionCalculatorTest {

    private val gbToMibFactor = 1024.0
    private val overheadMbPerPartition = 4

    @Test
    fun `visibleGbToPartitionMb converts GB to MiB correctly`() {
        assertEquals(
            (1 * gbToMibFactor).toInt() + overheadMbPerPartition,
            PartitionCalculator.visibleGbToPartitionMb(1.0, FileSystemType.NTFS),
        )
        assertEquals(
            (10 * gbToMibFactor).toInt() + overheadMbPerPartition,
            PartitionCalculator.visibleGbToPartitionMb(10.0, FileSystemType.NTFS),
        )
        assertEquals(
            (100 * gbToMibFactor).toInt() + overheadMbPerPartition,
            PartitionCalculator.visibleGbToPartitionMb(100.0, FileSystemType.NTFS),
        )
        assertEquals(
            (2.5 * gbToMibFactor).toInt() + overheadMbPerPartition,
            PartitionCalculator.visibleGbToPartitionMb(2.5, FileSystemType.NTFS),
        )
        // FAT32 uses the exact same conversion.
        assertEquals(
            (1 * gbToMibFactor).toInt() + overheadMbPerPartition,
            PartitionCalculator.visibleGbToPartitionMb(1.0, FileSystemType.FAT32),
        )
    }

    @Test
    fun `visibleGbToPartitionMb matches the documented reference values`() {
        assertEquals(1028, PartitionCalculator.visibleGbToPartitionMb(1.0, FileSystemType.NTFS))
        assertEquals(10244, PartitionCalculator.visibleGbToPartitionMb(10.0, FileSystemType.NTFS))
        assertEquals(102404, PartitionCalculator.visibleGbToPartitionMb(100.0, FileSystemType.NTFS))
        assertEquals(2564, PartitionCalculator.visibleGbToPartitionMb(2.5, FileSystemType.NTFS))
    }

    @Test
    fun `partitionMbToDisplayedGb converts MiB back to GB`() {
        val mb = (10 * gbToMibFactor).toInt() + overheadMbPerPartition
        assertEquals(
            10.0,
            PartitionCalculator.partitionMbToDisplayedGb(mb, FileSystemType.NTFS),
            0.01,
        )
    }

    @Test
    fun `calculate returns the expected values`() {
        val visibleGb = 10.0
        val filesystem = FileSystemType.NTFS
        val result: PartitionCalculationResult =
            PartitionCalculator.calculate(visibleGb, filesystem)
        val expectedMb = (visibleGb * gbToMibFactor).toInt() + overheadMbPerPartition

        assertEquals(expectedMb, result.partitionSizeMb)
        assertEquals(expectedMb, result.requiredInputMb)
        assertEquals(visibleGb, result.displayedGb, 0.01)
        assertFalse(result.usesWindowsSetupOverhead)
        assertEquals(filesystem, result.filesystem)
    }

    @Test
    fun `calculate with Windows setup overhead adds 566 MB to the required input only`() {
        val visibleGb = 10.0
        val filesystem = FileSystemType.NTFS
        val result = PartitionCalculator.calculate(
            visibleGb,
            filesystem,
            simulateWindowsSetup = true,
        )
        val expectedMb = (visibleGb * gbToMibFactor).toInt() + overheadMbPerPartition

        assertEquals(expectedMb, result.partitionSizeMb)
        assertEquals(
            expectedMb + PartitionCalculator.hiddenWindowsSetupOverheadMb,
            result.requiredInputMb,
        )
        assertEquals(10810, result.requiredInputMb)
        assertEquals(visibleGb, result.displayedGb, 0.01)
        assertTrue(result.usesWindowsSetupOverhead)
        assertEquals(filesystem, result.filesystem)
    }

    @Test
    fun `requiredInputMb mirrors calculate`() {
        assertEquals(
            10244,
            PartitionCalculator.requiredInputMb(10.0, FileSystemType.NTFS),
        )
        assertEquals(
            10810,
            PartitionCalculator.requiredInputMb(
                10.0,
                FileSystemType.NTFS,
                simulateWindowsSetup = true,
            ),
        )
    }

    @Test
    fun `isFat32SizeValid accepts sizes up to 32 GB`() {
        assertTrue(PartitionCalculator.isFat32SizeValid(32.0))
        assertTrue(PartitionCalculator.isFat32SizeValid(31.9))
        assertTrue(PartitionCalculator.isFat32SizeValid(0.0))
    }

    @Test
    fun `isFat32SizeValid rejects sizes above 32 GB`() {
        assertFalse(PartitionCalculator.isFat32SizeValid(32.1))
        assertFalse(PartitionCalculator.isFat32SizeValid(100.0))
    }
}
