import 'package:flutter_test/flutter_test.dart';
import 'package:disk_partition_calculator/utils/partition_calculator.dart';
import 'package:disk_partition_calculator/models/partition_models.dart';

void main() {
  const double gbToMibFactor = 1024.0; // base conversion
  const int overheadMbPerPartition = 4;

  group('PartitionCalculator', () {
    test('visibleGbToPartitionMb converts GB to MiB correctly', () {
      expect(
          PartitionCalculator.visibleGbToPartitionMb(1, FileSystemType.ntfs),
          equals((1 * gbToMibFactor).round() + overheadMbPerPartition));
      expect(
          PartitionCalculator.visibleGbToPartitionMb(10, FileSystemType.ntfs),
          equals((10 * gbToMibFactor).round() + overheadMbPerPartition));
      expect(
          PartitionCalculator.visibleGbToPartitionMb(100, FileSystemType.ntfs),
          equals((100 * gbToMibFactor).round() + overheadMbPerPartition));
      expect(
          PartitionCalculator.visibleGbToPartitionMb(2.5, FileSystemType.ntfs),
          equals((2.5 * gbToMibFactor).round() + overheadMbPerPartition));
      // FAT32 should be the same
      expect(
          PartitionCalculator.visibleGbToPartitionMb(1, FileSystemType.fat32),
          equals((1 * gbToMibFactor).round() + overheadMbPerPartition));
    });

    test('partitionMbToDisplayedGb converts MiB to GB correctly', () {
      // Inverse test: given MB, compute expected GB
      final int mb = ((10 * gbToMibFactor)).round() + overheadMbPerPartition;
      expect(
          PartitionCalculator.partitionMbToDisplayedGb(mb, FileSystemType.ntfs),
          closeTo(10, 0.01)); // Allow small rounding difference
    });

    test('calculate returns correct values', () {
      final double visibleGb = 10.0;
      final FileSystemType fs = FileSystemType.ntfs;
      final PartitionCalculationResult result =
          PartitionCalculator.calculate(visibleGb, fs);
      final int expectedMb = (visibleGb * gbToMibFactor).round() + overheadMbPerPartition;
      expect(result.partitionSizeMb, equals(expectedMb));
      expect(result.requiredInputMb, equals(expectedMb)); // no simulation
      expect(result.displayedGb, closeTo(visibleGb, 0.01));
      expect(result.usesWindowsSetupOverhead, isFalse);
      expect(result.filesystem, equals(fs));
    });

    test('calculate with Windows setup overhead adds overhead', () {
      final double visibleGb = 10.0;
      final FileSystemType fs = FileSystemType.ntfs;
      final PartitionCalculationResult result =
          PartitionCalculator.calculate(visibleGb, fs,
              simulateWindowsSetup: true);
      final int expectedMb = (visibleGb * gbToMibFactor).round() + overheadMbPerPartition;
      expect(result.partitionSizeMb, equals(expectedMb));
      expect(result.requiredInputMb,
          equals(expectedMb + PartitionCalculator.hiddenWindowsSetupOverheadMb));
      expect(result.displayedGb, closeTo(visibleGb, 0.01));
      expect(result.usesWindowsSetupOverhead, isTrue);
      expect(result.filesystem, equals(fs));
    });

    group('FAT32 size validation', () {
      test('isFat32SizeValid returns true for sizes <= 32 GB', () {
        expect(
            PartitionCalculator.isFat32SizeValid(32.0), isTrue);
        expect(
            PartitionCalculator.isFat32SizeValid(31.9), isTrue);
        expect(
            PartitionCalculator.isFat32SizeValid(0.0), isTrue);
      });

      test('isFat32SizeValid returns false for sizes > 32 GB', () {
        expect(
            PartitionCalculator.isFat32SizeValid(32.1), isFalse);
        expect(
            PartitionCalculator.isFat32SizeValid(100.0), isFalse);
      });
    });
  });
}