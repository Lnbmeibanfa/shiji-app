import 'package:flutter/material.dart';

import 'app_feedback.dart';

/// 旧版 SnackBar 入口；已委托 [AppFeedback] Overlay Toast。
@Deprecated('Use AppFeedback.showToast with FeedbackToastKind')
abstract final class AppSnackBar {
  static void showMessage(BuildContext context, String message) {
    AppFeedback.showToast(
      context,
      kind: FeedbackToastKind.hintFrosted,
      title: message,
    );
  }

  static void showError(BuildContext context, String message) {
    AppFeedback.showToast(
      context,
      kind: FeedbackToastKind.failure,
      title: message,
    );
  }
}
