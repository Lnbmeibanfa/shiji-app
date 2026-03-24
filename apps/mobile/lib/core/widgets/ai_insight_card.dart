import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_sizes.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

/// AI 建议卡：暖色外层 + icon 区 + 内层建议块（品牌感核心组件）。
class AIInsightCard extends StatelessWidget {
  const AIInsightCard({
    super.key,
    required this.title,
    required this.body,
    this.suggestion,
    this.icon = Icons.spa_outlined,
  });

  final String title;
  final String body;
  final String? suggestion;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.accentWarm,
        borderRadius: BorderRadius.circular(AppRadius.lg),
      ),
      padding: const EdgeInsets.all(AppSpacing.s20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(AppSpacing.s8),
                decoration: BoxDecoration(
                  color: AppColors.aiInsightIconBackground,
                  borderRadius: BorderRadius.circular(AppRadius.md),
                ),
                child: Icon(icon, color: AppColors.warning, size: AppSizes.iconMd),
              ),
              const SizedBox(width: AppSpacing.s12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: AppTypography.titleMedium()),
                    const SizedBox(height: AppSpacing.s8),
                    Text(body, style: AppTypography.bodyLarge()),
                  ],
                ),
              ),
            ],
          ),
          if (suggestion != null && suggestion!.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.s16),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(AppSpacing.s16),
              decoration: BoxDecoration(
                color: AppColors.accentWarmInner,
                borderRadius: BorderRadius.circular(AppRadius.md),
              ),
              child: Text(suggestion!, style: AppTypography.bodyLarge()),
            ),
          ],
        ],
      ),
    );
  }
}
