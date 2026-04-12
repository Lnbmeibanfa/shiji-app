import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import 'meal_type_selector.dart';

/// 最近一餐（仅占位，无接口）。
class RecentMealsPlaceholder extends StatelessWidget {
  const RecentMealsPlaceholder({
    super.key,
    required this.mealType,
  });

  final String mealType;

  String get _mealLabel {
    return switch (mealType) {
      MealTypes.breakfast => '早餐',
      MealTypes.lunch => '午餐',
      MealTypes.dinner => '晚餐',
      MealTypes.snack => '加餐',
      _ => '餐次',
    };
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(
              Icons.schedule_rounded,
              size: 20,
              color: AppColors.textSecondary,
            ),
            const SizedBox(width: AppSpacing.s8),
            Text(
              '最近的$_mealLabel',
              style: AppTypography.titleSmall(color: AppColors.textPrimary),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.s12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(AppSpacing.s20),
          decoration: BoxDecoration(
            color: AppColors.bgMuted,
            borderRadius: BorderRadius.circular(AppRadius.lg),
            border: Border.all(color: AppColors.borderLight),
          ),
          child: Text(
            '暂无最近记录，后续将支持快速复用',
            style: AppTypography.bodyMedium(color: AppColors.textTertiary),
            textAlign: TextAlign.center,
          ),
        ),
      ],
    );
  }
}
