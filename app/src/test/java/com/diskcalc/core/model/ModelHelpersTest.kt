package com.diskcalc.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelHelpersTest {

    @Test
    fun `formats sizes the way the original app rendered them`() {
        assertEquals("100", 100.0.toFormattedString())
        assertEquals("1024", 1024.0.toFormattedString())
        assertEquals("0", 0.0.toFormattedString())
        assertEquals("119230.75", 119230.75.toFormattedString())
        assertEquals("100.50", 100.5.toFormattedString())
        assertEquals("2.50", 2.5.toFormattedString())
    }

    @Test
    fun `theme mode keeps the persisted ordinal mapping`() {
        assertEquals(0, ThemeMode.SYSTEM.ordinal)
        assertEquals(1, ThemeMode.LIGHT.ordinal)
        assertEquals(2, ThemeMode.DARK.ordinal)

        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromIndex(0))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromIndex(1))
        assertEquals(ThemeMode.DARK, ThemeMode.fromIndex(2))
    }

    @Test
    fun `missing or unknown theme preference falls back to light`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromIndex(null))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromIndex(3))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromIndex(-1))
        assertNull(ThemeMode.fromIndexOrNull(null))
    }

    @Test
    fun `filesystem index and name mapping matches the original enum`() {
        assertEquals(FileSystemType.NTFS, FileSystemType.fromIndex(0))
        assertEquals(FileSystemType.FAT32, FileSystemType.fromIndex(1))
        assertEquals(FileSystemType.NTFS, FileSystemType.fromIndex(2))
        assertNull(FileSystemType.fromIndexOrNull(2))

        assertEquals(FileSystemType.NTFS, FileSystemType.fromNameOrNull("ntfs"))
        assertEquals(FileSystemType.FAT32, FileSystemType.fromNameOrNull("FAT32"))
        assertNull(FileSystemType.fromNameOrNull("exfat"))
        assertNull(FileSystemType.fromNameOrNull(null))
    }

    @Test
    fun `filesystem labels are the user visible strings`() {
        assertEquals("NTFS", FileSystemType.NTFS.label)
        assertEquals("FAT32", FileSystemType.FAT32.label)
    }
}
