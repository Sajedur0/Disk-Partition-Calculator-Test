package com.diskcalc.ui.result

import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.PartitionCalculationResult
import com.diskcalc.core.model.SplitPartitionPlan
import com.diskcalc.core.model.toFormattedString
import com.diskcalc.ui.text.AppStrings

/** One labelled row of the result dialog. */
data class ResultRow(val label: String, val value: String)

/** Everything the result dialog renders. */
data class ResultDialogContent(
    val title: String,
    val rows: List<ResultRow>,
    val explanation: String,
    val heroValueMb: Int?,
)

/**
 * Builds the Calc tab result content.
 *
 * Row labels, formatting and the FAT32 / Windows Setup explanation strings are copied verbatim
 * from the original app so the dialog reads exactly the same.
 */
object CalcResultContent {

    /** The single number the user has to type into Windows disk setup. */
    fun heroValue(result: PartitionCalculationResult): Int = result.requiredInputMb

    fun rows(visibleGb: Double, result: PartitionCalculationResult): List<ResultRow> = listOf(
        ResultRow(
            label = AppStrings.Result.ROW_VISIBLE_SIZE,
            value = "${visibleGb.toFormattedString()} ${AppStrings.Result.GB}",
        ),
        ResultRow(
            label = AppStrings.Result.ROW_FILESYSTEM,
            value = result.filesystem.label,
        ),
        ResultRow(
            label = AppStrings.Result.ROW_REQUIRED_INPUT,
            value = "${result.requiredInputMb} ${AppStrings.Result.MB}",
        ),
        ResultRow(
            label = AppStrings.Result.ROW_WINDOWS_SETUP,
            value = if (result.usesWindowsSetupOverhead) {
                AppStrings.Result.WINDOWS_SETUP_ENABLED
            } else {
                AppStrings.Result.WINDOWS_SETUP_DISABLED
            },
        ),
    )

    /**
     * Explanation block: the Windows Setup note first, then the FAT32 warnings, joined with
     * newlines — the exact concatenation order of the original implementation.
     */
    fun explanation(
        visibleGb: Double,
        filesystem: FileSystemType,
        usesWindowsSetupOverhead: Boolean,
    ): String {
        var explanation = if (usesWindowsSetupOverhead) {
            AppStrings.Result.EXPLANATION_OVERHEAD
        } else {
            ""
        }

        if (filesystem == FileSystemType.FAT32) {
            if (visibleGb > 32.0) {
                explanation = explanation.appendingLine(AppStrings.Result.FAT32_SIZE_WARNING)
            }
            explanation = explanation.appendingLine(AppStrings.Result.FAT32_FILE_LIMIT_NOTE)
        }

        return explanation
    }

    fun dialog(visibleGb: Double, result: PartitionCalculationResult): ResultDialogContent =
        ResultDialogContent(
            title = AppStrings.Result.CALC_TITLE,
            rows = rows(visibleGb, result),
            explanation = explanation(
                visibleGb = visibleGb,
                filesystem = result.filesystem,
                usesWindowsSetupOverhead = result.usesWindowsSetupOverhead,
            ),
            heroValueMb = heroValue(result),
        )
}

/** Builds the Split tab result content (one row per partition). */
object SplitResultContent {

    fun rows(plans: List<SplitPartitionPlan>): List<ResultRow> =
        plans.mapIndexed { index, plan ->
            ResultRow(
                label = "${AppStrings.Result.PARTITION_ROW_PREFIX} ${index + 1}:",
                value = "${plan.displayMB.toFormattedString()} ${AppStrings.Result.MB}",
            )
        }

    fun explanation(partitionSizeGb: Double, filesystem: FileSystemType): String =
        if (filesystem == FileSystemType.FAT32) {
            if (partitionSizeGb > 32.0) {
                AppStrings.Result.FAT32_SIZE_WARNING +
                    "\n" +
                    AppStrings.Result.FAT32_FILE_LIMIT_NOTE
            } else {
                AppStrings.Result.FAT32_FILE_LIMIT_NOTE
            }
        } else {
            ""
        }

    fun dialog(plans: List<SplitPartitionPlan>, filesystem: FileSystemType): ResultDialogContent {
        val partitionSizeGb = (plans.firstOrNull()?.displayMB ?: 0.0) / 1024.0
        return ResultDialogContent(
            title = AppStrings.Result.SPLIT_TITLE,
            rows = rows(plans),
            explanation = explanation(partitionSizeGb, filesystem),
            heroValueMb = null,
        )
    }
}

private fun String.appendingLine(line: String): String =
    if (isEmpty()) line else "$this\n$line"
