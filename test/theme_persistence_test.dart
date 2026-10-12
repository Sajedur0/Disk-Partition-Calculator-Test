import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

ThemeMode loadThemeMode(SharedPreferences prefs) {
  final savedMode = prefs.getInt('theme_mode');
  return (savedMode != null &&
          savedMode >= 0 &&
          savedMode < ThemeMode.values.length)
      ? ThemeMode.values[savedMode]
      : ThemeMode.light;
}

void saveThemeMode(SharedPreferences prefs, ThemeMode mode) {
  prefs.setInt('theme_mode', mode.index);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('dark theme survives a save/load round-trip', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();

    saveThemeMode(prefs, ThemeMode.dark);
    expect(loadThemeMode(prefs), ThemeMode.dark);
  });

  test('light theme survives a save/load round-trip', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();

    saveThemeMode(prefs, ThemeMode.light);
    expect(loadThemeMode(prefs), ThemeMode.light);
  });

  test('missing preference defaults to light', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();

    expect(loadThemeMode(prefs), ThemeMode.light);
  });
}
