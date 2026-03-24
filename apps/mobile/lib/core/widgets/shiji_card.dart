import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_shadows.dart';
import '../theme/app_spacing.dart';

/// 食迹通用卡片：bgCard、大圆角、内边距 20，可选轻阴影或浅边框。
class ShijiCard extends StatelessWidget {
  const ShijiCard({
    super.key,
    required this.child,
    this.useShadow = false,
    this.showBorder = false,
  });

  final Widget child;
  final bool useShadow;
  final bool showBorder;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.bgCard,
        borderRadius: BorderRadius.circular(AppRadius.lg),
        border: showBorder
            ? Border.all(color: AppColors.borderLight)
            : null,
        boxShadow: useShadow ? AppShadows.shadowCard : null,
      ),
      padding: AppSpacing.cardPadding,
      child: child,
    );
  }
}
