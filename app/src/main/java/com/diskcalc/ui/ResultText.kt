package com.diskcalc.ui

import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitResult
import java.text.NumberFormat
import java.util.Locale

/** Clipboard payloads and shared formatting. Values are estimates, not guaranteed Setup sizes. */
object ResultText {

    /** Calc: the suggested NEW partition's total size in whole MB. */
    fun calc(result: PartitionCalculationResult): String = result.requiredInputMb.toString()

    /** Additional space is only offered for a below-target existing NTFS volume. */
    fun calcAdditional(result: PartitionCalculationResult): String? =
        if (result.filesystem == FileSystemType.NTFS) result.estimatedAdditionalMb?.toString() else null

    /** Split: one MB value per partition, newest list order preserved. */
    fun split(result: SplitResult): String =
        result.partitions.joinToString(separator = "\n") { it.typeMb.toString() }

    /** Converts binary MB to a nominal GiB input size, not a formatted volume estimate. */
    fun mbToGb(mb: Int): String =
        String.format(Locale.US, "%.2f", mb / PartitionConstants.MB_PER_GB.toDouble())

    /** Group bytes for display; editable fields still accept ungrouped digits only. */
    fun bytes(value: Long): String = NumberFormat.getIntegerInstance(Locale.US).format(value)

    /** A model conversion, not a prediction of Explorer's rounding or actual capacity. */
    fun bytesToGib(value: Long): String =
        String.format(Locale.US, "%.2f", value / PartitionConstants.BYTES_PER_GB.toDouble())

    fun capacityCaution(result: PartitionCalculationResult): String =
        if (result.measuredGapBytes == null) {
            "The +${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB buffer aims to avoid a lower display, but is an app assumption, not a Microsoft rule. Verify actual total capacity, not free space, in Explorer Properties."
        } else {
            "* The new-volume model uses the measured gap plus a ${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB app buffer. It assumes Windows accepts the input and the gap repeats; verify actual total capacity. Explorer may round differently."
        }

    /** Advice only for a sample explicitly identified as the drive being corrected. */
    fun targetVolumeAdvice(result: PartitionCalculationResult): String? {
        val shortfall = result.targetShortfallBytes ?: return null
        return when {
            shortfall == 0L ->
                "The measured capacity is at or above the target in bytes. A lower free-space value can be normal. Check Explorer's actual Capacity label; do not shrink the drive to match the new-volume suggestion."
            result.filesystem == FileSystemType.FAT32 ->
                "Windows Disk Management cannot extend FAT32 volumes with Extend Volume. Back up and review the layout; do not delete or format a partition just to change the display."
            else ->
                "The extra size is an estimate for this existing volume, not the new-volume total above. It assumes capacity grows by the added amount. Back up first; Windows needs suitable unallocated space immediately after the NTFS volume on the same disk. Do not delete system or recovery partitions to make room. Recheck Capacity afterwards."
        }
    }

    /** Trims trailing zeros: 10.0 -> "10", 2.50 -> "2.5". */
    fun trim(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
