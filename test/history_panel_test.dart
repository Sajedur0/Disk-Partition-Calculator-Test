import 'package:disk_partition_calculator/managers/history_manager.dart';
import 'package:disk_partition_calculator/models/partition_models.dart';
import 'package:disk_partition_calculator/widgets/history_panel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets(
    'History panel does not leave a blank scroll area when content is short',
    (tester) async {
      SharedPreferences.setMockInitialValues({});
      final prefs = await SharedPreferences.getInstance();
      final historyManager = HistoryManager(prefs);

      await historyManager.addEntry(
        ConversionHistory(
          id: '1',
          inputValue: 120,
          fromUnit: 'GB',
          toUnit: 'GB',
          filesystem: FileSystemType.ntfs,
          requiredInputMb: 122880,
          result: '120 GB',
          timestamp: DateTime(2026, 9, 16, 10, 30),
        ),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: HistoryPanel(
              historyManager: historyManager,
              onClearHistory: () {},
            ),
          ),
        ),
      );

      expect(find.text('Conversion History'), findsOneWidget);
      expect(find.byType(ListView), findsNothing);
      expect(find.byType(SingleChildScrollView), findsOneWidget);
    },
  );
}
