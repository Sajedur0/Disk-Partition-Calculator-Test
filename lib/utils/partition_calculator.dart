import '../models/partition_models.dart';

class PartitionCalculator {
  static const int hiddenWindowsSetupOverheadMb = 566;
  static const int mbPerGb = 1024; // base conversion
  static const int overheadMbPerPartition = 4; // fixed 4 MB per partition

  static int visibleGbToPartitionMb(
    double visibleGb,
    FileSystemType filesystem,
  ) {
    // The conversion is the same for both filesystems.
    return (visibleGb * mbPerGb).round() + overheadMbPerPartition;
  }

  static int requiredInputMb(
    double visibleGb,
    FileSystemType filesystem, {
    bool simulateWindowsSetup = false,
  }) {
    final int baseMb = visibleGbToPartitionMb(visibleGb, filesystem);
    return simulateWindowsSetup
        ? baseMb + hiddenWindowsSetupOverheadMb
        : baseMb;
  }

  static double partitionMbToDisplayedGb(
    int partitionMb,
    FileSystemType filesystem,
  ) {
    // Subtract the per-partition overhead, then convert MiB to GB
    return (partitionMb - overheadMbPerPartition) / mbPerGb;
  }

  static PartitionCalculationResult calculate(
    double visibleGb,
    FileSystemType filesystem, {
    bool simulateWindowsSetup = false,
  }) {
    final int partitionSizeMb = visibleGbToPartitionMb(visibleGb, filesystem);
    final int requiredInput = simulateWindowsSetup
        ? partitionSizeMb + hiddenWindowsSetupOverheadMb
        : partitionSizeMb;

    return PartitionCalculationResult(
      partitionSizeMb: partitionSizeMb,
      requiredInputMb: requiredInput,
      displayedGb: partitionMbToDisplayedGb(partitionSizeMb, filesystem),
      usesWindowsSetupOverhead: simulateWindowsSetup,
      filesystem: filesystem,
    );
  }

  /// Returns true if the given visible size in GB is valid for FAT32 (<= 32 GB).
  static bool isFat32SizeValid(double visibleGb) {
    return visibleGb <= 32;
  }
}