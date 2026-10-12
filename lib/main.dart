import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'src/entry_point.dart';
import 'core/theme_reveal/theme_reveal_controller.dart';

const String _themeModePrefsKey = 'theme_mode';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  SharedPreferences prefs;
  try {
    prefs = await SharedPreferences.getInstance();
  } catch (e) {
    await SharedPreferences.getInstance().then((p) => p.clear());
    prefs = await SharedPreferences.getInstance();
  }

  final ThemeMode initialMode = _loadThemeMode(prefs);

  runApp(
    ThemeRevealControllerApp(initialThemeMode: initialMode),
  );
}

ThemeMode _loadThemeMode(SharedPreferences prefs) {
  try {
    final savedMode = prefs.getInt(_themeModePrefsKey);
    if (savedMode != null &&
        savedMode >= 0 &&
        savedMode < ThemeMode.values.length) {
      return ThemeMode.values[savedMode];
    }
  } catch (_) {
    prefs.remove(_themeModePrefsKey);
  }
  return ThemeMode.light;
}

class ThemeRevealControllerApp extends StatefulWidget {
  final ThemeMode initialThemeMode;

  const ThemeRevealControllerApp({
    super.key,
    required this.initialThemeMode,
  });

  @override
  State<ThemeRevealControllerApp> createState() =>
      _ThemeRevealControllerAppState();
}

class _ThemeRevealControllerAppState extends State<ThemeRevealControllerApp> {
  late ThemeMode _themeMode;

  @override
  void initState() {
    super.initState();
    _themeMode = widget.initialThemeMode;
  }

  Future<void> _toggleTheme() async {
    setState(() {
      _themeMode = _themeMode == ThemeMode.dark
          ? ThemeMode.light
          : ThemeMode.dark;
    });
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_themeModePrefsKey, _themeMode.index);
  }

  @override
  Widget build(BuildContext context) {
    return ThemeRevealController(
      builder: (context) {
        return DiskPartitionCalculator(
          themeMode: _themeMode,
          onToggleTheme: _toggleTheme,
        );
      },
    );
  }
}
