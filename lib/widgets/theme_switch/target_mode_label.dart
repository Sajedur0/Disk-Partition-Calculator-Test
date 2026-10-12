import 'package:flutter/material.dart';

/// Shows the icon + label of the mode the app will switch TO —
/// i.e. shows "Dark Mode" while app is currently in Light Mode, and
/// "Light Mode" while app is currently in Dark Mode. This is NOT a
/// toggle/switch — it never reflects current state, only the target.
class TargetModeLabel extends StatelessWidget {
  final bool isDarkMode; // current app mode
  final bool showText;
  final double iconSize;

  const TargetModeLabel({
    super.key,
    required this.isDarkMode,
    this.showText = true,
    this.iconSize = 22,
  });

  @override
  Widget build(BuildContext context) {
    // Target = the OPPOSITE of current mode.
    final goingToDark = !isDarkMode;
    final icon = goingToDark
        ? Icons.dark_mode_outlined
        : Icons.light_mode_outlined;
    final label = goingToDark ? 'Dark Mode' : 'Light Mode';

    if (!showText) return Icon(icon, size: iconSize);

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: iconSize),
        const SizedBox(width: 8),
        Text(label),
      ],
    );
  }
}
