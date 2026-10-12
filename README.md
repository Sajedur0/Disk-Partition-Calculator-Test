<div align="center">
  <a href="#readme" style="text-decoration:none; margin:0 10px; padding:8px 16px; background:#02569B; color:white; border-radius:4px; font-weight:bold;">📖 Readme</a>
  <a href="./CHANGELOG.md" style="text-decoration:none; margin:0 10px; padding:8px 16px; color:#02569B; border-radius:4px;">📜 Changelog</a>
  <a href="LICENSE" style="text-decoration:none; margin:0 10px; padding:8px 16px; color:#02569B; border-radius:4px;">🚀 License</a>
  <a href="#contributing" style="text-decoration:none; margin:0 10px; padding:8px 16px; color:#02569B; border-radius:4px;">🤝 Contributing</a>
</div>

<div align="center">

  <img src="assets/Icon.png" alt="Disk Partition Calculator Icon" width="120" />

  # Disk Partition Calculator

  **Version 1.1.3**

</div>

[![Flutter](https://img.shields.io/badge/Flutter-3.11+-02569B?logo=flutter)](https://flutter.dev)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.1.3-blue.svg)](CHANGELOG.md)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Stars](https://img.shields.io/github/stars/Sajedur0/Disk-Partition-Calculator?style=social)](https://github.com/Sajedur0/Disk-Partition-Calculator)

A Flutter app to calculate exact partition sizes for Windows. Enter the visible GB you want after format, get the required MB to enter in the disk setup dialog. Supports NTFS and FAT32 with optional Windows Setup overhead simulation.

<details>
<summary>## Features</summary>

- **NTFS & FAT32** — Select filesystem type; each uses its own GB→MB conversion ratio
- **Windows Setup Simulation** — Adds the 566 MB System Reserved overhead for realistic partitioning
- **Quick Presets** — Tap 20 GB, 50 GB, 100 GB, 250 GB, 500 GB, or 1 TB for instant calculation
- **History Panel** — Every calculation is saved locally; browse or clear past results (capped at 100 entries)
- **Light & Dark Mode** — Full Material 3 theme with persistent preference and instant switching
- **In-App Updates** — Google Play native UI; flexible by default, mandatory after 5 days
- **Optimized Performance** — R8 minification, cached shadows, reduced memory footprint for smooth experience on all devices
- **Privacy Policy & About** — Built-in pages for transparency and app info

</details>

<details>
<summary>## Screenshots</summary>

| Light | Dark |
|-------|------|
| *Screenshot coming soon* | *Screenshot coming soon* |

</details>

<details>
<summary>## Tech Stack</summary>

| Layer | Choice |
|-------|--------|
| Framework | Flutter (Material 3) |
| State | `StatefulWidget` + `setState` |
| Persistence | `shared_preferences` |
| Updates | `in_app_update_android` 1.1.7 (Play Core) |
| Sharing | `share_plus` |
| Launch | `url_launcher` |
| App Info | `package_info_plus` |

</details>

<details>
<summary>## Getting Started</summary>

```bash
# Clone the repo
git clone https://github.com/Sajedur0/Disk-Partition-Calculator.git
cd Disk-Partition-Calculator

# Install dependencies
flutter pub get

# Run on a connected device or emulator
flutter run
```

### Build APK

```bash
flutter build apk --release
```

### Build App Bundle (Play Store)

```bash
flutter build appbundle --release
```

</details>

<details>
<summary>## Project Structure</summary>

```
lib/
├── main.dart                       # App bootstrap
├── app_theme.dart                  # Light/dark ThemeData definitions
├── constants/
│   └── app_constants.dart          # App-wide constants
├── core/
│   └── theme_reveal/
│       ├── theme_reveal_controller.dart  # Circular reveal animation
│       └── circular_reveal_clipper.dart  # Custom clipper
├── managers/
│   └── history_manager.dart        # History persistence logic
├── models/
│   └── partition_models.dart       # Data models
├── pages/
│   └── calculator_page.dart        # Main calculator screen
├── services/
│   └── update_service.dart         # In-app update orchestration
├── src/
│   └── entry_point.dart            # Theme state, lifecycle, MaterialApp
├── utils/
│   └── partition_calculator.dart   # Partition math logic
└── widgets/
    ├── about_page.dart             # App info page
    ├── app_drawer.dart             # Navigation drawer
    ├── history_panel.dart          # Calculation history bottom sheet
    ├── privacy_policy_page.dart    # Privacy policy page
    ├── quit_dialog.dart            # Exit confirmation dialog
    ├── result_dialog.dart          # Calculation result dialog
    └── theme_switch/
        ├── target_mode_label.dart  # Theme icon/label
        └── theme_switch_drawer_tile.dart  # Theme toggle tile
```

</details>

<details>
<summary>## Version History</summary>

See [CHANGELOG.md](CHANGELOG.md) for full version history.

</details>

<details>
<summary>## License</summary>

```
MIT License

Copyright (c) 2024-2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

</details>