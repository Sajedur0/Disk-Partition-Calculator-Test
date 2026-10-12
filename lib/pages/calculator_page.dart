import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:share_plus/share_plus.dart';
import 'package:package_info_plus/package_info_plus.dart';
import '../models/partition_models.dart';
import '../utils/partition_calculator.dart';
import '../managers/history_manager.dart';
import '../widgets/history_panel.dart';
import '../widgets/privacy_policy_page.dart';
import '../widgets/result_dialog.dart';
import '../widgets/quit_dialog.dart';
import '../widgets/shared_controls.dart';
import '../widgets/split_tab.dart';
import '../widgets/settings_tab.dart';
import '../app_theme.dart';

const String _defaultFilesystemKey = 'settings_default_filesystem';
const String _defaultSimulationKey = 'settings_default_simulation';

class CalculatorPage extends StatefulWidget {
  final VoidCallback onToggleTheme;

  const CalculatorPage({super.key, required this.onToggleTheme});

  @override
  State<CalculatorPage> createState() => _CalculatorPageState();
}

class _CalculatorPageState extends State<CalculatorPage> {
  final TextEditingController _desiredSizeGbController =
      TextEditingController();
  late HistoryManager _historyManager;
  bool _historyLoaded = false;
  FileSystemType _selectedFilesystem = FileSystemType.ntfs;
  bool _simulateWindowsSetup = false;
  double _currentGbValue = 0.0;
  bool _isCalculating = false;

  @override
  void initState() {
    super.initState();
    _initHistoryManager();
    _desiredSizeGbController.addListener(_onSizeChanged);
  }

  void _onSizeChanged() {
    final double? parsed = double.tryParse(
      _desiredSizeGbController.text.trim(),
    );
    final double newVal = (parsed == null || parsed < 0) ? 0.0 : parsed;
    if (newVal != _currentGbValue) {
      setState(() {
        _currentGbValue = newVal;
      });
    }
  }

