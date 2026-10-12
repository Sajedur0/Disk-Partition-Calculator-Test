import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../app_theme.dart';
import '../models/partition_models.dart';

Widget buildHighlightedResultRow(
  BuildContext context,
  String label,
  String value,
) {
  final theme = Theme.of(context);
  final colorScheme = theme.colorScheme;

  return Padding(
    padding: const EdgeInsets.symmetric(vertical: 8),
    child: RichText(
      text: TextSpan(
        style: theme.textTheme.bodyMedium?.copyWith(
          color: colorScheme.onSurface,
        ),
        children: [
          TextSpan(
            text: '$label: ',
            style: const TextStyle(fontWeight: FontWeight.w500),
          ),
          TextSpan(
            text: value,
            style: theme.textTheme.bodyMedium?.copyWith(
              fontWeight: FontWeight.w800,
              color: colorScheme.primary,
            ),
          ),
        ],
      ),
    ),
  );
}

void showResultDialog(
  BuildContext context,
  String title,
  List<Widget> resultRows,
  String explanation, {
  int? requiredInputMb,
}) {
  showDialog(
    context: context,
    barrierDismissible: true,
    builder: (context) {
      final theme = Theme.of(context);
      final colorScheme = theme.colorScheme;

      return Dialog(
        backgroundColor: Colors.transparent,
        insetPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
        child: TweenAnimationBuilder<double>(
          duration: AppMotion.slow,
          curve: Curves.easeOutCubic,
          tween: Tween<double>(begin: 0.94, end: 1.0),
          builder: (context, scale, child) {
            return Transform.scale(scale: scale, child: child);
          },
          child: Container(
            decoration: BoxDecoration(
              color: colorScheme.surface,
              borderRadius: BorderRadius.circular(AppRadius.xl),
              boxShadow: AppShadows.raised(theme.brightness),
            ),
            clipBehavior: Clip.antiAlias,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(24, 24, 24, 4),
                  child: Row(
                    children: [
                      Icon(
                        Icons.check_circle_rounded,
                        color: colorScheme.primary,
                        size: 22,
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          title,
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),

                Padding(
                  padding: const EdgeInsets.fromLTRB(24, 16, 24, 16),
                  child: Column(
                    children: [
                      if (requiredInputMb != null) ...[
                        Container(
                          width: double.infinity,
                          decoration: BoxDecoration(
                            color: colorScheme.primary.withValues(alpha: 0.08),
                            borderRadius: BorderRadius.circular(AppRadius.md),
                          ),
                          padding: const EdgeInsets.all(20),
                          child: Column(
                            children: [
                              Text(
                                'Value to Enter in Windows Disk Setup',
                                style: theme.textTheme.labelMedium?.copyWith(
                                  color: colorScheme.primary,
                                  fontWeight: FontWeight.w700,
                                  letterSpacing: 0.3,
                                ),
                                textAlign: TextAlign.center,
                              ),
                              const SizedBox(height: 8),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                crossAxisAlignment: CrossAxisAlignment.baseline,
                                textBaseline: TextBaseline.alphabetic,
                                children: [
                                  Text(
                                    '$requiredInputMb',
                                    style: theme.textTheme.headlineLarge?.copyWith(
                                      fontWeight: FontWeight.w800,
                                      color: colorScheme.primary,
                                      fontSize: 36,
                                    ),
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    'MB',
                                    style: theme.textTheme.titleMedium?.copyWith(
                                      fontWeight: FontWeight.w700,
                                      color: colorScheme.primary.withValues(
                                        alpha: 0.7,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 14),
                              FilledButton.icon(
                                onPressed: () {
                                  Clipboard.setData(
                                    ClipboardData(
                                      text: requiredInputMb.toString(),
                                    ),
                                  );
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text('Value copied to clipboard'),
                                    ),
                                  );
                                },
                                icon: const Icon(Icons.copy_rounded, size: 18),
                                label: const Text('Copy to Clipboard'),
                                style: FilledButton.styleFrom(
                                  backgroundColor: colorScheme.primary,
                                  foregroundColor: colorScheme.onPrimary,
                                  minimumSize: const Size(double.infinity, 46),
                                ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 14),
                      ],

                      Container(
                        decoration: BoxDecoration(
                          color: colorScheme.surfaceContainer,
                          borderRadius: BorderRadius.circular(AppRadius.md),
                          border: appCardBorder(colorScheme),
                        ),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 6,
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: resultRows.asMap().entries.map((entry) {
                            final isLast = entry.key == resultRows.length - 1;
                            return Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                entry.value,
                                if (!isLast) const Divider(height: 1),
                              ],
                            );
                          }).toList(),
                        ),
                      ),

                      if (explanation.isNotEmpty) ...[
                        const SizedBox(height: 14),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Icon(
                              Icons.info_outline_rounded,
                              size: 16,
                              color: colorScheme.onSurfaceVariant,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                explanation,
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: colorScheme.onSurfaceVariant,
                                  height: 1.3,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),

                Padding(
                  padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
                  child: OutlinedButton(
                    onPressed: () => Navigator.of(context).pop(),
                    style: OutlinedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: const Text('Close'),
                  ),
                ),
              ],
            ),
          ),
        ),
      );
    },
  );
}

List<Widget> buildResultRows(
  BuildContext context,
  double visibleGb,
  String filesystemLabel,
  int requiredInputMb,
  bool usesWindowsSetupOverhead,
) {
  return [
    buildHighlightedResultRow(
      context,
      'Desired visible size',
      '${visibleGb.toFormattedString()} GB',
    ),
    buildHighlightedResultRow(context, 'Filesystem', filesystemLabel),
    buildHighlightedResultRow(
      context,
      'Required input size',
      '$requiredInputMb MB',
    ),
    buildHighlightedResultRow(
      context,
      'Windows Setup simulation',
      usesWindowsSetupOverhead
          ? 'Enabled (566 MB hidden overhead)'
          : 'Disabled',
    ),
  ];
}

String getExplanation(bool usesWindowsSetupOverhead) {
  return usesWindowsSetupOverhead
      ? '566 MB added for Windows Setup System Reserved partition overhead.'
      : '';
}
