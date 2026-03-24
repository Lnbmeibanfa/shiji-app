import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_sizes.dart';
import '../theme/app_typography.dart';

/// 食迹主按钮（Primary）：主色、白字、圆角 md、高度 52。
class ShijiButton extends StatelessWidget {
  const ShijiButton({
    super.key,
    required this.label,
    this.onPressed,
  });

  final String label;
  final VoidCallback? onPressed;

  bool get _enabled => onPressed != null;

  @override
  Widget build(BuildContext context) {
    final bg = _enabled ? AppColors.primary : AppColors.buttonDisabledBackground;
    final fg =
        _enabled ? AppColors.textInverse : AppColors.buttonDisabledForeground;

    return SizedBox(
      height: AppSizes.buttonPrimaryHeight,
      width: double.infinity,
      child: Material(
        color: bg,
        borderRadius: BorderRadius.circular(AppRadius.md),
        child: InkWell(
          onTap: onPressed,
          borderRadius: BorderRadius.circular(AppRadius.md),
          child: Center(
            child: Text(
              label,
              style: AppTypography.buttonText(color: fg),
            ),
          ),
        ),
      ),
    );
  }
}
