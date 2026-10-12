import 'package:flutter/material.dart';
import '../app_theme.dart';
import '../models/partition_models.dart';

/// A toggle chip pair used to pick between NTFS and FAT32.
/// Shared by the Calc, Split and Settings screens so the selection
/// interaction is consistent everywhere.
class FilesystemToggle extends StatelessWidget {
  final FileSystemType selectedValue;
  final ValueChanged<FileSystemType> onChanged;

  const FilesystemToggle({
    super.key,
    required this.selectedValue,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final isNtfs = selectedValue == FileSystemType.ntfs;

    return Container(
      height: 52,
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainer,
        borderRadius: BorderRadius.circular(AppRadius.md),
      ),
      padding: const EdgeInsets.all(4),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final width = constraints.maxWidth / 2;
          return Stack(
            children: [
              AnimatedPositioned(
                duration: AppMotion.base,
                curve: AppMotion.curve,
                left: isNtfs ? 0 : width,
                width: width,
                height: 44,
                child: Container(
                  decoration: BoxDecoration(
                    color: colorScheme.primary,
                    borderRadius: BorderRadius.circular(AppRadius.sm),
                  ),
                ),
              ),
              Row(
                children: [
                  Expanded(
                    child: InkWell(
                      borderRadius: BorderRadius.circular(AppRadius.sm),
                      onTap: () => onChanged(FileSystemType.ntfs),
                      child: Center(
                        child: AnimatedDefaultTextStyle(
                          duration: AppMotion.fast,
                          style: theme.textTheme.titleMedium!.copyWith(
                            fontWeight: FontWeight.w700,
                            color: isNtfs
                                ? colorScheme.onPrimary
                                : colorScheme.onSurfaceVariant,
                          ),
                          child: const Text('NTFS'),
                        ),
                      ),
                    ),
                  ),
                  Expanded(
                    child: InkWell(
                      borderRadius: BorderRadius.circular(AppRadius.sm),
                      onTap: () => onChanged(FileSystemType.fat32),
                      child: Center(
                        child: AnimatedDefaultTextStyle(
                          duration: AppMotion.fast,
                          style: theme.textTheme.titleMedium!.copyWith(
                            fontWeight: FontWeight.w700,
                            color: !isNtfs
                                ? colorScheme.onPrimary
                                : colorScheme.onSurfaceVariant,
                          ),
                          child: const Text('FAT32'),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          );
        },
      ),
    );
  }
}

/// A tap-and-switch card that toggles the Windows Setup simulation.
/// Shared by the Calc and Split screens.
class SimulationCard extends StatelessWidget {
  final bool value;
  final ValueChanged<bool> onChanged;

  const SimulationCard({
    super.key,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(AppRadius.sm),
        onTap: () => onChanged(!value),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Row(
            children: [
              AnimatedContainer(
                duration: AppMotion.base,
                curve: AppMotion.curve,
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: value
                      ? colorScheme.primary.withValues(alpha: 0.12)
                      : colorScheme.surfaceContainer,
                  borderRadius: BorderRadius.circular(AppRadius.sm),
                ),
                child: Icon(
                  Icons.dns_outlined,
                  color: value
                      ? colorScheme.primary
                      : colorScheme.onSurfaceVariant,
                  size: 22,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Simulate Windows Setup',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: colorScheme.onSurface,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Adds 566 MB System Reserved overhead.',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              Switch.adaptive(
                value: value,
                onChanged: onChanged,
                activeTrackColor: colorScheme.primary,
                activeThumbColor: colorScheme.onPrimary,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Reusable section heading style used by the tabs.
class SectionHeader extends StatelessWidget {
  final String text;

  const SectionHeader(this.text, {super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 10, left: 2),
      child: Text(
        text,
        style: theme.textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.w700,
          letterSpacing: 0.2,
          color: theme.colorScheme.onSurfaceVariant,
        ),
      ),
    );
  }
}

/// Reusable card container used by the tabs, matching the app's
/// elevation / border language defined in [appCardBorder] / [AppShadows].
class SectionCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;

  const SectionCard({super.key, required this.child, this.padding});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return AnimatedContainer(
      duration: AppMotion.base,
      curve: AppMotion.curve,
      width: double.infinity,
      padding: padding ?? const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: appCardBorder(theme.colorScheme),
        boxShadow: AppShadows.card(theme.brightness),
      ),
      child: child,
    );
  }
}
