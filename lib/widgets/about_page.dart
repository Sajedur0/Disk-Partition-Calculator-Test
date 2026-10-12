import 'package:flutter/material.dart';
import '../constants/app_constants.dart';
import '../app_theme.dart';

class AboutPage extends StatefulWidget {
  const AboutPage({super.key});

  @override
  State<AboutPage> createState() => _AboutPageState();
}

class _AboutPageState extends State<AboutPage> {
  String _version = '';

  @override
  void initState() {
    super.initState();
    _loadVersion();
  }

  Future<void> _loadVersion() async {
    final version = await getAppVersion();
    if (!mounted) return;
    setState(() {
      _version = 'v$version';
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Scaffold(
      appBar: AppBar(title: const Text('About'), centerTitle: false),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(24, 16, 24, 32),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: colorScheme.primaryContainer,
                shape: BoxShape.circle,
              ),
              child: ClipOval(
                child: Image.asset(
                  'assets/icon.png',
                  width: 64,
                  height: 64,
                  fit: BoxFit.cover,
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              appName,
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w700,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerLow,
                borderRadius: BorderRadius.circular(AppRadius.sm),
              ),
              child: Text(
                _version.isEmpty ? '' : _version,
                style: theme.textTheme.labelSmall?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            const SizedBox(height: 24),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerLow,
                borderRadius: BorderRadius.circular(AppRadius.md),
                border: appCardBorder(colorScheme),
                boxShadow: AppShadows.card(theme.brightness),
              ),
              child: Column(
                children: [
                  Text(
                    'About This App',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'Converts a desired visible GB capacity into the exact MB input required by partitioning tools, accounting for filesystem-specific overhead and optional Windows Setup System Reserved partition.',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: colorScheme.onSurfaceVariant,
                      height: 1.4,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 10),
                  Text(
                    'NTFS ratio: 1024.5 MB/GB. FAT32 ratio: 1027.6 MB/GB. Optionally simulate the 566 MB System Reserved overhead Windows Setup adds to the first primary partition.',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurfaceVariant,
                      height: 1.4,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 28),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Key Features',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            const SizedBox(height: 14),
            _buildFeatureCard(
              context,
              Icons.speed_outlined,
              'Quick Presets',
              'One-tap selection of common partition sizes (20GB, 50GB, 100GB, 250GB, 500GB, 1TB).',
            ),
            const SizedBox(height: 12),
            _buildFeatureCard(
              context,
              Icons.vertical_split_outlined,
              'Split Partitions',
              'Divide total disk capacity into equal partitions for multi-drive setup.',
            ),
            const SizedBox(height: 12),
            _buildFeatureCard(
              context,
              Icons.dns_outlined,
              'NTFS & FAT32 Support',
              'Accurate conversion using Windows internal ratios: 1024.5 MB/GB for NTFS, 1027.6 MB/GB for FAT32.',
            ),
            const SizedBox(height: 12),
            _buildFeatureCard(
              context,
              Icons.settings_suggest_outlined,
              'Windows Setup Simulation',
              'Optionally include 566 MB System Reserved partition overhead.',
            ),
            const SizedBox(height: 12),
            _buildFeatureCard(
              context,
              Icons.history_rounded,
              'Local History',
              'All calculations are saved locally on your device for later review. No data is ever transmitted or shared.',
            ),
            const SizedBox(height: 28),
            Text(
              'Made with ❤️ in Bangladesh 🇧🇩',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFeatureCard(
    BuildContext context,
    IconData icon,
    String title,
    String description,
  ) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: appCardBorder(colorScheme),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: colorScheme.primary.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(AppRadius.sm),
            ),
            child: Icon(icon, color: colorScheme.primary, size: 22),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  description,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: colorScheme.onSurfaceVariant,
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
