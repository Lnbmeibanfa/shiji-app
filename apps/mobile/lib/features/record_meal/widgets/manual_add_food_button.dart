import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import '../models/draft_food_item.dart';

/// 「+ 手动添加食物」：弹出 bottom sheet，确认后回调一条 [DraftFoodItem]。
class ManualAddFoodButton extends StatelessWidget {
  const ManualAddFoodButton({
    super.key,
    required this.onAdd,
  });

  final ValueChanged<DraftFoodItem> onAdd;

  Future<void> _openSheet(BuildContext context) async {
    final nameController = TextEditingController();
    final calController = TextEditingController();

    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.bgPrimary,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.xl)),
      ),
      builder: (ctx) {
        final bottom = MediaQuery.viewInsetsOf(ctx).bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: AppSpacing.s24,
            right: AppSpacing.s24,
            top: AppSpacing.s24,
            bottom: bottom + AppSpacing.s24,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                '手动添加食物',
                style: AppTypography.titleMedium(color: AppColors.textPrimary),
              ),
              const SizedBox(height: AppSpacing.s16),
              TextField(
                controller: nameController,
                decoration: const InputDecoration(
                  labelText: '食物名称',
                  border: OutlineInputBorder(),
                ),
                textInputAction: TextInputAction.next,
              ),
              const SizedBox(height: AppSpacing.s12),
              TextField(
                controller: calController,
                keyboardType:
                    const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(
                  labelText: '估算热量（kcal，可选）',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: AppSpacing.s20),
              FilledButton(
                onPressed: () {
                  final name = nameController.text.trim();
                  if (name.isEmpty) return;
                  final calRaw = calController.text.trim();
                  final cal =
                      calRaw.isEmpty ? null : double.tryParse(calRaw);
                  onAdd(DraftFoodItem(name: name, estimatedCalories: cal));
                  Navigator.of(ctx).pop();
                },
                style: FilledButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: AppColors.textInverse,
                  padding: const EdgeInsets.symmetric(vertical: AppSpacing.s16),
                ),
                child: Text(
                  '添加',
                  style: AppTypography.buttonText(color: AppColors.textInverse),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.bgMuted,
      borderRadius: BorderRadius.circular(AppRadius.pill),
      child: InkWell(
        onTap: () => _openSheet(context),
        borderRadius: BorderRadius.circular(AppRadius.pill),
        child: Padding(
          padding: const EdgeInsets.symmetric(
            vertical: AppSpacing.s16,
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.add_rounded, color: AppColors.textSecondary),
              const SizedBox(width: AppSpacing.s8),
              Text(
                '手动添加食物',
                style: AppTypography.bodyLarge(color: AppColors.textSecondary),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
