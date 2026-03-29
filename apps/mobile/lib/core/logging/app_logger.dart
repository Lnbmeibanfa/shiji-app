import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

/// 调试日志；release 下静默。
abstract final class AppLogger {
  static void debug(String message) {
    if (kDebugMode) {
      // ignore: avoid_print
      print('[shiji] $message');
    }
  }

  static void apiError(String context, DioException e) {
    if (kDebugMode) {
      // ignore: avoid_print
      print('[shiji][api] $context: ${e.message} ${e.response?.statusCode}');
    }
  }
}
