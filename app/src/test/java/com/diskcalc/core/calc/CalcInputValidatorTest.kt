package com.diskcalc.core.calc

import com.diskcalc.core.model.toSizeOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalcInputValidatorTest {

    @Test
    fun `empty input is reported as empty`() {
        assertEquals(InputValidation.Empty, CalcInputValidator.validate(""))
        assertEquals(InputValidation.Empty, CalcInputValidator.validate("   "))
    }

    @Test
    fun `non numeric input is invalid`() {
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("abc"))
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("100 GB"))
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("12d"))
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("0x10"))
    }

    @Test
    fun `zero and negative values are invalid`() {
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("0"))
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("-5"))
        assertEquals(InputValidation.Invalid, CalcInputValidator.validate("0.0"))
    }

    @Test
    fun `positive values are accepted including decimals and exponents`() {
        assertEquals(InputValidation.Valid(100.0), CalcInputValidator.validate("100"))
        assertEquals(InputValidation.Valid(2.5), CalcInputValidator.validate("2.5"))
        assertEquals(InputValidation.Valid(0.5), CalcInputValidator.validate(".5"))
        assertEquals(InputValidation.Valid(1000.0), CalcInputValidator.validate("1e3"))
        assertEquals(InputValidation.Valid(476923.0), CalcInputValidator.validate(" 476923 "))
    }

    @Test
    fun `parsing never returns non finite numbers`() {
        assertNull("Infinity".toSizeOrNull())
        assertNull("NaN".toSizeOrNull())
        assertNull("1e999".toSizeOrNull())
        assertNull("".toSizeOrNull())
        assertNull("  ".toSizeOrNull())
        assertEquals(100.0, "100".toSizeOrNull()!!, 0.0)
    }
}
