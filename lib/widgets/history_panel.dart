import 'package:flutter/material.dart';
import '../managers/history_manager.dart';
import '../models/partition_models.dart';
import '../app_theme.dart';

class HistoryPanel extends StatelessWidget {
  final HistoryManager historyManager;
  final VoidCallback onClearHistory;

  const HistoryPanel({
    super.key,
    required this.historyManager,
    required this.onClearHistory,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final history = historyManager.history;

    return Container(
      height: MediaQuery.of(context).size.height * 0.75,
      decoration: BoxDecoration(
        color: colorScheme.surface,
        borderRadius: const BorderRadius.vertical(
          top: Radius.circular(AppRadius.xl),
        ),
      ),
      child: Column(
        children: [
          Container(
            margin: const EdgeInsets.only(top: 12),
            width: 36,
            height: 4,
            decoration: BoxDecoration(
              color: colorScheme.outlineVariant,
              borderRadius: BorderRadius.circular(4),
            ),
          ),
          const SizedBox(height: 16),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Row(
              children: [
                Text(
                  'Conversion History',
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const Spacer(),
                if (history.isNotEmpty)
                  TextButton(
                    onPressed: () async {
                      await historyManager.clearHistory();
                      onClearHistory();
                      if (context.mounted) {
                        Navigator.of(context).pop();
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('History cleared'),
                            duration: Duration(seconds: 2),
                          ),
                        );
                      }
                    },
                    style: TextButton.styleFrom(
                      foregroundColor: colorScheme.error,
                    ),
                    child: const Text('Clear All'),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          const Divider(height: 1),
          const SizedBox(height: 8),
          Expanded(
            child: history.isEmpty
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 32),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            padding: const EdgeInsets.all(24),
                            decoration: BoxDecoration(
                              color: colorScheme.primary.withValues(
                                alpha: 0.08,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.history_toggle_off_rounded,
                              size: 72,
                              color: colorScheme.primary.withValues(alpha: 0.7),
                            ),
                          ),
                          const SizedBox(height: 24),
                          Text(
                            'No History Yet',
                            style: theme.textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: colorScheme.onSurface,
                            ),
                          ),
                          const SizedBox(height: 12),
                          Text(
                            'Calculated partition sizes will be saved here automatically',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: colorScheme.onSurfaceVariant.withValues(
                                alpha: 0.7,
                              ),
                            ),
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    ),
                  )
                : SingleChildScrollView(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      children: [
                        for (final entry in history) _HistoryCard(entry: entry),
                      ],
                    ),
                  ),
          ),
        ],
      ),
    );
  }
}

class _HistoryCard extends StatelessWidget {
  final ConversionHistory entry;

  const _HistoryCard({required this.entry});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final int displayHour = entry.timestamp.hour % 12 == 0
        ? 12
        : entry.timestamp.hour % 12;
    final String minute = entry.timestamp.minute.toString().padLeft(2, '0');
    final String period = entry.timestamp.hour >= 12 ? 'PM' : 'AM';
    final String dateTimeText =
        '${entry.timestamp.day}/${entry.timestamp.month.toString().padLeft(2, '0')}/${entry.timestamp.year} ${displayHour.toString().padLeft(2, '0')}:$minute $period';

    // Determine if this is a split partition entry
    final bool isSplit = entry.splitCount != null;

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainer,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: appCardBorder(colorScheme),
        boxShadow: AppShadows.card(theme.brightness),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        isSplit
                            ? 'Total Disk Capacity'
                            : 'Desired visible size',
                        style: theme.textTheme.bodySmall?.copyWith(
                          fontWeight: FontWeight.w700,
                          color: colorScheme.onSurfaceVariant,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '${entry.inputValue.toFormattedString()} ${isSplit ? 'MB' : 'GB'}',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                          color: colorScheme.primary,
                        ),
                      ),
                    ],
                  ),
                ),
                Text(
                  dateTimeText,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: colorScheme.onSurfaceVariant,
                    fontSize: 11,
                  ),
                  textAlign: TextAlign.right,
                ),
              ],
            ),
            const SizedBox(height: 18),
            if (isSplit) ...[
              _buildHistoryLine(
                context,
                'Number of Splits',
                '${entry.splitCount}',
              ),
              const SizedBox(height: 14),
              _buildHistoryLine(
                context,
                'Displayed Partition Size',
                '${entry.splitPartitionDisplayMb!.toFormattedString()} MB',
              ),
            ] else ...[
              _buildHistoryLine(
                context,
                'Filesystem type',
                entry.filesystem == FileSystemType.fat32 ? 'FAT32' : 'NTFS',
              ),
              const SizedBox(height: 14),
              _buildHistoryLine(
                context,
                'Required input size',
                '${entry.requiredInputMb} MB',
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildHistoryLine(BuildContext context, String label, String value) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(
          child: Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(
              color: colorScheme.onSurfaceVariant,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        Text(
          value,
          style: theme.textTheme.bodyMedium?.copyWith(
            fontWeight: FontWeight.w700,
            color: colorScheme.primary,
          ),
          textAlign: TextAlign.right,
        ),
      ],
    );
  }
}
