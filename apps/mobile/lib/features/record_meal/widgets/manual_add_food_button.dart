import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import '../models/draft_food_item.dart';
import 'add_food_bottom_sheet.dart';

/// 「+ 手动添加食物」：打开搜索底栏，选中后回调 [DraftFoodItem]。
class ManualAddFoodButton extends ConsumerWidget {
  const ManualAddFoodButton({
    super.key,
    required this.onAdd,
  });

  final ValueChanged<DraftFoodItem> onAdd;

  Future<void> _openSheet(BuildContext context) async {
    final item = await showModalBottomSheet<DraftFoodItem>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => const AddFoodBottomSheet(),
    );
    if (item != null) onAdd(item);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
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
