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
    this.isLoading = false,
  });

  final String label;
  final VoidCallback? onPressed;

  /// 为 true 时展示 loading 并禁止点击（用于登录等提交）。
  final bool isLoading;

  bool get _enabled => onPressed != null && !isLoading;

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
          onTap: _enabled ? onPressed : null,
          borderRadius: BorderRadius.circular(AppRadius.md),
          child: Center(
            child: isLoading
                ? SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: fg,
                    ),
                  )
                : Text(
                    label,
                    style: AppTypography.buttonText(color: fg),
                  ),
          ),
        ),
      ),
    );
  }
}
