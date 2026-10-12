enum FileSystemType { ntfs, fat32 }

class PartitionCalculationResult {
  final int partitionSizeMb;
  final int requiredInputMb;
  final double displayedGb;
  final bool usesWindowsSetupOverhead;
  final FileSystemType filesystem;

  PartitionCalculationResult({
    required this.partitionSizeMb,
    required this.requiredInputMb,
    required this.displayedGb,
    required this.usesWindowsSetupOverhead,
    required this.filesystem,
  });
}

extension DoubleFormatter on double {
  String toFormattedString() {
    return toStringAsFixed(2).replaceAll(RegExp(r'\.00$'), '');
  }
}

class ConversionHistory {
  final String id;
  final double inputValue;
  final String fromUnit;
  final String toUnit;
  final FileSystemType filesystem;
  final int requiredInputMb;
  final String result;
  final DateTime timestamp;
  final int? splitCount;
  final double? splitPartitionGb;
  final int? splitPartitionMb;
  final double? splitPartitionDisplayMb;

  ConversionHistory({
    required this.id,
    required this.inputValue,
    required this.fromUnit,
    required this.toUnit,
    required this.filesystem,
    required this.requiredInputMb,
    required this.result,
    required this.timestamp,
    this.splitCount,
    this.splitPartitionGb,
    this.splitPartitionMb,
    this.splitPartitionDisplayMb,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'inputValue': inputValue,
      'fromUnit': fromUnit,
      'toUnit': toUnit,
      'filesystem': filesystem.name,
      'requiredInputMb': requiredInputMb,
      'result': result,
      'timestamp': timestamp.toIso8601String(),
      'splitCount': splitCount,
      'splitPartitionGb': splitPartitionGb,
      'splitPartitionMb': splitPartitionMb,
      'splitPartitionDisplayMb': splitPartitionDisplayMb,
    };
  }

  factory ConversionHistory.fromMap(Map<String, dynamic> map) {
    return ConversionHistory(
      id: map['id'],
      inputValue: map['inputValue'],
      fromUnit: map['fromUnit'],
      toUnit: map['toUnit'],
      filesystem: FileSystemType.values.firstWhere(
        (type) => type.name == map['filesystem'],
        orElse: () => FileSystemType.ntfs,
      ),
      requiredInputMb: map['requiredInputMb'] ?? 0,
      result: map['result'] ?? '',
      timestamp: DateTime.parse(map['timestamp']),
      splitCount: map['splitCount'],
      splitPartitionGb: map['splitPartitionGb'],
      splitPartitionMb: map['splitPartitionMb'],
      splitPartitionDisplayMb: map['splitPartitionDisplayMb'],
    );
  }
}
