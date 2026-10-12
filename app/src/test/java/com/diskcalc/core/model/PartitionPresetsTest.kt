package com.diskcalc.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PartitionPresetsTest {

    @Test
    fun `preset list matches the original quick sizes`() {
        assertEquals(listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1024.0), PartitionPresets.visibleGb)
    }

    @Test
    fun `presets are labelled in GB and TB`() {
        assertEquals(
            listOf("20 GB", "50 GB", "100 GB", "250 GB", "500 GB", "1 TB"),
            PartitionPresets.visibleGb.map { PartitionPresets.labelFor(it) },
        )
    }
}
