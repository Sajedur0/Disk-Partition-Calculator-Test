import 'package:flutter_test/flutter_test.dart';
import 'package:disk_partition_calculator/utils/split_partition_calculator.dart';

void main() {
  group('SplitPartitionCalculator', () {
    test('calculates the fractional four-partition plan', () {
      expect(
        SplitPartitionCalculator.basePerPartitionMB(
          totalSizeMB: 476923,
          partitionCount: 4,
        ),
        closeTo(119226.75, 0.000001),
      );
      expect(
        SplitPartitionCalculator.displayedPerPartitionMB(
          totalSizeMB: 476923,
          partitionCount: 4,
        ),
        closeTo(119230.75, 0.000001),
      );
    });

    test('calculates the integer five-partition plan', () {
      expect(
        SplitPartitionCalculator.basePerPartitionMB(
          totalSizeMB: 100000,
          partitionCount: 5,
        ),
        19996,
      );
      expect(
        SplitPartitionCalculator.displayedPerPartitionMB(
          totalSizeMB: 100000,
          partitionCount: 5,
        ),
        20000,
      );
    });

    test('calculates the integer two-partition plan', () {
      expect(
        SplitPartitionCalculator.basePerPartitionMB(
          totalSizeMB: 50000,
          partitionCount: 2,
        ),
        24996,
      );
      expect(
        SplitPartitionCalculator.displayedPerPartitionMB(
          totalSizeMB: 50000,
          partitionCount: 2,
        ),
        25000,
      );
    });

    test('split plan keeps creation size separate from display size', () {
      final plan = SplitPartitionCalculator.splitPlan(
        totalSizeMB: 476923,
        partitionCount: 4,
      );

      expect(plan, hasLength(4));
      expect(plan.first.displayMB, closeTo(119230.75, 0.000001));
      expect(plan.first.createMB, closeTo(119226.75, 0.000001));
    });

    test('rejects a non-positive partition count', () {
      expect(
        () => SplitPartitionCalculator.basePerPartitionMB(
          totalSizeMB: 100,
          partitionCount: 0,
        ),
        throwsArgumentError,
      );
    });

    test('rejects capacity at or below total overhead', () {
      expect(
        () => SplitPartitionCalculator.basePerPartitionMB(
          totalSizeMB: 16,
          partitionCount: 4,
        ),
        throwsArgumentError,
      );
      expect(
        () => SplitPartitionCalculator.basePerPartitionMB(
          totalSizeMB: 15,
          partitionCount: 4,
        ),
        throwsArgumentError,
      );
    });
  });
}
