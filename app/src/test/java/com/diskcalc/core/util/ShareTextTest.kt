package com.diskcalc.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ShareTextTest {

    @Test
    fun `share text matches the original wording and layout`() {
        assertEquals(
            """
            Disk Partition Calculator

            Calculate exact and recommended partition sizes for Windows.

            Download now: https://play.google.com/store/apps/details?id=com.diskcalc

            Version: 1.1.3 (14)
            """.trimIndent(),
            ShareText.build(versionName = "1.1.3", versionCode = 14),
        )
    }
}
