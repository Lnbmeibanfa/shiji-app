import 'package:flutter/material.dart';

import 'app_colors.dart';

/// 食迹 Design Token v1 — 字体层级（系统默认字体族）。
abstract final class AppTypography {
  static const String? _fontFamily = null;

  /// 页面主标题：28 / 600 / 1.25
  static TextStyle displayLarge({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 28,
        fontWeight: FontWeight.w600,
        height: 1.25,
        color: color ?? AppColors.textPrimary,
      );

  /// 大数字：24 / 600 / 1.2
  static TextStyle numberLarge({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 24,
        fontWeight: FontWeight.w600,
        height: 1.2,
        color: color ?? AppColors.textPrimary,
      );

  /// 页面二级标题：20 / 600 / 1.3
  static TextStyle titleLarge({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 20,
        fontWeight: FontWeight.w600,
        height: 1.3,
        color: color ?? AppColors.textPrimary,
      );

  /// 卡片标题：18 / 600 / 1.35
  static TextStyle titleMedium({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 18,
        fontWeight: FontWeight.w600,
        height: 1.35,
        color: color ?? AppColors.textPrimary,
      );

  /// 小标题：16 / 500 / 1.4
  static TextStyle titleSmall({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 16,
        fontWeight: FontWeight.w500,
        height: 1.4,
        color: color ?? AppColors.textPrimary,
      );

  /// 正文主文案：15 / 400 / 1.6
  static TextStyle bodyLarge({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 15,
        fontWeight: FontWeight.w400,
        height: 1.6,
        color: color ?? AppColors.textPrimary,
      );

  /// 正文次级：14 / 400 / 1.5
  static TextStyle bodyMedium({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 14,
        fontWeight: FontWeight.w400,
        height: 1.5,
        color: color ?? AppColors.textPrimary,
      );

  /// 辅助文字：13 / 400 / 1.45
  static TextStyle bodySmall({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 13,
        fontWeight: FontWeight.w400,
        height: 1.45,
        color: color ?? AppColors.textPrimary,
      );

  /// 标签文字：13 / 500 / 1.2
  static TextStyle labelMedium({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 13,
        fontWeight: FontWeight.w500,
        height: 1.2,
        color: color ?? AppColors.textPrimary,
      );

  /// 按钮文字：16 / 500 / 1.2
  static TextStyle buttonText({Color? color}) => TextStyle(
        fontFamily: _fontFamily,
        fontSize: 16,
        fontWeight: FontWeight.w500,
        height: 1.2,
        color: color ?? AppColors.textPrimary,
      );
}
