package com.diskcalc.core.calc

import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.PartitionCalculationResult
import kotlin.math.roundToInt

/**
 * Single-partition math: *visible GB -> required MB*.
 *
 * Ported one-to-one from the original Dart implementation — the constants and the order of
 * operations are intentionally identical so results stay byte-for-byte the same.
 */
object PartitionCalculator {

    /** System Reserved partition that Windows Setup inserts in front of the first partition. */
    const val hiddenWindowsSetupOverheadMb = 566

    /** Base conversion: 1 GB == 1024 MiB. */
    const val mbPerGb = 1024

    /** Fixed per-partition overhead caused by partition table / alignment. */
    const val overheadMbPerPartition = 4

    /** FAT32 cannot address a partition larger than 32 GB. */
    const val fat32MaxVisibleGb = 32.0

    /**
     * Converts a desired *visible* size into the MB value diskpart expects.
     * The conversion is identical for both filesystems.
     */
    fun visibleGbToPartitionMb(visibleGb: Double, filesystem: FileSystemType): Int =
        (visibleGb * mbPerGb).roundToInt() + overheadMbPerPartition

    /** MB the user has to type into Windows disk setup, including the optional setup overhead. */
    fun requiredInputMb(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
    ): Int {
        val baseMb = visibleGbToPartitionMb(visibleGb, filesystem)
        return if (simulateWindowsSetup) baseMb + hiddenWindowsSetupOverheadMb else baseMb
    }

    /** Converts a partition size back into the GB value Windows Explorer will report. */
    fun partitionMbToDisplayedGb(partitionMb: Int, filesystem: FileSystemType): Double =
        (partitionMb - overheadMbPerPartition) / mbPerGb.toDouble()

    /** Full calculation for the Calc tab. */
    fun calculate(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
    ): PartitionCalculationResult {
        val partitionSizeMb = visibleGbToPartitionMb(visibleGb, filesystem)
        val requiredInput = if (simulateWindowsSetup) {
            partitionSizeMb + hiddenWindowsSetupOverheadMb
        } else {
            partitionSizeMb
        }

        return PartitionCalculationResult(
            partitionSizeMb = partitionSizeMb,
            requiredInputMb = requiredInput,
            displayedGb = partitionMbToDisplayedGb(partitionSizeMb, filesystem),
            usesWindowsSetupOverhead = simulateWindowsSetup,
            filesystem = filesystem,
        )
    }

    /** `true` while the requested visible size is still addressable by FAT32 (<= 32 GB). */
    fun isFat32SizeValid(visibleGb: Double): Boolean = visibleGb <= fat32MaxVisibleGb
}
