import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';

/// 备注多行输入。
class MealNotesField extends StatelessWidget {
  const MealNotesField({
    super.key,
    required this.controller,
  });

  final TextEditingController controller;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '添加备注（可选）',
          style: AppTypography.titleSmall(color: AppColors.textPrimary),
        ),
        const SizedBox(height: AppSpacing.s12),
        TextField(
          controller: controller,
          minLines: 3,
          maxLines: 5,
          decoration: InputDecoration(
            hintText: '记录今天的想法…',
            hintStyle: AppTypography.bodyMedium(color: AppColors.textTertiary),
            filled: true,
            fillColor: AppColors.bgMuted,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(AppRadius.md),
              borderSide: BorderSide.none,
            ),
          ),
          style: AppTypography.bodyMedium(color: AppColors.textPrimary),
        ),
      ],
    );
  }
}
