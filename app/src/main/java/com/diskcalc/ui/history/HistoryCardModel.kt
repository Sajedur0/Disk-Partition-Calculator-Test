package com.diskcalc.ui.history

import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.core.model.toFormattedString
import com.diskcalc.ui.result.ResultRow
import com.diskcalc.ui.text.AppStrings
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * Presentation model of one history card, so the exact same copy can be unit tested:
 * headline label/value, the timestamp rendering and the two detail rows.
 */
data class HistoryCardModel(
    val headlineLabel: String,
    val headlineValue: String,
    val timestamp: String,
    val details: List<ResultRow>,
)

object HistoryCardModelFactory {

    fun from(entry: ConversionHistory, zone: ZoneId = ZoneId.systemDefault()): HistoryCardModel {
        val value = entry.inputValue.toFormattedString()
        return if (entry.isSplit) {
            HistoryCardModel(
                headlineLabel = AppStrings.History.LABEL_TOTAL_CAPACITY,
                headlineValue = "$value ${AppStrings.History.UNIT_MB}",
                timestamp = formatTimestamp(entry.timestamp, zone),
                details = listOf(
                    ResultRow(
                        label = AppStrings.History.LABEL_SPLIT_COUNT,
                        value = entry.splitCount?.toString().orEmpty(),
                    ),
                    ResultRow(
                        label = AppStrings.History.LABEL_SPLIT_DISPLAY_SIZE,
                        value = "${entry.splitPartitionDisplayMb?.toFormattedString().orEmpty()} " +
                            AppStrings.History.UNIT_MB,
                    ),
                ),
            )
        } else {
            HistoryCardModel(
                headlineLabel = AppStrings.History.LABEL_VISIBLE_SIZE,
                headlineValue = "$value ${AppStrings.History.UNIT_GB}",
                timestamp = formatTimestamp(entry.timestamp, zone),
                details = listOf(
                    ResultRow(
                        label = AppStrings.History.LABEL_FILESYSTEM,
                        value = entry.filesystem.label,
                    ),
                    ResultRow(
                        label = AppStrings.History.LABEL_REQUIRED_INPUT,
                        value = "${entry.requiredInputMb} ${AppStrings.History.UNIT_MB}",
                    ),
                ),
            )
        }
    }

    /**
     * `d/MM/yyyy hh:mm a` — the exact rendering the original history card produced:
     * unpadded day, padded month, 12-hour clock with a padded minute and an AM/PM suffix.
     */
    fun formatTimestamp(timestamp: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val dateTime = timestamp.atZone(zone)
        val hourOfDay = dateTime.hour
        val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
        val minute = dateTime.minute.toString().padStart(2, '0')
        val period = if (hourOfDay >= 12) "PM" else "AM"

        return String.format(
            Locale.US,
            "%d/%02d/%d %02d:%s %s",
            dateTime.dayOfMonth,
            dateTime.monthValue,
            dateTime.year,
            displayHour,
            minute,
            period,
        )
    }
}
