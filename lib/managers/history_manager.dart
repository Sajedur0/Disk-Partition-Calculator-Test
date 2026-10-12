import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/partition_models.dart';

class HistoryManager {
  static const String _historyKey = 'conversion_history';
  static const int _maxEntries = 100;
  final SharedPreferences _prefs;
  List<ConversionHistory> _history = [];

  HistoryManager(this._prefs) {
    _loadHistory();
  }

  List<ConversionHistory> get history => List.unmodifiable(_history);

  void _loadHistory() {
    final String? historyJson = _prefs.getString(_historyKey);
    if (historyJson != null) {
      try {
        final List<dynamic> decoded = jsonDecode(historyJson);
        _history = decoded
            .map((item) => ConversionHistory.fromMap(item))
            .toList();
        if (_history.length > _maxEntries) {
          _history = _history.sublist(0, _maxEntries);
        }
      } catch (e) {
        _history = [];
      }
    }
  }

  Future<void> addEntry(ConversionHistory entry) async {
    _history.insert(0, entry);
    if (_history.length > _maxEntries) {
      _history = _history.sublist(0, _maxEntries);
    }
    await _saveHistory();
  }

  Future<void> clearHistory() async {
    _history.clear();
    await _prefs.remove(_historyKey);
  }

  Future<void> _saveHistory() async {
    final String encoded = jsonEncode(
      _history.map((entry) => entry.toMap()).toList(),
    );
    await _prefs.setString(_historyKey, encoded);
  }
}
