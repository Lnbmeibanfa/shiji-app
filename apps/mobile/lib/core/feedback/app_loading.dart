import 'package:flutter/material.dart';

/// 全屏 loading 屏障（登录等提交期间使用）。
Future<T> withAppLoadingOverlay<T>(
  BuildContext context,
  Future<T> future,
) async {
  showDialog<void>(
    context: context,
    barrierDismissible: false,
    builder: (context) => const Center(
      child: CircularProgressIndicator(),
    ),
  );
  try {
    return await future;
  } finally {
    if (context.mounted) {
      Navigator.of(context, rootNavigator: true).pop();
    }
  }
}
