package com.diskcalc.core.calc

import com.diskcalc.core.model.FileSystemType
import org.junit.Assert.assertEquals
import org.junit.Test

class PartitionDiagnosticsTest {

    @Test
    fun `diagnostic trace keeps the original log format`() {
        val result = PartitionCalculator.calculate(100.0, FileSystemType.NTFS)

        assertEquals(
            """
            Partition size diagnostic:
              requested_gb: 100.0
              mib_sent_to_diskpart: 102404
              actual_bytes_reported (simulated): 107378376704
              explorer_displayed_gb (simulated): 100.0
            """.trimIndent(),
            PartitionDiagnostics.describe(100.0, result),
        )
    }

    @Test
    fun `diagnostic trace reports the simulated explorer size`() {
        val result = PartitionCalculator.calculate(2.5, FileSystemType.NTFS)

        assertEquals(
            """
            Partition size diagnostic:
              requested_gb: 2.5
              mib_sent_to_diskpart: 2564
              actual_bytes_reported (simulated): 2688548864
              explorer_displayed_gb (simulated): 2.5
            """.trimIndent(),
            PartitionDiagnostics.describe(2.5, result),
        )
    }
}
