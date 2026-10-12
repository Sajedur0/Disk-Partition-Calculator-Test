import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:in_app_update_android/in_app_update_android.dart';

class UpdateService {
  static StreamSubscription<InstallState>? _subscription;
  static bool _isChecking = false;
  static bool _userDismissedInSession = false;
  static Timer? _fallbackTimer;

  static const _restartChannel = MethodChannel('com.diskcalc/restart');

  static void dispose() {
    _cancelListener();
    _isChecking = false;
  }

  static Future<void> checkForUpdate(
    BuildContext context, {
    bool isResume = false,
  }) async {
    if (!InAppUpdate.isAndroid) return;
    if (_isChecking) return;
    if (isResume && _userDismissedInSession) return;

    _isChecking = true;

    try {
      _cancelListener();

      final info = await InAppUpdate.checkForUpdate().timeout(
        const Duration(seconds: 10),
      );
      if (!context.mounted) return;

      if (info.installStatus == InstallStatus.downloaded) {
        _completeFlexibleUpdate();
        return;
      }

      if (info.immediateUpdateInProgress) {
        if (_userDismissedInSession) return;
        _listenForFlexibleUpdate();
        final result = await InAppUpdate.startFlexibleUpdate();
        if (result == AppUpdateResult.userDeniedUpdate) {
          _userDismissedInSession = true;
        }
        return;
      }

      if (!info.updateAvailable) return;

      final staleness = info.clientVersionStalenessDays ?? 0;
      if (info.immediateUpdateAllowed && staleness >= 5) {
        final result = await InAppUpdate.performImmediateUpdate();
        if (result == AppUpdateResult.userDeniedUpdate) {
          _userDismissedInSession = true;
        }
        return;
      }

      if (!info.flexibleUpdateAllowed) return;

      _listenForFlexibleUpdate();
      final result = await InAppUpdate.startFlexibleUpdate();
      if (result == AppUpdateResult.userDeniedUpdate) {
        _userDismissedInSession = true;
      }
    } on InAppUpdateException catch (e) {
      debugPrint('UpdateService: plugin error [${e.code}] ${e.message}');
      _cancelListener();
    } catch (e) {
      debugPrint('UpdateService.checkForUpdate error: $e');
      _cancelListener();
    } finally {
      _isChecking = false;
    }
  }

  static void _listenForFlexibleUpdate() {
    _cancelListener();
    _fallbackTimer?.cancel();

    _subscription = InAppUpdate.installStateListener.listen(
      (state) {
        switch (state.installStatus) {
          case InstallStatus.downloaded:
            _cancelListener();
            _completeFlexibleUpdate();
          case InstallStatus.installed:
            _fallbackTimer?.cancel();
            _restartApp();
            _cancelListener();
          case InstallStatus.failed:
            debugPrint(
              'UpdateService: flexible update failed '
              '(code ${state.installErrorCode})',
            );
            _fallbackTimer?.cancel();
            _cancelListener();
          case InstallStatus.canceled:
            _fallbackTimer?.cancel();
            _cancelListener();
          case InstallStatus.pending:
          case InstallStatus.downloading:
          case InstallStatus.installing:
          case InstallStatus.unknown:
            break;
        }
      },
      onError: (e) {
        debugPrint('UpdateService listener error: $e');
        _fallbackTimer?.cancel();
        _cancelListener();
      },
    );

    _fallbackTimer = Timer(const Duration(seconds: 30), () {
      debugPrint('UpdateService: fallback restart after timeout');
      _cancelListener();
      _restartApp();
    });
  }

  static Future<void> _completeFlexibleUpdate() async {
    try {
      await InAppUpdate.completeFlexibleUpdate();
      await Future.delayed(const Duration(seconds: 2));
      _restartApp();
    } catch (e) {
      debugPrint('UpdateService.completeFlexibleUpdate error: $e');
    }
  }

  static Future<void> _restartApp() async {
    try {
      await _restartChannel.invokeMethod('restartApp');
    } catch (e) {
      debugPrint('UpdateService.restartApp error: $e');
    }
  }

  static void _cancelListener() {
    _subscription?.cancel();
    _subscription = null;
  }
}
