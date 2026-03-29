import 'package:flutter/material.dart';

/// 统一 SnackBar，样式走 Theme，不引入硬编码色值。
abstract final class AppSnackBar {
  static void showMessage(BuildContext context, String message) {
    final messenger = ScaffoldMessenger.maybeOf(context);
    if (messenger == null) {
      return;
    }
    messenger.hideCurrentSnackBar();
    messenger.showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  static void showError(BuildContext context, String message) {
    showMessage(context, message);
  }
}
