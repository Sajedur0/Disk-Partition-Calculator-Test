package com.diskcalc.ui

import com.diskcalc.domain.CapacitySample
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.SplitPartitionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTextTest {
    @Test
    fun `result labels preserve target bytes and qualify the calculation method`() {
        val fallback = PartitionCalculator.calculate(50.0, FileSystemType.NTFS)
        val measured = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = CapacitySample(10000, 5904))
        assertEquals("53,687,091,200", ResultText.bytes(fallback.targetCapacityBytes))
        assertEquals("51204", ResultText.calc(fallback))
        assertEquals("51205", ResultText.calc(measured))
        assertTrue(ResultText.capacityCaution(fallback).contains("not a Microsoft rule"))
        assertTrue(ResultText.capacityCaution(measured).contains("gap repeats"))
        assertTrue(ResultText.capacityCaution(measured).contains("verify actual total capacity"))
        assertEquals("50.00", ResultText.bytesToGib(measured.estimatedCapacityBytes!!))
        assertEquals("50.00", ResultText.mbToGb(51200))
        assertNull(ResultText.targetVolumeAdvice(measured))
        assertNull(ResultText.calcAdditional(measured))
    }

    @Test
    fun `split clipboard copies final sizes with the automatic buffer exactly once`() {
        val split = SplitPartitionCalculator.split(102408, 2, FileSystemType.NTFS)
        assertEquals("51204\n51204", ResultText.split(split))
        assertTrue(ResultText.capacityCaution(PartitionCalculator.calculate(50.0, FileSystemType.NTFS))
            .contains("already included"))
    }

    @Test
    fun `existing volume advice distinguishes below target met target and FAT32`() {
        val target = 50 * com.diskcalc.domain.PartitionConstants.BYTES_PER_GB
        val below = CapacitySample(target, target - 4096, isTargetVolume = true)
        val ntfs = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, sample = below)
        val advice = ResultText.targetVolumeAdvice(ntfs)!!
        assertEquals("5", ResultText.calcAdditional(ntfs))
        assertEquals("51205", ResultText.calc(ntfs))
        assertTrue(advice.contains("not the new-volume total"))
        assertTrue(advice.contains("Back up"))
        assertTrue(advice.contains("immediately after"))
        assertTrue(advice.contains("system or recovery"))

        val fat32 = PartitionCalculator.calculate(50.0, FileSystemType.FAT32, sample = below)
        assertNull(ResultText.calcAdditional(fat32))
        assertTrue(ResultText.targetVolumeAdvice(fat32)!!.contains("cannot extend FAT32"))
        val enough = PartitionCalculator.calculate(
            50.0, FileSystemType.NTFS, sample = CapacitySample(target + 1, target, true)
        )
        val enoughAdvice = ResultText.targetVolumeAdvice(enough)!!
        assertNull(ResultText.calcAdditional(enough))
        assertTrue(enoughAdvice.contains("at or above"))
        assertTrue(enoughAdvice.contains("do not shrink"))
    }
}
