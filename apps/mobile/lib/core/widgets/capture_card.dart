import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_sizes.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

/// 首页拍照主卡：主色底、超大圆角、固定高度 172。
class CaptureCard extends StatelessWidget {
  const CaptureCard({
    super.key,
    required this.title,
    this.icon = Icons.camera_alt_outlined,
    this.onTap,
  });

  final String title;
  final IconData icon;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.xl),
        child: Ink(
          height: AppSizes.captureCardHeight,
          decoration: BoxDecoration(
            color: AppColors.primary,
            borderRadius: BorderRadius.circular(AppRadius.xl),
          ),
          padding: const EdgeInsets.all(AppSpacing.s20),
          child: Row(
            children: [
              Container(
                width: AppSizes.iconCapture + AppSpacing.s16,
                height: AppSizes.iconCapture + AppSpacing.s16,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppColors.captureCardIconBackdrop,
                ),
                child: Icon(icon, color: AppColors.textInverse, size: AppSizes.iconCapture),
              ),
              const SizedBox(width: AppSpacing.s16),
              Expanded(
                child: Text(
                  title,
                  style: AppTypography.titleMedium(color: AppColors.textInverse),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
