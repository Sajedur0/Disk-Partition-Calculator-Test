class SplitPartitionCalculator {
  static const int perPartitionOverheadMB = 4;

  /// Returns the actual whole-MB size to request when creating each partition.
  /// Fractional MB values remain internal; the Split Tab rounds this value to
  /// the nearest MB only when passing it to diskpart.
  /// This value is INTERNAL — never display or persist it.
  static double basePerPartitionMB({
    required double totalSizeMB,
    required int partitionCount,
  }) {
    if (partitionCount <= 0) {
      throw ArgumentError('partitionCount must be > 0');
    }
    final totalOverheadMB = partitionCount * perPartitionOverheadMB;
    if (totalSizeMB <= totalOverheadMB) {
      throw ArgumentError(
        'Total size too small for the requested partition count',
      );
    }
    final adjustedCapacityMB = totalSizeMB - totalOverheadMB;
    return adjustedCapacityMB / partitionCount;
  }

  /// Returns the size (MB) to SHOW to the user for each partition in the
  /// Split Result table.
  static double displayedPerPartitionMB({
    required double totalSizeMB,
    required int partitionCount,
  }) {
    return basePerPartitionMB(
          totalSizeMB: totalSizeMB,
          partitionCount: partitionCount,
        ) +
        perPartitionOverheadMB;
  }

  /// Convenience: returns a list of {displayMB, createMB} pairs, one per
  /// partition, ready for both UI rendering and the creation service call.
  static List<({double displayMB, double createMB})> splitPlan({
    required double totalSizeMB,
    required int partitionCount,
  }) {
    final base = basePerPartitionMB(
      totalSizeMB: totalSizeMB,
      partitionCount: partitionCount,
    );
    final display = base + perPartitionOverheadMB;
    return List.generate(
      partitionCount,
      (_) => (displayMB: display, createMB: base),
    );
  }
}
