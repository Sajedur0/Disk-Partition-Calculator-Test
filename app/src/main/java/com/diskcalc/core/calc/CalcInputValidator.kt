package com.diskcalc.core.calc

import com.diskcalc.core.model.toSizeOrNull

/** Outcome of validating the size field of a tab. */
sealed interface InputValidation {

    /** Input is empty — the user is asked to type a value first. */
    data object Empty : InputValidation

    /** Input is not a number, or not a positive one. */
    data object Invalid : InputValidation

    /** Input is usable. */
    data class Valid(val value: Double) : InputValidation
}

/**
 * Validates the numeric input fields.
 *
 * The original app trimmed the text, showed "enter a value" for empty input and
 * "enter a valid positive value" for anything that failed to parse or was `<= 0`.
 */
object CalcInputValidator {

    fun validate(text: String): InputValidation {
        if (text.trim().isEmpty()) return InputValidation.Empty
        val value = text.toSizeOrNull() ?: return InputValidation.Invalid
        return if (value <= 0.0) InputValidation.Invalid else InputValidation.Valid(value)
    }
}
