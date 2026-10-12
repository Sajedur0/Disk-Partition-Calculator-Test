package com.diskcalc.core.calc

/**
 * Raised when a split calculation receives input it cannot work with.
 *
 * The original Dart implementation threw `ArgumentError`; Dart renders that as
 * `Invalid argument(s): <message>` and the app surfaced that string directly in a snack bar,
 * so [displayMessage] reproduces the exact same user visible text.
 */
class PartitionInputException(message: String) : IllegalArgumentException(message) {

    val displayMessage: String = "Invalid argument(s): $message"

    override fun toString(): String = displayMessage
}
