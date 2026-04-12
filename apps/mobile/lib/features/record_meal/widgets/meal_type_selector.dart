import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';

/// 后端 `meal_type` 字符串。
abstract final class MealTypes {
  static const breakfast = 'breakfast';
  static const lunch = 'lunch';
  static const dinner = 'dinner';
  static const snack = 'snack';
}

/// 四餐单选。
class MealTypeSelector extends StatelessWidget {
  const MealTypeSelector({
    super.key,
    required this.value,
    required this.onChanged,
  });

  final String value;
  final ValueChanged<String> onChanged;

  static const _items = <({String code, String label})>[
    (code: MealTypes.breakfast, label: '早餐'),
    (code: MealTypes.lunch, label: '午餐'),
    (code: MealTypes.dinner, label: '晚餐'),
    (code: MealTypes.snack, label: '加餐'),
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '这是哪一餐？',
          style: AppTypography.titleSmall(color: AppColors.textPrimary),
        ),
        const SizedBox(height: AppSpacing.s12),
        Wrap(
          spacing: AppSpacing.s8,
          runSpacing: AppSpacing.s8,
          children: [
            for (final item in _items)
              _Pill(
                label: item.label,
                selected: value == item.code,
                onTap: () => onChanged(item.code),
              ),
          ],
        ),
      ],
    );
  }
}

class _Pill extends StatelessWidget {
  const _Pill({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: selected ? AppColors.primary : AppColors.bgMuted,
      borderRadius: BorderRadius.circular(AppRadius.pill),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.pill),
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.s16,
            vertical: AppSpacing.s12,
          ),
          child: Text(
            label,
            style: AppTypography.labelMedium(
              color: selected ? AppColors.textInverse : AppColors.textPrimary,
            ).copyWith(fontSize: 16),
          ),
        ),
      ),
    );
  }
}
