# Changelog

All notable changes to Disk Partition Calculator are documented here.

## [2.0.0] — Kotlin + Jetpack Compose rewrite

- **Complete rewrite of the UI layer with Jetpack Compose (Material 3)** — the Flutter/Dart
  implementation was replaced by a native Kotlin app; all calculation logic, constants, copy and
  stored data formats are unchanged (see `MIGRATION.md`)
- New Material 3 design language: brand blue palette with slate surfaces, refreshed typography
  scale, one radius/em sizing system
- Bottom navigation bar with three destinations (Calc, Split, Settings) replacing the top tab bar
- Hand-drawn Material Symbols icons compiled into vector drawables (no icon font at runtime)
- Result dialog redesigned around the "value to enter in Windows disk setup" with an animated
  counter, copy action and clearer detail rows
- History sheet redesigned with tonal cards, the same data and the same empty state
- Settings redesigned with tonal icon rows; theme switching now animates as a circular reveal that
  grows from the tapped point
- Splash/launcher resources kept from the previous release; the app still starts in light mode and
  follows the saved preference
- In-app updates ported to the Google Play `app-update` library (flexible by default, immediate
  after 5 stale days, restart after install)
- Architecture: single module, MVVM with `ViewModel` + `StateFlow`, `SharedPreferences` behind a
  testable storage seam, and a dependency-free JSON codec
- Unit test suite rewritten in JUnit (72 tests) covering the calculator math, validation,
  persistence, migration compatibility and every result/history string
- Project tooling: Gradle 9.6 + AGP 9.2.1 + Kotlin 2.4.0, R8 minification/shrinking for release
  builds, `tools/generate_icons.py` to regenerate the icon set, `.editorconfig`

## [1.1.3]
- Flexible updates no longer cause the app to restart mid-download when resuming
- Update status now accurately shows completion, cancellation, or errors
- Improved error reporting for failed updates
- Split tab input now correctly uses MB units
- Split partition sizes now include required system overhead for accuracy
- Calculated sizes now match Windows Explorer display (within rounding)

## [1.1.2]
- Fixed black screen after app update when data wasn't cleared
- App now handles corrupted settings gracefully by resetting them
- Faster app restart after updates (now 2 seconds)
- Improved theme loading to prevent startup crashes

## [1.1.1]
- Calculations are now instant and responsive
- Calculation history automatically limited to 100 entries
- Reduced memory usage on high-DPI screens
- Improved reliability of update restarts
- Increased update installation delay for safer completion
- Added timeout to prevent getting stuck during updates

## [1.1.0]
- Fixed app launch issues: black screen, freezing, and lag
- Added custom splash screen for light and dark modes
- Update check no longer delays app startup
- Update check moved after initial app load for smoother startup
- Improved theme display on high-DPI screens
- Fixed app restart issues after updates

## [1.0.9]
- App version now displayed accurately from your device
- Sidebar header updated with new gradient design
- App icon shown in a rounded glass card in the sidebar
- Improved app name typography and contrast in sidebar
- Updates now use Google Play's native interface
- Flexible updates are now default; immediate updates offered after 5 days

## [1.0.8]
- Added immediate and flexible app update options
- Light and dark themes with Material 3 styling applied throughout the app
- App restarts after installing updates
- Update progress shown with retry and completion options
- Automatic update check when resuming the app
- Improved theme persistence and streamlined app startup

## [1.0.7]
- Added Privacy Policy page to navigation menu
- Added About page with app version and developer info
- Added option to rate the app on Play Store
- Added share app feature
- Status bar now adapts to light and dark themes
- Enhanced navigation menu with more options and improved performance

## [1.0.6]
- Added Windows Setup simulation option (includes 566 MB system overhead)
- Visual simulation card with animated feedback
- Overhead breakdown shown in calculation results
- Toggle to include Windows Setup overhead in calculations
- Result dialog shows detailed overhead when simulation enabled

## [1.0.5]
- Updated to Material 3 design language
- New color scheme with modern container and surface colors
- Smooth animated theme switching
- Floating snack bar style
- Updated component styles for Material 3 compatibility

## [1.0.4]
- Added in-app update support
- Automatic update check on app startup
- Flexible update flow with download completion handling

## [1.0.3]
- Added dark mode with toggle in navigation menu
- Automatically follows device theme settings
- Smooth theme transition animation
- Calculator and drawer now respond to theme changes

## [1.0.2]
- Added calculation history panel
- Browse past calculations from a bottom sheet
- Option to clear history
- History saved automatically
- Calculation results now include detailed data
- History supports saving and loading

## [1.0.1]
- Switch between NTFS and FAT32 filesystems
- Accurate conversion ratios for NTFS and FAT32
- Quick select preset sizes (20 GB to 1024 GB)
- Result dialog shows partition size details
- Input validation for invalid values
- Clear error messages for empty or incorrect input

## [1.0.0]
- Initial release with basic partition size calculator
- NTFS filesystem support
- Material Design interface
- Gradient background UI
