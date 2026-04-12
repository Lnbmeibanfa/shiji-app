import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';

/// 底部「保存记录」：无食物时禁用（置灰）。
class RecordMealSaveBar extends StatelessWidget {
  const RecordMealSaveBar({
    super.key,
    required this.canSave,
    required this.loading,
    required this.onSave,
  });

  final bool canSave;
  final bool loading;
  final VoidCallback onSave;

  @override
  Widget build(BuildContext context) {
    final enabled = canSave && !loading;

    return SafeArea(
      top: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.s24,
          AppSpacing.s8,
          AppSpacing.s24,
          AppSpacing.s16,
        ),
        child: SizedBox(
          width: double.infinity,
          child: FilledButton(
            onPressed: enabled ? onSave : null,
            style: FilledButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: AppColors.textInverse,
              disabledBackgroundColor: AppColors.bgMuted,
              disabledForegroundColor: AppColors.textTertiary,
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.s16),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppRadius.lg),
              ),
            ),
            child: loading
                ? const SizedBox(
                    height: 22,
                    width: 22,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: AppColors.textInverse,
                    ),
                  )
                : Text(
                    '保存记录',
                    style: AppTypography.buttonText(color: AppColors.textInverse),
                  ),
          ),
        ),
      ),
    );
  }
}
