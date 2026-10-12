package com.diskcalc.core.calc

import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Ported from the original Dart test suite — the numbers are the contract. */
class SplitPartitionCalculatorTest {

    @Test
    fun `calculates the fractional four-partition plan`() {
        assertEquals(
            119226.75,
            SplitPartitionCalculator.basePerPartitionMB(totalSizeMB = 476923.0, partitionCount = 4),
            0.000001,
        )
        assertEquals(
            119230.75,
            SplitPartitionCalculator.displayedPerPartitionMB(
                totalSizeMB = 476923.0,
                partitionCount = 4,
            ),
            0.000001,
        )
    }

    @Test
    fun `calculates the integer five-partition plan`() {
        assertEquals(
            19996.0,
            SplitPartitionCalculator.basePerPartitionMB(totalSizeMB = 100000.0, partitionCount = 5),
            0.0,
        )
        assertEquals(
            20000.0,
            SplitPartitionCalculator.displayedPerPartitionMB(
                totalSizeMB = 100000.0,
                partitionCount = 5,
            ),
            0.0,
        )
    }

    @Test
    fun `calculates the integer two-partition plan`() {
        assertEquals(
            24996.0,
            SplitPartitionCalculator.basePerPartitionMB(totalSizeMB = 50000.0, partitionCount = 2),
            0.0,
        )
        assertEquals(
            25000.0,
            SplitPartitionCalculator.displayedPerPartitionMB(
                totalSizeMB = 50000.0,
                partitionCount = 2,
            ),
            0.0,
        )
    }

    @Test
    fun `split plan keeps the creation size separate from the display size`() {
        val plan = SplitPartitionCalculator.splitPlan(totalSizeMB = 476923.0, partitionCount = 4)

        assertEquals(4, plan.size)
        assertEquals(119230.75, plan.first().displayMB, 0.000001)
        assertEquals(119226.75, plan.first().createMB, 0.000001)
        // diskpart only accepts whole MB values; rounding happens at the creation boundary.
        assertEquals(119227, plan.first().createMB.roundToInt())
        assertEquals(119231, plan.first().displayMB.roundToInt())
    }

    @Test
    fun `rejects a non-positive partition count`() {
        val error = assertThrows(PartitionInputException::class.java) {
            SplitPartitionCalculator.basePerPartitionMB(totalSizeMB = 100.0, partitionCount = 0)
        }
        assertEquals("partitionCount must be > 0", error.message)
        assertEquals("Invalid argument(s): partitionCount must be > 0", error.displayMessage)
    }

    @Test
    fun `rejects capacity at or below the total overhead`() {
        assertThrows(PartitionInputException::class.java) {
            SplitPartitionCalculator.basePerPartitionMB(totalSizeMB = 16.0, partitionCount = 4)
        }
        val error = assertThrows(PartitionInputException::class.java) {
            SplitPartitionCalculator.basePerPartitionMB(totalSizeMB = 15.0, partitionCount = 4)
        }
        assertEquals(
            "Invalid argument(s): Total size too small for the requested partition count",
            error.displayMessage,
        )
    }
}
