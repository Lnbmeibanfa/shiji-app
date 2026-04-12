import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
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

/// 「此刻的感受」多选：浅灰胶囊、无描边，与食迹 token 一致（避免 FilterChip 默认白底黑边）。
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
              _EmotionPill(
                option: o,
                selected: selectedIds.contains(o.id),
                onTap: () {
                  final next = Set<int>.from(selectedIds);
                  if (next.contains(o.id)) {
                    next.remove(o.id);
                  } else {
                    next.add(o.id);
                  }
                  onChanged(next);
                },
              ),
          ],
        ),
      ],
    );
  }
}

class _EmotionPill extends StatelessWidget {
  const _EmotionPill({
    required this.option,
    required this.selected,
    required this.onTap,
  });

  final EmotionOption option;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final bg = selected ? AppColors.primarySoft : AppColors.tagNeutralBg;
    final fg = selected ? AppColors.primary : AppColors.textPrimary;

    return Material(
      color: bg,
      borderRadius: BorderRadius.circular(AppRadius.pill),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.pill),
        splashColor: AppColors.primary.withValues(alpha: 0.12),
        highlightColor: AppColors.primary.withValues(alpha: 0.06),
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.s16,
            vertical: AppSpacing.s12,
          ),
          child: Text(
            '${option.emoji} ${option.label}',
            style: AppTypography.labelMedium(color: fg).copyWith(fontSize: 14),
          ),
        ),
      ),
    );
  }
}