  Future<void> _initHistoryManager() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _historyManager = HistoryManager(prefs);
      _selectedFilesystem = _loadFilesystem(prefs);
      _simulateWindowsSetup = prefs.getBool(_defaultSimulationKey) ?? false;
      _historyLoaded = true;
    });
  }

  FileSystemType _loadFilesystem(SharedPreferences prefs) {
    final index = prefs.getInt(_defaultFilesystemKey);
    if (index != null && index >= 0 && index < FileSystemType.values.length) {
      return FileSystemType.values[index];
    }
    return FileSystemType.ntfs;
  }

  Future<void> _setFilesystem(FileSystemType fs) async {
    setState(() => _selectedFilesystem = fs);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_defaultFilesystemKey, fs.index);
  }

  Future<void> _setSimulateWindowsSetup(bool value) async {
    setState(() => _simulateWindowsSetup = value);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_defaultSimulationKey, value);
  }

  @override
  void dispose() {
    _desiredSizeGbController.removeListener(_onSizeChanged);
    _desiredSizeGbController.dispose();
    super.dispose();
  }

  Widget _sectionHeader(String text) {
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

  Widget _sectionCard({required Widget child}) {
    final theme = Theme.of(context);
    return AnimatedContainer(
      duration: AppMotion.base,
      curve: AppMotion.curve,
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: appCardBorder(theme.colorScheme),
        boxShadow: AppShadows.card(theme.brightness),
      ),
      child: child,
    );
  }

  Widget _buildCalculateButton() {
    final colorScheme = Theme.of(context).colorScheme;
    return AnimatedScale(
      duration: AppMotion.fast,
      curve: AppMotion.curve,
      scale: _isCalculating ? 0.99 : 1.0,
      child: FilledButton.icon(
        onPressed: _isCalculating ? null : _calculate,
        icon: _isCalculating
            ? SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(
                  strokeWidth: 2.5,
                  color: colorScheme.onPrimary,
                ),
              )
            : const Icon(Icons.calculate_rounded, size: 20),
        label: Text(
          _isCalculating ? 'Calculating…' : 'Calculate Exact MB',
          style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
        ),
        style: FilledButton.styleFrom(
          backgroundColor: colorScheme.primary,
          foregroundColor: colorScheme.onPrimary,
          disabledBackgroundColor: colorScheme.primary.withValues(alpha: 0.6),
          disabledForegroundColor: colorScheme.onPrimary,
          minimumSize: const Size(double.infinity, 56),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppRadius.md),
          ),
          elevation: 0,
        ),
      ),
    );
  }

  void _calculate() {
    final String gbText = _desiredSizeGbController.text.trim();

    if (gbText.isEmpty) {
      _showToast('Enter a visible partition size in GB');
      return;
    }

    final double? visibleGb = double.tryParse(gbText);
    if (visibleGb == null || visibleGb <= 0) {
      _showToast('Enter a valid positive GB value');
      return;
    }

    setState(() {
      _isCalculating = true;
    });

    final PartitionCalculationResult result = PartitionCalculator.calculate(
      visibleGb,
      _selectedFilesystem,
      simulateWindowsSetup: _simulateWindowsSetup,
    );

    // Diagnostic logging for partition size accuracy
    final int mibSent = result.partitionSizeMb;
    final int bytesSent = mibSent * 1024 * 1024;
    final double displayedGbFromBytes = (mibSent - 4) / 1024.0;
    debugPrint('Partition size diagnostic:');
    debugPrint('  requested_gb: $visibleGb');
    debugPrint('  mib_sent_to_diskpart: $mibSent');
    debugPrint('  actual_bytes_reported (simulated): $bytesSent');
    debugPrint('  explorer_displayed_gb (simulated): $displayedGbFromBytes');

    final String filesystemLabel = _selectedFilesystem == FileSystemType.fat32
        ? 'FAT32'
        : 'NTFS';

    final List<Widget> resultRows = buildResultRows(
      context,
      visibleGb,
      filesystemLabel,
      result.requiredInputMb,
      result.usesWindowsSetupOverhead,
    );

    String explanation = getExplanation(result.usesWindowsSetupOverhead);
    if (_selectedFilesystem == FileSystemType.fat32) {
      if (visibleGb > 32) {
        explanation = explanation.isNotEmpty
            ? '$explanation\nWarning: Partition size exceeds 32 GB limit for FAT32.'
            : 'Warning: Partition size exceeds 32 GB limit for FAT32.';
      }
      explanation = explanation.isNotEmpty
          ? '$explanation\nNote: FAT32 has a 4 GB maximum file size limit.'
          : 'Note: FAT32 has a 4 GB maximum file size limit.';
    }

    final entry = ConversionHistory(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      inputValue: visibleGb,
      fromUnit: 'Visible GB',
      toUnit: 'Required MB',
      filesystem: result.filesystem,
      requiredInputMb: result.requiredInputMb,
      result: '$filesystemLabel - ${result.requiredInputMb} MB',
      timestamp: DateTime.now(),
    );

    _historyManager.addEntry(entry);

    setState(() {
      _isCalculating = false;
    });

    if (!mounted) return;

    showResultDialog(
      context,
      'Calculation Result',
      resultRows,
      explanation,
      requiredInputMb: result.requiredInputMb,
    );
  }

  void _showToast(String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  void _showHistoryPanel() {
    if (!_historyLoaded) return;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) =>
          HistoryPanel(historyManager: _historyManager, onClearHistory: () {}),
    );
  }

  void _showPrivacyPolicy() {
    Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (context) => const PrivacyPolicyPage()));
  }

  Future<void> _shareApp() async {
    const playStoreUrl =
        'https://play.google.com/store/apps/details?id=com.diskcalc';
    final info = await PackageInfo.fromPlatform();
    final text =
        '''
Disk Partition Calculator

Calculate exact and recommended partition sizes for Windows.

Download now: $playStoreUrl

Version: ${info.version} (${info.buildNumber})
''';
    await SharePlus.instance.share(ShareParams(text: text.trim()));
  }

  Future<void> _rateUs() async {
    const packageName = 'com.diskcalc';
    final uri = Uri.parse('market://details?id=$packageName');
    final fallbackUri = Uri.parse(
      'https://play.google.com/store/apps/details?id=$packageName',
    );
    try {
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri);
      } else if (await canLaunchUrl(fallbackUri)) {
        await launchUrl(fallbackUri);
      } else {
        _showToast('Could not open the store');
      }
    } catch (_) {
      if (await canLaunchUrl(fallbackUri)) {
        await launchUrl(fallbackUri);
      } else {
        _showToast('Could not open the store');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_historyLoaded) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text(
            'Disk Partition Calculator',
            overflow: TextOverflow.ellipsis,
          ),
          actions: [
            Builder(
              builder: (context) {
                final tabController = DefaultTabController.of(context);
                return AnimatedBuilder(
                  animation: tabController,
                  builder: (context, child) {
                    if (tabController.index > 1) {
                      return const SizedBox.shrink();
                    }
                    return IconButton(
                      icon: const Icon(Icons.history_rounded),
                      onPressed: _showHistoryPanel,
                      tooltip: 'View History',
                    );
                  },
                );
              },
            ),
            const SizedBox(width: 4),
          ],
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Calc'),
              Tab(text: 'Split'),
              Tab(text: 'Settings'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _buildCalcTabContent(),
            SplitTab(historyManager: _historyManager),
            SettingsTab(
              onToggleTheme: widget.onToggleTheme,
              onPrivacyPolicyTap: _showPrivacyPolicy,
              onRateUsTap: _rateUs,
              onShareTap: _shareApp,
              onQuitTap: () => showQuitDialog(context),
              historyManager: _historyManager,
              selectedFilesystem: _selectedFilesystem,
              onFilesystemChanged: _setFilesystem,
              simulateWindowsSetup: _simulateWindowsSetup,
              onSimulateWindowsSetupChanged: _setSimulateWindowsSetup,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCalcTabContent() {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return SafeArea(
      child: SingleChildScrollView(
        physics: const BouncingScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _sectionHeader('Quick Presets'),
            _sectionCard(
              child: SizePresetsRow(
                currentValue: _currentGbValue,
                onPresetSelected: (val) {
                  setState(() {
                    _desiredSizeGbController.text = val.toFormattedString();
                    _currentGbValue = val;
                  });
                },
              ),
            ),
            const SizedBox(height: 20),

            _sectionHeader('Desired Partition Size'),
            _sectionCard(
              child: TextField(
                controller: _desiredSizeGbController,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
                decoration: InputDecoration(
                  prefixIcon: Icon(
                    Icons.storage_rounded,
                    color: colorScheme.primary,
                  ),
                  labelText: 'Size (GB)',
                  hintText: 'e.g. 100',
                  helperText: 'The capacity Windows displays after format.',
                ),
              ),
            ),
            const SizedBox(height: 20),

            _sectionHeader('Filesystem Type'),
            _sectionCard(
              child: FilesystemToggle(
                selectedValue: _selectedFilesystem,
                onChanged: _setFilesystem,
              ),
            ),
            const SizedBox(height: 20),

            _sectionCard(
              child: SimulationCard(
                value: _simulateWindowsSetup,
                onChanged: _setSimulateWindowsSetup,
              ),
            ),
            const SizedBox(height: 32),

            _buildCalculateButton(),
            const SizedBox(height: 10),
          ],
        ),
      ),
    );
  }
}

class SizePresetsRow extends StatelessWidget {
  final ValueChanged<double> onPresetSelected;
  final double? currentValue;

  const SizePresetsRow({
    super.key,
    required this.onPresetSelected,
    this.currentValue,
  });

  static const List<double> presets = [20, 50, 100, 250, 500, 1024];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return SizedBox(
      height: 44,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: presets.length,
        itemBuilder: (context, index) {
          final preset = presets[index];
          final String label = preset >= 1024
              ? '${(preset / 1024).round()} TB'
              : '${preset.round()} GB';
          final isSelected = currentValue == preset;

          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: ChoiceChip(
              label: Text(label),
              selected: isSelected,
              onSelected: (selected) {
                if (selected) {
                  onPresetSelected(preset);
                }
              },
              showCheckmark: false,
              selectedColor: colorScheme.primary,
              backgroundColor: colorScheme.surfaceContainer,
              labelStyle: theme.textTheme.bodyMedium?.copyWith(
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected
                    ? colorScheme.onPrimary
                    : colorScheme.onSurfaceVariant,
              ),
              side: BorderSide.none,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppRadius.sm),
              ),
            ),
          );
        },
      ),
    );
  }
}
