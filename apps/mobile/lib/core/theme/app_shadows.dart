import 'package:flutter/material.dart';

/// 食迹 Design Token v1 — 阴影（轻、克制）。
abstract final class AppShadows {
  static const Color _shadowColor = Color(0xFF000000);

  /// 卡片：opacity 0.04, blur 16, offsetY 4
  static List<BoxShadow> get shadowCard => const [
        BoxShadow(
          color: Color(0x0A000000), // ~4% black
          blurRadius: 16,
          offset: Offset(0, 4),
        ),
      ];

  /// 浮层：opacity 0.06, blur 24, offsetY 8
  static List<BoxShadow> get shadowFloating => const [
        BoxShadow(
          color: Color(0x0F000000), // ~6% black
          blurRadius: 24,
          offset: Offset(0, 8),
        ),
      ];

  /// 与规范等价的黑色不透明度（便于单测校验参数）。
  static Color get cardShadowColor => _shadowColor.withValues(alpha: 0.04);

  static Color get floatingShadowColor => _shadowColor.withValues(alpha: 0.06);
}
