package com.diskcalc.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PartitionCalculatorTest {
    @Test
    fun `system reserve is a separate budget and never changes suggested partition`() {
        val expectedPartition = 102404
        listOf(0, 566, 826, 100000).forEach { reserve ->
            val result = PartitionCalculator.calculate(100.0, FileSystemType.NTFS, true, reserve)
            assertEquals(expectedPartition, result.partitionSizeMb)
            assertEquals(expectedPartition, result.requiredInputMb)
            assertEquals(107374182400L, result.targetCapacityBytes)
            assertEquals(102400, result.baselineMb)
            assertEquals(null, result.estimatedCapacityBytes)
            assertEquals(reserve, result.setupOverheadMb)
            assertEquals(expectedPartition.toLong() + reserve, result.estimatedTotalMb)
        }
        val disabled = PartitionCalculator.calculate(100.0, FileSystemType.NTFS, false, 826)
        assertEquals(expectedPartition, disabled.requiredInputMb)
        assertEquals(0, disabled.setupOverheadMb)
        assertEquals(102404L, disabled.estimatedTotalMb)
    }

    @Test
    fun `guide examples use the same values as the calculation`() {
        val examples = listOf(
            listOf(1.0, 1028.0, 1594.0, 1854.0),
            listOf(2.5, 2564.0, 3130.0, 3390.0),
            listOf(50.0, 51204.0, 51770.0, 52030.0),
            listOf(100.0, 102404.0, 102970.0, 103230.0),
            listOf(1024.0, 1048580.0, 1049146.0, 1049406.0)
        )
        examples.forEach { (gb, suggested, win10Budget, win11Budget) ->
            val win10 = PartitionCalculator.calculate(gb, FileSystemType.NTFS, true, 566)
            val win11 = PartitionCalculator.calculate(gb, FileSystemType.NTFS, true, 826)
            assertEquals(suggested.toInt(), win10.requiredInputMb)
            assertEquals(suggested.toInt(), win11.requiredInputMb)
            assertEquals(win10Budget.toLong(), win10.estimatedTotalMb)
            assertEquals(win11Budget.toLong(), win11.estimatedTotalMb)
        }
        assertEquals(67108868, PartitionCalculator.visibleGbToPartitionMb(65536.0))
        assertEquals(15, PartitionCalculator.visibleGbToPartitionMb(0.01))
    }

    @Test
    fun `every uncalibrated Calc partition gets four MB automatically`() {
        val targets = listOf(0.01, 1.0, 2.5, 50.0, 100.0, 1024.0, 65536.0)
        for (filesystem in FileSystemType.entries) {
            for (target in targets) {
                val result = PartitionCalculator.calculate(target, filesystem)
                val withReserve = PartitionCalculator.calculate(target, filesystem, true, 566)
                assertEquals(4, result.requiredInputMb - result.baselineMb)
                assertEquals(result.requiredInputMb, withReserve.requiredInputMb)
                assertEquals(result.requiredInputMb.toLong() + 566, withReserve.estimatedTotalMb)
            }
        }
    }

    @Test
    fun `50 binary GB separates target baseline fallback and sampled model`() {
        val uncalibrated = PartitionCalculator.calculate(50.0, FileSystemType.NTFS)
        assertEquals(53_687_091_200L, uncalibrated.targetCapacityBytes)
        assertEquals(51200, uncalibrated.baselineMb)
        assertEquals(51204, uncalibrated.requiredInputMb)
        assertEquals(4, uncalibrated.adjustmentMb)
        assertNull(uncalibrated.measuredGapBytes)
        assertNull(uncalibrated.estimatedCapacityBytes)

        val sample = CapacitySample(partitionBytes = 53_691_285_504L, volumeBytes = 53_691_281_408L)
        val measured = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, true, 566, sample)
        assertEquals(4096L, sample.gapBytes)
        assertEquals(4096L, measured.measuredGapBytes)
        assertEquals(51205, measured.requiredInputMb)
        assertEquals(5, measured.adjustmentMb)
        assertEquals(53_692_329_984L, measured.estimatedCapacityBytes)
        assertEquals(51771L, measured.estimatedTotalMb)
        assertNull(measured.targetVolumeBytes)
        assertNull(measured.estimatedAdditionalMb)
    }

    @Test
    fun `sample rounds up after adding bytes including zero and exact boundaries`() {
        val mb = PartitionConstants.BYTES_PER_MB
        val volume = 100 * mb
        val cases = listOf(0L to 51204, 1L to 51205, mb - 1 to 51205, mb to 51205, mb + 1 to 51206, 4 * mb to 51208)
        cases.forEach { (gap, expectedMb) ->
            val sample = CapacitySample(volume + gap, volume)
            val result = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = sample)
            assertEquals(gap, result.measuredGapBytes)
            assertEquals(expectedMb, result.requiredInputMb)
            assertEquals(expectedMb * mb - gap, result.estimatedCapacityBytes)
        }

        val fractional = PartitionCalculator.calculate(0.01, FileSystemType.NTFS, sample = CapacitySample(volume, volume))
        assertEquals(10_737_419L, fractional.targetCapacityBytes)
        assertEquals(11, fractional.baselineMb)
        assertEquals(15, fractional.requiredInputMb)
        assertTrue(fractional.estimatedCapacityBytes!! >= fractional.targetCapacityBytes)
    }

    @Test
    fun `decimal targets and sampled ceilings remain correct over many inputs`() {
        val random = Random(20261027)
        val mb = PartitionConstants.BYTES_PER_MB
        repeat(3000) {
            val hundredths = random.nextLong(1, 6_553_601)
            val gb = hundredths / 100.0
            val expectedBytes = (hundredths * PartitionConstants.BYTES_PER_GB + 99) / 100
            val gap = random.nextLong(0, 100_000_000)
            val volume = 200 * mb
            val result = PartitionCalculator.calculate(gb, FileSystemType.NTFS, sample = CapacitySample(volume + gap, volume))
            assertEquals(expectedBytes, result.targetCapacityBytes)
            assertEquals((expectedBytes + mb - 1) / mb, result.baselineMb.toLong())
            val bufferedTarget = expectedBytes + PartitionConstants.DISPLAY_SAFETY_BUFFER_MB * mb
            assertEquals((bufferedTarget + gap + mb - 1) / mb, result.requiredInputMb.toLong())
            assertTrue(result.estimatedCapacityBytes!! >= bufferedTarget)
            assertTrue((result.requiredInputMb - 1L) * mb - gap < bufferedTarget)
        }
    }

    @Test
    fun `existing below-target volume shows extra space separately from new total`() {
        val mib = PartitionConstants.BYTES_PER_MB
        val target = 50 * PartitionConstants.BYTES_PER_GB
        val sample = CapacitySample(target, target - 4096, isTargetVolume = true)
        val result = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = sample)
        assertEquals(target - 4096, result.targetVolumeBytes)
        assertEquals(4096L, result.targetShortfallBytes)
        assertEquals(5L, result.estimatedAdditionalMb)
        assertEquals(51205, result.requiredInputMb)
        assertTrue(sample.volumeBytes + result.estimatedAdditionalMb!! * mib >= target + 4 * mib)

        // The partition can begin/end between whole MiB; extra space is rounded independently.
        val fractional = CapacitySample(target + 8 * mib / 10, target - 4 * mib / 10, true)
        val fractionalResult = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = fractional)
        assertEquals(51206, fractionalResult.requiredInputMb)
        assertEquals(5L, fractionalResult.estimatedAdditionalMb)
    }

    @Test
    fun `target sample at or above capacity never suggests extension`() {
        val target = 50 * PartitionConstants.BYTES_PER_GB
        listOf(target, target + 1, target + PartitionConstants.BYTES_PER_MB).forEach { capacity ->
            val sample = CapacitySample(capacity + 4096, capacity, isTargetVolume = true)
            val result = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = sample)
            assertEquals(capacity, result.targetVolumeBytes)
            assertEquals(0L, result.targetShortfallBytes)
            assertNull(result.estimatedAdditionalMb)
        }
        val otherDrive = CapacitySample(target, target - 4096, isTargetVolume = false)
        val result = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = otherDrive)
        assertNull(result.targetVolumeBytes)
        assertNull(result.targetShortfallBytes)
        assertNull(result.estimatedAdditionalMb)
    }

    @Test
    fun `extra-space estimate rounds shortfall and buffer upward exactly`() {
        val mib = PartitionConstants.BYTES_PER_MB
        val target = 50 * PartitionConstants.BYTES_PER_GB
        val cases = listOf(1L to 5L, 4096L to 5L, mib to 5L, mib + 1 to 6L, 100 * mib to 104L)
        cases.forEach { (shortfall, expected) ->
            val sample = CapacitySample(target, target - shortfall, isTargetVolume = true)
            val result = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = sample)
            assertEquals(shortfall, result.targetShortfallBytes)
            assertEquals(expected, result.estimatedAdditionalMb)
            assertTrue(sample.volumeBytes + expected * mib >= target + 4 * mib)
            assertTrue(sample.volumeBytes + (expected - 1) * mib < target + 4 * mib)
        }
    }

    @Test
    fun `extra-space rounding stays safe across random shortfalls and partition alignments`() {
        val mib = PartitionConstants.BYTES_PER_MB
        val target = 50 * PartitionConstants.BYTES_PER_GB
        val random = Random(20261028)
        repeat(3000) {
            val shortfall = random.nextLong(1, target)
            val sample = CapacitySample(target + random.nextLong(0, 10 * mib), target - shortfall, true)
            val result = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = sample)
            val extra = result.estimatedAdditionalMb!!
            val modelledAfterIncrease = sample.volumeBytes + extra * mib
            assertTrue(modelledAfterIncrease >= target + 4 * mib)
            assertTrue(modelledAfterIncrease < target + 5 * mib)
        }
    }

    @Test
    fun `uncalibrated FAT32 warns the buffer may be insufficient`() {
        val fallback = PartitionCalculator.calculate(50.0, FileSystemType.FAT32)
        val measured = PartitionCalculator.calculate(50.0, FileSystemType.FAT32, sample = CapacitySample(10000, 5904))
        assertTrue(fallback.warnings.any { it.contains("buffer may be too small for FAT32") })
        assertFalse(measured.warnings.any { it.contains("buffer may be too small for FAT32") })
    }

    @Test
    fun `sample requires a positive comparable partition and volume`() {
        assertThrows(IllegalArgumentException::class.java) { CapacitySample(0, 1) }
        assertThrows(IllegalArgumentException::class.java) { CapacitySample(-1, 1) }
        assertThrows(IllegalArgumentException::class.java) { CapacitySample(100, 0) }
        assertThrows(IllegalArgumentException::class.java) { CapacitySample(100, 101) }
        assertThrows(IllegalArgumentException::class.java) { CapacitySample(PartitionConstants.MAX_SAMPLE_BYTES + 1, 1) }
        assertEquals(0L, CapacitySample(100, 100).gapBytes)
        assertEquals(PartitionConstants.MAX_SAMPLE_BYTES - 1, CapacitySample(PartitionConstants.MAX_SAMPLE_BYTES, 1).gapBytes)
    }

    @Test
    fun `maximum target and sample do not overflow`() {
        val sample = CapacitySample(PartitionConstants.MAX_SAMPLE_BYTES, 1)
        val result = PartitionCalculator.calculate(65536.0, FileSystemType.NTFS, true, 100000, sample)
        assertEquals(70_368_744_177_664L, result.targetCapacityBytes)
        assertEquals(67_108_864, result.baselineMb)
        assertEquals(201_326_596, result.requiredInputMb)
        assertEquals(70_368_748_371_969L, result.estimatedCapacityBytes)
        assertEquals(201_426_596L, result.estimatedTotalMb)
        val existing = PartitionCalculator.calculate(
            65536.0, FileSystemType.NTFS, sample = CapacitySample(PartitionConstants.MAX_SAMPLE_BYTES, 1, true)
        )
        assertEquals(67_108_868L, existing.estimatedAdditionalMb)
    }

    @Test
    fun `direct calls reject invalid sizes and reserves`() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 65536.01).forEach { gb ->
            assertThrows(IllegalArgumentException::class.java) {
                PartitionCalculator.calculate(gb, FileSystemType.NTFS)
            }
        }
        listOf(-1, 100001).forEach { reserve ->
            assertThrows(IllegalArgumentException::class.java) {
                PartitionCalculator.calculate(1.0, FileSystemType.NTFS, true, reserve)
            }
        }
    }

    @Test
    fun `split examples subtract only requested future reserve`() {
        val noReserve = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        assertEquals(listOf(119226, 119226, 119226, 119229), noReserve.partitions.map { it.baseMb })
        assertEquals(listOf(119230, 119230, 119230, 119233), noReserve.partitions.map { it.typeMb })
        assertEquals(16L, noReserve.totalBufferMb)
        assertEquals(476907L, noReserve.baseBudgetMb)
        assertEquals(3L, noReserve.remainderMb)
        assertEquals(476923L, noReserve.usableMb)
        assertEquals(noReserve.usableMb, noReserve.sumOfTypeMb)

        val reserve = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS, true, 566)
        assertEquals(listOf(119085, 119085, 119085, 119086), reserve.partitions.map { it.baseMb })
        assertEquals(listOf(119089, 119089, 119089, 119090), reserve.partitions.map { it.typeMb })
        assertEquals(16L, reserve.totalBufferMb)
        assertEquals(476341L, reserve.baseBudgetMb)
        assertEquals(1L, reserve.remainderMb)
        assertEquals(476357L, reserve.usableMb)
        assertEquals(reserve.usableMb, reserve.sumOfTypeMb)
        assertEquals(566L, reserve.totalMb - reserve.usableMb)
    }

    @Test
    fun `two 50 GB nominal shares require room for both automatic buffers`() {
        for (filesystem in FileSystemType.entries) {
            val enough = SplitPartitionCalculator.split(102408, 2, filesystem)
            assertEquals(listOf(51200, 51200), enough.partitions.map { it.baseMb })
            assertEquals(listOf(51204, 51204), enough.partitions.map { it.typeMb })
            assertEquals(8L, enough.totalBufferMb)
            assertEquals(102408L, enough.sumOfTypeMb)

            val fixedRegion = SplitPartitionCalculator.split(102400, 2, filesystem)
            assertEquals(listOf(51196, 51196), fixedRegion.partitions.map { it.baseMb })
            assertEquals(listOf(51200, 51200), fixedRegion.partitions.map { it.typeMb })
            assertEquals(102400L, fixedRegion.sumOfTypeMb)
        }
    }

    @Test
    fun `split sums and remainders are exact for many budgets`() {
        val random = Random(20261027)
        repeat(3000) {
            val n = random.nextInt(1, 9)
            val reserve = random.nextInt(0, 100001)
            val budget = random.nextLong(5L * n, 2_000_000L)
            val result = SplitPartitionCalculator.split(budget + reserve, n, FileSystemType.NTFS, true, reserve)
            assertEquals(budget, result.usableMb)
            assertEquals(budget, result.sumOfTypeMb)
            assertEquals(n, result.partitions.size)
            val buffer = PartitionConstants.DISPLAY_SAFETY_BUFFER_MB
            val baseBudget = budget - buffer * n
            val base = (baseBudget / n).toInt()
            val remainder = (baseBudget % n).toInt()
            assertEquals(buffer.toLong() * n, result.totalBufferMb)
            assertEquals(baseBudget, result.baseBudgetMb)
            assertEquals(remainder.toLong(), result.remainderMb)
            assertTrue(result.partitions.all { it.baseMb >= 1 && it.typeMb == it.baseMb + buffer })
            assertTrue(result.partitions.dropLast(1).all { it.baseMb == base })
            assertEquals(base + remainder, result.partitions.last().baseMb)
            assertEquals(base + remainder + buffer, result.partitions.last().typeMb)
            assertEquals(budget, result.partitions.sumOf { it.baseMb.toLong() } + result.totalBufferMb)
        }
    }

    @Test
    fun `split rejects invalid count size reserve and tiny partitions`() {
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(100, 0, FileSystemType.NTFS) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(100, 9, FileSystemType.NTFS) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(0, 1, FileSystemType.NTFS) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(134217729, 1, FileSystemType.NTFS) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(100, 1, FileSystemType.NTFS, true, 100) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(9, 2, FileSystemType.NTFS) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(100, 1, FileSystemType.NTFS, true, -1) }
        assertThrows(IllegalArgumentException::class.java) { SplitPartitionCalculator.split(100001, 1, FileSystemType.NTFS, true, 100001) }
        val minimum = SplitPartitionCalculator.split(10, 2, FileSystemType.NTFS)
        assertEquals(listOf(1, 1), minimum.partitions.map { it.baseMb })
        assertEquals(listOf(5, 5), minimum.partitions.map { it.typeMb })
        assertEquals(8L, minimum.totalBufferMb)
        assertEquals(10L, minimum.sumOfTypeMb)
        val largest = SplitPartitionCalculator.split(PartitionConstants.MAX_TOTAL_MB, 8, FileSystemType.NTFS)
        assertEquals(32L, largest.totalBufferMb)
        assertEquals(PartitionConstants.MAX_TOTAL_MB, largest.sumOfTypeMb)
    }

    @Test
    fun `filesystem warnings qualify tool limits and include existing MBR partitions`() {
        val atLimit = PartitionCalculator.filesystemWarnings(32768, FileSystemType.FAT32)
        assertFalse(atLimit.any { it.contains("32 GB") })
        assertTrue(atLimit.any { it.contains("smaller than 4 GiB") && it.contains("NTFS") })
        val aboveLimit = PartitionCalculator.filesystemWarnings(32769, FileSystemType.FAT32)
        assertTrue(aboveLimit.any { it.contains("Some Windows") && it.contains("32 GB") })
        assertTrue(PartitionCalculator.filesystemWarnings(100000, FileSystemType.NTFS).isEmpty())
        for (n in listOf(1, 4, 5, 8)) {
            val warnings = SplitPartitionCalculator.split(10000, n, FileSystemType.NTFS).warnings
            assertTrue(warnings.any { it.contains("MBR") && it.contains("existing", ignoreCase = true) })
        }
        val fatSplit = SplitPartitionCalculator.split(10000, 2, FileSystemType.FAT32)
        assertTrue(fatSplit.warnings.any { it.contains("buffer may be too small for FAT32") })
    }
}
