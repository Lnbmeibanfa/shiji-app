import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';

/// 本地情绪选项（UI 多选；保存接口接线前不提交 emotion_tag_id）。
class EmotionOption {
  const EmotionOption({
    required this.id,
    required this.emoji,
    required this.label,
  });

  final int id;
  final String emoji;
  final String label;
}

/// 「此刻的感受」多选 chips。
class EmotionChipsSection extends StatelessWidget {
  const EmotionChipsSection({
    super.key,
    required this.selectedIds,
    required this.onChanged,
  });

  final Set<int> selectedIds;
  final ValueChanged<Set<int>> onChanged;

  static const List<EmotionOption> options = [
    EmotionOption(id: 1, emoji: '😋', label: '很饿'),
    EmotionOption(id: 2, emoji: '😌', label: '想放松'),
    EmotionOption(id: 3, emoji: '🥂', label: '聚餐'),
    EmotionOption(id: 4, emoji: '😨', label: '压力大'),
    EmotionOption(id: 5, emoji: '🎉', label: '奖励自己'),
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '此刻的感受（可选）',
          style: AppTypography.titleSmall(color: AppColors.textPrimary),
        ),
        const SizedBox(height: AppSpacing.s12),
        Wrap(
          spacing: AppSpacing.s8,
          runSpacing: AppSpacing.s8,
          children: [
            for (final o in options)
              FilterChip(
                label: Text('${o.emoji} ${o.label}'),
                selected: selectedIds.contains(o.id),
                onSelected: (selected) {
                  final next = Set<int>.from(selectedIds);
                  if (selected) {
                    next.add(o.id);
                  } else {
                    next.remove(o.id);
                  }
                  onChanged(next);
                },
                selectedColor: AppColors.primary.withValues(alpha: 0.2),
                checkmarkColor: AppColors.primary,
                labelStyle: AppTypography.labelMedium(
                  color: selectedIds.contains(o.id)
                      ? AppColors.primary
                      : AppColors.textPrimary,
                ),
              ),
          ],
        ),
      ],
    );
  }
}
