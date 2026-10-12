import 'package:flutter/material.dart';
import '../../core/theme_reveal/theme_reveal_controller.dart';
import 'target_mode_label.dart';

class ThemeSwitchDrawerTile extends StatelessWidget {
  final bool isDarkMode;
  final VoidCallback onThemeChanged;

  const ThemeSwitchDrawerTile({
    super.key,
    required this.isDarkMode,
    required this.onThemeChanged,
  });

  @override
  Widget build(BuildContext context) {
    // Target = the OPPOSITE of the current mode (we are a shortcut to the
    // other mode, never a toggle reflecting the current state).
    final goingToDark = !isDarkMode;
    final label = goingToDark ? 'Switch to Dark Mode' : 'Switch to Light Mode';

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTapDown: (details) {
        ThemeRevealController.of(
          context,
        ).revealFrom(details.globalPosition, onThemeChanged);
      },
      child: ListTile(
        leading: TargetModeLabel(
          isDarkMode: isDarkMode,
          showText: false,
          iconSize: 24,
        ),
        title: Text(label),
        // No Switch()/Checkbox() — purely a tappable action row.
      ),
    );
  }
}
