package com.diskcalc.ui.result

import com.diskcalc.core.calc.PartitionCalculator
import com.diskcalc.core.calc.SplitPartitionCalculator
import com.diskcalc.core.model.FileSystemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Locks the dialog copy to the wording of the original app. */
class ResultContentTest {

    private val ntfsNoOverhead =
        PartitionCalculator.calculate(100.0, FileSystemType.NTFS)
    private val ntfsWithOverhead =
        PartitionCalculator.calculate(100.0, FileSystemType.NTFS, simulateWindowsSetup = true)

    @Test
    fun `calc rows match the original labels and values`() {
        val rows = CalcResultContent.rows(100.0, ntfsWithOverhead)

        assertEquals(
            listOf(
                "Desired visible size" to "100 GB",
                "Filesystem" to "NTFS",
                "Required input size" to "102970 MB",
                "Windows Setup simulation" to "Enabled (566 MB hidden overhead)",
            ),
            rows.map { it.label to it.value },
        )
    }

    @Test
    fun `calc rows show the disabled simulation copy`() {
        val rows = CalcResultContent.rows(100.0, ntfsNoOverhead)
        assertEquals("Disabled", rows.last().value)
    }

    @Test
    fun `calc hero value is the required input size`() {
        assertEquals(102970, CalcResultContent.heroValue(ntfsWithOverhead))
        assertEquals(102404, CalcResultContent.heroValue(ntfsNoOverhead))
    }

    @Test
    fun `calc explanation is empty without overhead and NTFS`() {
        assertEquals(
            "",
            CalcResultContent.explanation(100.0, FileSystemType.NTFS, false),
        )
    }

    @Test
    fun `calc explanation mentions the System Reserved overhead`() {
        assertEquals(
            "566 MB added for Windows Setup System Reserved partition overhead.",
            CalcResultContent.explanation(100.0, FileSystemType.NTFS, true),
        )
    }

    @Test
    fun `calc explanation always notes the FAT32 file size limit`() {
        assertEquals(
            "Note: FAT32 has a 4 GB maximum file size limit.",
            CalcResultContent.explanation(20.0, FileSystemType.FAT32, false),
        )
    }

    @Test
    fun `calc explanation combines overhead and both FAT32 warnings`() {
        assertEquals(
            "566 MB added for Windows Setup System Reserved partition overhead.\n" +
                "Warning: Partition size exceeds 32 GB limit for FAT32.\n" +
                "Note: FAT32 has a 4 GB maximum file size limit.",
            CalcResultContent.explanation(100.0, FileSystemType.FAT32, true),
        )
    }

    @Test
    fun `calc dialog title matches the original`() {
        val dialog = CalcResultContent.dialog(100.0, ntfsWithOverhead)
        assertEquals("Calculation Result", dialog.title)
        assertEquals(102970, dialog.heroValueMb)
        assertEquals(4, dialog.rows.size)
    }

    @Test
    fun `split rows are numbered one per partition`() {
        val plans = SplitPartitionCalculator.splitPlan(476923.0, 4)
        val rows = SplitResultContent.rows(plans)

        assertEquals(4, rows.size)
        assertEquals("Partition 1:" to "119230.75 MB", rows.first().label to rows.first().value)
        assertEquals("Partition 4:" to "119230.75 MB", rows.last().label to rows.last().value)
    }

    @Test
    fun `split explanation only appears for FAT32`() {
        assertEquals("", SplitResultContent.explanation(119.0, FileSystemType.NTFS))
        assertEquals(
            "Warning: Partition size exceeds 32 GB limit for FAT32.\n" +
                "Note: FAT32 has a 4 GB maximum file size limit.",
            SplitResultContent.explanation(119.0, FileSystemType.FAT32),
        )
        assertEquals(
            "Note: FAT32 has a 4 GB maximum file size limit.",
            SplitResultContent.explanation(10.0, FileSystemType.FAT32),
        )
    }

    @Test
    fun `split dialog has no hero number and the split title`() {
        val plans = SplitPartitionCalculator.splitPlan(100000.0, 5)
        val dialog = SplitResultContent.dialog(plans, FileSystemType.NTFS)

        assertEquals("Split Partitions Result", dialog.title)
        assertNull(dialog.heroValueMb)
        assertTrue(dialog.explanation.isEmpty())
        assertEquals("Partition 5:", dialog.rows.last().label)
        assertEquals("20000 MB", dialog.rows.last().value)
    }
}
