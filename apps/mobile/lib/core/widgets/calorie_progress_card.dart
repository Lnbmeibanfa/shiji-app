import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';
import 'shiji_card.dart';

class _Summary {
  const _Summary({
    required this.consumed,
    required this.remaining,
    required this.goal,
  });

  final int consumed;
  final int remaining;
  final int goal;
}

/// 热量进度卡：外层 ShijiCard 语义，内含 numberLarge 与进度条。
///
/// 简单模式：`[kcalLabel]` + `[progress]`（兼容旧用法）。
/// 摘要模式：[CalorieProgressCard.summary] 展示已摄入 / 还可吃 / 目标；当 `consumed >= goal` 时进度满条且条色为 [AppColors.progressOverBudget]。
class CalorieProgressCard extends StatelessWidget {
  const CalorieProgressCard({
    super.key,
    required this.kcalLabel,
    this.progress = 0,
  })  : _summary = null;

  CalorieProgressCard.summary({
    super.key,
    required int consumedKcal,
    required int remainingKcal,
    required int goalKcal,
  })  : kcalLabel = '',
        progress = 0,
        _summary = _Summary(
          consumed: consumedKcal,
          remaining: remainingKcal,
          goal: goalKcal,
        );

  /// 已摄入 kcal 展示文案（简单模式由调用方格式化）。
  final String kcalLabel;

  /// 0.0 ~ 1.0（仅简单模式）
  final double progress;

  final _Summary? _summary;

  @override
  Widget build(BuildContext context) {
    final summary = _summary;
    if (summary != null) {
      return _buildSummary(summary);
    }
    return _buildSimple();
  }

  Widget _buildSimple() {
    final clamped = progress.clamp(0.0, 1.0);
    const barColor = AppColors.primary;

    return ShijiCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(kcalLabel, style: AppTypography.numberLarge()),
          const SizedBox(height: AppSpacing.s12),
          _bar(clamped, barColor),
        ],
      ),
    );
  }

  Widget _buildSummary(_Summary s) {
    final goal = s.goal;
    final consumed = s.consumed;
    final frac = goal > 0 ? consumed / goal : 0.0;
    final clamped = frac.clamp(0.0, 1.0);
    final barColor =
        consumed >= goal ? AppColors.progressOverBudget : AppColors.primary;

    return ShijiCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '今日已摄入',
                      style: AppTypography.bodySmall(
                        color: AppColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.s4),
                    Text.rich(
                      TextSpan(
                        style: AppTypography.numberLarge(),
                        children: [
                          TextSpan(text: '$consumed'),
                          TextSpan(
                            text: ' kcal',
                            style: AppTypography.titleSmall(
                              color: AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '还可以吃',
                      style: AppTypography.bodySmall(
                        color: AppColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.s4),
                    Text(
                      '${s.remaining}',
                      style: AppTypography.numberLarge(
                        color: AppColors.tagGreenText,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.s12),
          _bar(clamped, barColor),
          const SizedBox(height: AppSpacing.s12),
          Text(
            '目标 $goal kcal',
            textAlign: TextAlign.center,
            style: AppTypography.bodySmall(color: AppColors.textSecondary),
          ),
        ],
      ),
    );
  }

  Widget _bar(double value, Color color) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppRadius.pill),
      child: LinearProgressIndicator(
        value: value,
        minHeight: 8,
        backgroundColor: AppColors.progressTrack,
        color: color,
      ),
    );
  }
}
