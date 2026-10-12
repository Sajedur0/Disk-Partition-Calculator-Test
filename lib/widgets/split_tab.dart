import 'package:flutter/material.dart';
import '../app_theme.dart';
import '../models/partition_models.dart';
import '../utils/split_partition_calculator.dart';
import '../managers/history_manager.dart';
import 'shared_controls.dart';
import 'result_dialog.dart';

/// Result of splitting a single partition in the [SplitTab] flow.
class _SplitPartitionResult {
  final int index;
  final double displayMb; // Size to show in UI (includes overhead)
  final int
  createMb; // Internal size to send to diskpart, rounded to nearest MB.

  _SplitPartitionResult(this.index, this.displayMb, this.createMb);
}

/// Tab that splits a total disk capacity into a number of equal partitions
/// and reports the exact MB input required for each — useful when you want
/// to lay out several partitions in the Windows disk-setup dialog.
class SplitTab extends StatefulWidget {
  const SplitTab({super.key, required this.historyManager});

  final HistoryManager historyManager;

  @override
  State<SplitTab> createState() => _SplitTabState();
}

class _SplitTabState extends State<SplitTab> {
  final TextEditingController _totalSizeController = TextEditingController();
  FileSystemType _filesystem = FileSystemType.ntfs;
  int _partitionCount = 2;
  bool _isCalculating = false;

  @override
  void dispose() {
    _totalSizeController.dispose();
    super.dispose();
  }

  void _increment() {
    if (_partitionCount < 8) setState(() => _partitionCount++);
  }

  void _decrement() {
    if (_partitionCount > 1) setState(() => _partitionCount--);
  }

  void _showToast(String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  String _formatMb(double mb) {
    return mb.toStringAsFixed(2).replaceAll(RegExp(r'\.00$'), '');
  }

  void _calculate() {
    final text = _totalSizeController.text.trim();
    if (text.isEmpty) {
      _showToast('Enter a total disk size in MB');
      return;
    }
    final totalSizeMb = double.tryParse(text);
    if (totalSizeMb == null || totalSizeMb <= 0) {
      _showToast('Enter a valid positive MB value');
      return;
    }

    setState(() => _isCalculating = true);

    try {
      final plan = SplitPartitionCalculator.splitPlan(
        totalSizeMB: totalSizeMb,
        partitionCount: _partitionCount,
      );

      // Diskpart accepts whole MB values, so round only at the creation boundary.
      final List<_SplitPartitionResult> results = [];
      for (int i = 0; i < _partitionCount; i++) {
        final double displayMb = plan[i].displayMB;
        final int createMb = plan[i].createMB
            .round(); // round to nearest integer
        results.add(_SplitPartitionResult(i + 1, displayMb, createMb));
      }

      setState(() {
        _isCalculating = false;
      });

      // Build explanation for FAT32 warnings
      String explanation = '';
      final double partitionSizeGb = results.first.displayMb / 1024.0;
      if (_filesystem == FileSystemType.fat32) {
        if (partitionSizeGb > 32) {
          explanation =
              'Warning: Partition size exceeds 32 GB limit for FAT32.\nNote: FAT32 has a 4 GB maximum file size limit.';
        } else {
          explanation = 'Note: FAT32 has a 4 GB maximum file size limit.';
        }
      }

      // Save to history
      final entry = ConversionHistory(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        inputValue: totalSizeMb,
        fromUnit: 'Total MB',
        toUnit: 'Per Partition MB',
        filesystem: _filesystem,
        requiredInputMb: results.first.displayMb.round(),
        result:
            '${results.length} partitions × ${_formatMb(results.first.displayMb)} MB each',
        timestamp: DateTime.now(),
        splitCount: results.length,
        splitPartitionDisplayMb: results.first.displayMb,
      );
      widget.historyManager.addEntry(entry);

      if (!mounted) return;

      final partitionRows = results.map((result) {
        return buildHighlightedResultRow(
          context,
          'Partition ${result.index}:',
          '${_formatMb(result.displayMb)} MB',
        );
      }).toList();

      showResultDialog(
        context,
        'Split Partitions Result',
        partitionRows,
        explanation,
        requiredInputMb: null,
      );
    } on ArgumentError catch (e) {
      setState(() {
        _isCalculating = false;
      });
      _showToast(e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return SafeArea(
      child: SingleChildScrollView(
        physics: const BouncingScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SectionHeader('Total Disk Size'),
            SectionCard(
              child: TextField(
                controller: _totalSizeController,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                style: Theme.of(
                  context,
                ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700),
                decoration: InputDecoration(
                  prefixIcon: Icon(
                    Icons.disc_full_rounded,
                    color: colorScheme.primary,
                  ),
                  labelText: 'Total Size (MB)',
                  hintText: 'e.g. 476923',
                  helperText: 'Total disk capacity to split into partitions.',
                ),
              ),
            ),
            const SizedBox(height: 20),

            const SectionHeader('Number of Partitions'),
            SectionCard(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  IconButton(
                    icon: const Icon(Icons.remove_rounded),
                    onPressed: _partitionCount > 1 ? _decrement : null,
                  ),
                  const SizedBox(width: 12),
                  Text(
                    '$_partitionCount',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    'partition${_partitionCount > 1 ? 's' : ''}',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(width: 12),
                  IconButton(
                    icon: const Icon(Icons.add_rounded),
                    onPressed: _partitionCount < 8 ? _increment : null,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            const SectionHeader('Filesystem Type'),
            SectionCard(
              child: FilesystemToggle(
                selectedValue: _filesystem,
                onChanged: (val) => setState(() => _filesystem = val),
              ),
            ),
            const SizedBox(height: 32),

            _buildCalculateButton(),
            const SizedBox(height: 24),

            if (_isCalculating)
              const Center(child: CircularProgressIndicator()),
          ],
        ),
      ),
    );
  }

  Widget _buildCalculateButton() {
    final colorScheme = Theme.of(context).colorScheme;
    return FilledButton.icon(
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
          : const Icon(Icons.call_split_rounded, size: 20),
      label: Text(
        _isCalculating ? 'Splitting…' : 'Split Partitions',
        style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
      ),
      style: FilledButton.styleFrom(
        backgroundColor: colorScheme.primary,
        foregroundColor: colorScheme.onPrimary,
        minimumSize: const Size(double.infinity, 56),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.md),
        ),
        elevation: 0,
      ),
    );
  }
}
