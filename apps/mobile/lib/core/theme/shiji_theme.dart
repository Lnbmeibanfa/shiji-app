import 'package:flutter/material.dart';

import 'app_colors.dart';
import 'app_radius.dart';
import 'app_spacing.dart';
import 'app_typography.dart';

/// 食迹全局 ThemeData，与 Design Token v1 对齐。
ThemeData buildShijiTheme() {
  final colorScheme = ColorScheme.light(
    primary: AppColors.primary,
    onPrimary: AppColors.textInverse,
    surface: AppColors.bgCard,
    onSurface: AppColors.textPrimary,
    error: AppColors.dangerSoft,
    onError: AppColors.textInverse,
  );

  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: AppColors.bgPrimary,
    splashColor: AppColors.primarySoft.withValues(alpha: 0.4),
    highlightColor: AppColors.primarySoft.withValues(alpha: 0.3),
    appBarTheme: AppBarTheme(
      backgroundColor: AppColors.bgPrimary,
      foregroundColor: AppColors.textPrimary,
      elevation: 0,
      scrolledUnderElevation: 0,
      titleTextStyle: AppTypography.titleMedium(),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: AppColors.bgSecondary,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(AppRadius.md),
        borderSide: BorderSide.none,
      ),
      contentPadding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.s16,
        vertical: AppSpacing.s12,
      ),
      hintStyle: AppTypography.bodyLarge(color: AppColors.textTertiary),
    ),
  );
}
