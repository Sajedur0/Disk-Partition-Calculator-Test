package com.diskcalc.ui

import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.SplitPartitionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTextTest {

    @Test
    fun trimDropsTrailingZeros() {
        assertEquals("10", ResultText.trim(10.0))
        assertEquals("2.5", ResultText.trim(2.5))
        assertEquals("100", ResultText.trim(100.0))
    }

    @Test
    fun calcTextContainsTheValueToType() {
        val text = ResultText.calc(PartitionCalculator.calculate(10.0, FileSystemType.NTFS))
        assertTrue(text.contains("Type in Windows Setup: 10244 MB"))
        assertTrue(text.contains("Filesystem: NTFS"))
        assertTrue(text.contains("Windows Setup overhead: No"))
    }

    @Test
    fun calcTextMentionsSetupOverheadWhenEnabled() {
        val text = ResultText.calc(
            PartitionCalculator.calculate(10.0, FileSystemType.NTFS, simulateWindowsSetup = true)
        )
        assertTrue(text.contains("Type in Windows Setup: 10810 MB"))
        assertTrue(text.contains("Windows Setup overhead: Yes (+566 MB)"))
    }

    @Test
    fun splitTextListsEveryPartition() {
        val text = ResultText.split(SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS))
        assertTrue(text.contains("Partition 1: 119230 MB (visible ≈ 119226 MB)"))
        assertTrue(text.contains("Partition 4: 119233 MB (visible ≈ 119229 MB)"))
        assertTrue(text.contains("Sum of all partitions: 476923 MB"))
    }

    @Test
    fun warningsAreAppendedAsBullets() {
        val text = ResultText.calc(PartitionCalculator.calculate(64.0, FileSystemType.FAT32))
        assertTrue(text.contains("• Warning: Partition size exceeds the 32 GB limit"))
        assertTrue(text.contains("• Note: FAT32 has a 4 GB maximum file size limit."))
    }
}
