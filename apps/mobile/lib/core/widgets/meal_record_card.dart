import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_sizes.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

/// 饮食记录卡：compact / full 两种图片边长。
enum MealRecordCardVariant { compact, full }

class MealRecordCard extends StatelessWidget {
  const MealRecordCard({
    super.key,
    required this.title,
    required this.subtitle,
    required this.image,
    this.variant = MealRecordCardVariant.compact,
  });

  final String title;
  final String subtitle;
  final Widget image;
  final MealRecordCardVariant variant;

  double get _imageSize => switch (variant) {
        MealRecordCardVariant.compact => AppSizes.mealImageCompact,
        MealRecordCardVariant.full => AppSizes.mealImageFull,
      };

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.bgCard,
        borderRadius: BorderRadius.circular(AppRadius.lg),
      ),
      padding: const EdgeInsets.all(AppSpacing.s16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(AppRadius.md),
            child: SizedBox(
              width: _imageSize,
              height: _imageSize,
              child: image,
            ),
          ),
          const SizedBox(width: AppSpacing.s12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: AppTypography.titleSmall()),
                const SizedBox(height: AppSpacing.s4),
                Text(subtitle, style: AppTypography.bodyMedium(color: AppColors.textSecondary)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
