package com.diskcalc.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors APP_LOGIC.md §9 and the fixed behaviour described in §5. */
class SplitPartitionCalculatorTest {

    @Test
    fun remainderGoesToLast() {
        val result = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        assertEquals(listOf(119230, 119230, 119230, 119233), result.partitions.map { it.typeMb })
        assertEquals(476923L, result.partitions.sumOf { it.typeMb.toLong() })
    }

    @Test
    fun expectedVisibleIsTypeMinusFour() {
        val result = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        result.partitions.forEach { partition ->
            assertEquals(partition.typeMb - PartitionConstants.OVERHEAD_MB_PER_PARTITION, partition.expectedVisibleMb)
        }
        assertEquals(119229, result.partitions.last().expectedVisibleMb)
    }

    @Test
    fun evenSplit() {
        val result = SplitPartitionCalculator.split(100000, 5, FileSystemType.NTFS)
        assertTrue(result.partitions.all { it.typeMb == 20000 })
    }

    @Test
    fun sumNeverExceedsDisk() {
        for (count in 1..8) {
            val result = SplitPartitionCalculator.split(476923, count, FileSystemType.NTFS)
            assertEquals(476923L, result.partitions.sumOf { it.typeMb.toLong() })
            assertEquals(476923L, result.sumOfTypeMb)
        }
    }

    @Test
    fun valuesAreWholeNumbers() {
        // 476923 / 4 = 119230.75 must never reach the UI as a fraction: floor for all,
        // the leftover 3 MB ride along on the last partition.
        val result = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        val base = 476923L / 4
        assertEquals(4, result.partitions.size)
        assertTrue(result.partitions.dropLast(1).all { it.typeMb.toLong() == base })
        assertEquals(base + 3L, result.partitions.last().typeMb.toLong())
    }

    @Test
    fun withSetupSubtractsOverhead() {
        val result = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS, simulateWindowsSetup = true)
        assertEquals(476357L, result.usableMb)
        assertEquals(listOf(119089, 119089, 119089, 119090), result.partitions.map { it.typeMb })
        assertTrue(result.warnings.any { it.contains("Setup overhead (566 MB)") })
    }

    @Test
    fun overheadIsIgnoredWhenSimulationIsOff() {
        val result = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS, simulateWindowsSetup = false, setupOverheadMb = 566)
        assertEquals(476923L, result.usableMb)
    }

    @Test
    fun indexesAreOneBasedAndOrdered() {
        val result = SplitPartitionCalculator.split(9000000, 7, FileSystemType.NTFS)
        assertEquals((1..7).toList(), result.partitions.map { it.index })
        assertTrue(result.partitions.dropLast(1).all { it.typeMb == 1285714 })
        assertEquals(1285716, result.partitions.last().typeMb)
        assertEquals(9000000L, result.sumOfTypeMb)
    }

    @Test
    fun mbrWarningAppearsFromFourPartitions() {
        val three = SplitPartitionCalculator.split(100000, 3, FileSystemType.NTFS)
        assertTrue(three.warnings.none { it.contains("MBR") })

        val four = SplitPartitionCalculator.split(100000, 4, FileSystemType.NTFS)
        assertTrue(four.warnings.any { it.contains("MBR") })
    }

    @Test
    fun fat32WarnsWhenAnySliceIsTooBig() {
        val result = SplitPartitionCalculator.split(200000, 2, FileSystemType.FAT32)
        assertTrue(result.partitions.all { it.typeMb > PartitionConstants.FAT32_MAX_PARTITION_MB })
        assertTrue(result.warnings.any { it.contains("exceed the 32 GB") })
        assertTrue(result.warnings.any { it.contains("4 GB maximum file size") })
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooSmall() {
        SplitPartitionCalculator.split(10, 4, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooSmallWithSetupOverhead() {
        SplitPartitionCalculator.split(570, 2, FileSystemType.NTFS, simulateWindowsSetup = true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun countBelowRange() {
        SplitPartitionCalculator.split(100000, 0, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun countAboveRange() {
        SplitPartitionCalculator.split(100000, 9, FileSystemType.NTFS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroTotal() {
        SplitPartitionCalculator.split(0, 2, FileSystemType.NTFS)
    }
}
