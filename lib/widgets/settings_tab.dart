import 'package:flutter/material.dart';
import '../constants/app_constants.dart';
import '../core/theme_reveal/theme_reveal_controller.dart';
import '../managers/history_manager.dart';
import '../models/partition_models.dart';
import 'about_page.dart';
import 'shared_controls.dart';

/// The "Settings" tab. It lets users toggle the active theme, pick the
/// default filesystem and Windows Setup simulation behaviour (these defaults
/// are reflected live in the Calc tab), clear the local calculation history,
/// and access app info/tools (Privacy Policy, Rate Us, Share, About, Quit).
class SettingsTab extends StatefulWidget {
  final VoidCallback onToggleTheme;
  final VoidCallback onPrivacyPolicyTap;
  final VoidCallback onRateUsTap;
  final VoidCallback onShareTap;
  final VoidCallback onQuitTap;
  final HistoryManager historyManager;
  final FileSystemType selectedFilesystem;
  final ValueChanged<FileSystemType> onFilesystemChanged;
  final bool simulateWindowsSetup;
  final ValueChanged<bool> onSimulateWindowsSetupChanged;

  const SettingsTab({
    super.key,
    required this.onToggleTheme,
    required this.onPrivacyPolicyTap,
    required this.onRateUsTap,
    required this.onShareTap,
    required this.onQuitTap,
    required this.historyManager,
    required this.selectedFilesystem,
    required this.onFilesystemChanged,
    required this.simulateWindowsSetup,
    required this.onSimulateWindowsSetupChanged,
  });

  @override
  State<SettingsTab> createState() => _SettingsTabState();
}

class _SettingsTabState extends State<SettingsTab> {
  String _version = '';

  @override
  void initState() {
    super.initState();
    _loadVersion();
  }

  Future<void> _loadVersion() async {
    final version = await getAppVersion();
    if (!mounted) return;
    setState(() => _version = version);
  }

  String get _themeLabel {
    final brightness = Theme.of(context).brightness;
    return brightness == Brightness.dark ? 'Dark Mode' : 'Light Mode';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final isDark = theme.brightness == Brightness.dark;

    return SafeArea(
      child: SingleChildScrollView(
        physics: const BouncingScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
children: [
            const SectionHeader('Appearance'),
            SectionCard(
              child: GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTapDown: (details) {
                  ThemeRevealController.of(context)
                      .revealFrom(details.globalPosition, widget.onToggleTheme);
                },
                child: ListTile(
                  leading: Icon(
                    isDark ? Icons.dark_mode_outlined : Icons.light_mode_outlined,
                    color: colorScheme.primary,
                  ),
                  title: const Text('Theme'),
                  subtitle: Text(
                    _themeLabel,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurfaceVariant,
                    ),
                  ),
                  trailing: Icon(
                    isDark ? Icons.dark_mode_outlined : Icons.light_mode_outlined,
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 20),

const SectionHeader('About'),
             SectionCard(
               child: Column(
                 children: [
                   ListTile(
                     leading: Icon(
                       Icons.share_outlined,
                       color: colorScheme.onSurfaceVariant,
                     ),
                     title: const Text('Share App'),
                     trailing: const Icon(Icons.chevron_right_rounded),
                     onTap: widget.onShareTap,
                   ),
                   const Divider(height: 1),
                   ListTile(
                     leading: Icon(
                       Icons.shield_outlined,
                       color: colorScheme.onSurfaceVariant,
                     ),
                     title: const Text('Privacy Policy'),
                     trailing: const Icon(Icons.chevron_right_rounded),
                     onTap: widget.onPrivacyPolicyTap,
                   ),
                   const Divider(height: 1),
                   ListTile(
                     leading: Icon(
                       Icons.star_outline_rounded,
                       color: colorScheme.onSurfaceVariant,
                     ),
                     title: const Text('Rate Us'),
                     trailing: const Icon(Icons.chevron_right_rounded),
                     onTap: widget.onRateUsTap,
                   ),
                   const Divider(height: 1),
                   ListTile(
                     leading: Icon(
                       Icons.info_outline_rounded,
                       color: colorScheme.onSurfaceVariant,
                     ),
                     title: const Text('About'),
                     trailing: const Icon(Icons.chevron_right_rounded),
                     onTap: () {
                       Navigator.of(context).push(
                         MaterialPageRoute(builder: (_) => const AboutPage()),
                       );
                     },
                   ),
                 ],
               ),
             ),
            const SizedBox(height: 24),
            Text(
              'Version ${_version.isEmpty ? '' : _version}',
              style: theme.textTheme.bodySmall?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
