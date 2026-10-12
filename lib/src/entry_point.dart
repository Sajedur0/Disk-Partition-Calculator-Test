import 'package:flutter/material.dart';
import '../app_theme.dart';
import '../pages/calculator_page.dart';
import '../services/update_service.dart';

class DiskPartitionCalculator extends StatefulWidget {
  final ThemeMode themeMode;
  final VoidCallback onToggleTheme;

  const DiskPartitionCalculator({
    super.key,
    required this.themeMode,
    required this.onToggleTheme,
  });

  @override
  State<DiskPartitionCalculator> createState() =>
      _DiskPartitionCalculatorState();
}

class _DiskPartitionCalculatorState extends State<DiskPartitionCalculator>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkForUpdates();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    UpdateService.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkForUpdates(isResume: true);
    }
  }

  void _checkForUpdates({bool isResume = false}) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Future.delayed(Duration(seconds: isResume ? 0 : 2), () {
        if (mounted) {
          UpdateService.checkForUpdate(context, isResume: isResume);
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Disk Partition Calculator',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      themeMode: widget.themeMode,
      home: CalculatorPage(onToggleTheme: widget.onToggleTheme),
    );
  }
}
