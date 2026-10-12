package com.diskcalc.data

import java.util.UUID

/**
 * `UUID` — never a timestamp, so two conversions inside the same millisecond stay unique
 * (APP_LOGIC.md §6).
 */
internal fun newHistoryId(): String = UUID.randomUUID().toString()
