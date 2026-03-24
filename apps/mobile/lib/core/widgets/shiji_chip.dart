import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_sizes.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

/// 食迹标签 Chip：默认中性色，选中为主色白字，胶囊圆角。
class ShijiChip extends StatelessWidget {
  const ShijiChip({
    super.key,
    required this.label,
    this.selected = false,
    this.onTap,
  });

  final String label;
  final bool selected;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final bg = selected ? AppColors.primary : AppColors.tagNeutralBg;
    final fg = selected ? AppColors.textInverse : AppColors.tagNeutralText;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.pill),
        child: Container(
          height: AppSizes.chipHeight,
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.s16),
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: bg,
            borderRadius: BorderRadius.circular(AppRadius.pill),
          ),
          child: Text(label, style: AppTypography.labelMedium(color: fg)),
        ),
      ),
    );
  }
}
