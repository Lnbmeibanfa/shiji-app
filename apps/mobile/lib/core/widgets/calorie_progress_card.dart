import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';
import 'shiji_card.dart';

/// 热量进度卡：外层 ShijiCard 语义，内含 numberLarge 与进度条。
class CalorieProgressCard extends StatelessWidget {
  const CalorieProgressCard({
    super.key,
    required this.kcalLabel,
    this.progress = 0,
  });

  /// 已摄入 kcal 展示文案（由调用方格式化，保持组件通用）。
  final String kcalLabel;

  /// 0.0 ~ 1.0
  final double progress;

  @override
  Widget build(BuildContext context) {
    final clamped = progress.clamp(0.0, 1.0);

    return ShijiCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(kcalLabel, style: AppTypography.numberLarge()),
          const SizedBox(height: AppSpacing.s12),
          ClipRRect(
            borderRadius: BorderRadius.circular(AppRadius.pill),
            child: LinearProgressIndicator(
              value: clamped,
              minHeight: 8,
              backgroundColor: AppColors.progressTrack,
              color: AppColors.primary,
            ),
          ),
        ],
      ),
    );
  }
}
