import 'package:flutter/material.dart';

/// 食迹 Design Token v1 — 间距（仅使用下列数值）。
abstract final class AppSpacing {
  static const double s4 = 4;
  static const double s8 = 8;
  static const double s12 = 12;
  static const double s16 = 16;
  static const double s20 = 20;
  static const double s24 = 24;
  static const double s28 = 28;
  static const double s32 = 32;
  static const double s40 = 40;

  /// 页面水平外边距（规范 24）。
  static const EdgeInsets pageHorizontal =
      EdgeInsets.symmetric(horizontal: s24);

  /// 普通卡片内边距（规范 20）。
  static const EdgeInsets cardPadding = EdgeInsets.all(s20);

  /// 紧凑卡片内边距（规范 16）。
  static const EdgeInsets cardPaddingCompact = EdgeInsets.all(s16);
}
