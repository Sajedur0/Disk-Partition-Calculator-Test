package com.diskcalc.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors the test list in APP_LOGIC.md §9 plus the examples table of §4. */
class PartitionCalculatorTest {

    @Test
    fun gbToMb() {
        assertEquals(1028, PartitionCalculator.visibleGbToPartitionMb(1.0))
        assertEquals(10244, PartitionCalculator.visibleGbToPartitionMb(10.0))
        assertEquals(2564, PartitionCalculator.visibleGbToPartitionMb(2.5))
        assertEquals(102404, PartitionCalculator.visibleGbToPartitionMb(100.0))
    }

    @Test
    fun plainCalculationMatchesSpecExamples() {
        val oneGb = PartitionCalculator.calculate(1.0, FileSystemType.NTFS)
        assertEquals(1028, oneGb.partitionSizeMb)
        assertEquals(1028, oneGb.requiredInputMb)

        val tenGb = PartitionCalculator.calculate(10.0, FileSystemType.NTFS)
        assertEquals(10244, tenGb.partitionSizeMb)
        assertEquals(10244, tenGb.requiredInputMb)
        assertEquals(10240, tenGb.expectedVisibleMb)

        val hundredGb = PartitionCalculator.calculate(100.0, FileSystemType.NTFS)
        assertEquals(102404, hundredGb.partitionSizeMb)
        assertEquals(102404, hundredGb.requiredInputMb)
    }

    @Test
    fun withSetup() {
        val result = PartitionCalculator.calculate(10.0, FileSystemType.NTFS, simulateWindowsSetup = true)
        assertEquals(10244, result.partitionSizeMb)
        assertEquals(10810, result.requiredInputMb)
        assertTrue(result.usesWindowsSetupOverhead)
        assertEquals(566, result.setupOverheadMb)
    }

    @Test
    fun setupOverheadOnlyAffectsRequiredInput() {
        val without = PartitionCalculator.calculate(100.0, FileSystemType.NTFS)
        val with = PartitionCalculator.calculate(100.0, FileSystemType.NTFS, simulateWindowsSetup = true)
        assertEquals(without.partitionSizeMb, with.partitionSizeMb)
        assertEquals(102404, with.partitionSizeMb)
        assertEquals(102970, with.requiredInputMb)
    }

    @Test
    fun customOverheadIsUsedWhenProvided() {
        val result = PartitionCalculator.calculate(
            visibleGb = 50.0,
            filesystem = FileSystemType.NTFS,
            simulateWindowsSetup = true,
            setupOverheadMb = 826
        )
        assertEquals(51204 + 826, result.requiredInputMb)
        assertEquals(826, result.setupOverheadMb)
    }

    @Test
    fun fat32Limit() {
        assertTrue(PartitionCalculator.isFat32SizeValid(32768))
        assertFalse(PartitionCalculator.isFat32SizeValid(32772)) // 32 GB visible + 4
    }

    @Test
    fun fat32SizeWarningOnlyAboveTheLimit() {
        val justUnder = PartitionCalculator.calculate(31.99, FileSystemType.FAT32)
        assertEquals(32762, justUnder.partitionSizeMb)
        assertFalse(justUnder.warnings.any { it.contains("exceeds the 32 GB") })

        val over = PartitionCalculator.calculate(32.0, FileSystemType.FAT32)
        assertTrue(over.warnings.any { it.contains("exceeds the 32 GB") })
    }

    @Test
    fun fat32AlwaysNotesTheFileSizeLimit() {
        val result = PartitionCalculator.calculate(20.0, FileSystemType.FAT32)
        assertTrue(result.warnings.any { it.contains("${PartitionConstants.FAT32_MAX_FILE_GB} GB maximum file size") })
    }

    @Test
    fun everyResultCarriesTheAccuracyNote() {
        val ntfs = PartitionCalculator.calculate(10.0, FileSystemType.NTFS)
        assertEquals(1, ntfs.warnings.size)
        assertTrue(ntfs.warnings.first().contains("1\u20132 MB"))

        val withSetup = PartitionCalculator.calculate(10.0, FileSystemType.NTFS, simulateWindowsSetup = true)
        assertTrue(withSetup.warnings.any { it.contains("~566 MB") })
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroInvalid() {
        PartitionCalculator.calculate(0.0, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nanInvalid() {
        PartitionCalculator.calculate(Double.NaN, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun infinityInvalid() {
        PartitionCalculator.calculate(Double.POSITIVE_INFINITY, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeInvalid() {
        PartitionCalculator.calculate(-1.0, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeOverheadInvalid() {
        PartitionCalculator.calculate(10.0, FileSystemType.NTFS, simulateWindowsSetup = true, setupOverheadMb = -5)
    }
}
