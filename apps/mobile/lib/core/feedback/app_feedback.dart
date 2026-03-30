import 'dart:async';
import 'dart:ui' show ImageFilter;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_shadows.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

/// Toast 语义（与设计稿四档对应）。
enum FeedbackToastKind {
  success,
  failure,
  hintFrosted,
  hintWarm,
}

/// 全局反馈：Toast（Overlay）、顶区 Banner、确认 Dialog。
abstract final class AppFeedback {
  static OverlayEntry? _toastEntry;
  static OverlayEntry? _bannerEntry;
  static Timer? _bannerTimer;

  static const Duration _defaultToastDuration = Duration(seconds: 4);

  /// 底部 Toast：双行可选，3–5s 默认 4s；单条替换。
  static void showToast(
    BuildContext context, {
    required FeedbackToastKind kind,
    required String title,
    String? subtitle,
    Duration? duration,
  }) {
    final overlay = Overlay.maybeOf(context, rootOverlay: true);
    if (overlay == null) {
      return;
    }
    _toastEntry?.remove();
    _toastEntry = null;

    final d = duration ?? _defaultToastDuration;
    late OverlayEntry entry;
    entry = OverlayEntry(
      builder: (ctx) => _ToastOverlayBody(
        kind: kind,
        title: title,
        subtitle: subtitle,
      ),
    );
    _toastEntry = entry;
    overlay.insert(entry);
    Future<void>.delayed(d, () {
      if (_toastEntry == entry) {
        entry.remove();
        _toastEntry = null;
      }
    });
  }

  /// 顶区公告：可选「查看详情」、关闭、自动消失。
  static void showBanner(
    BuildContext context, {
    required String message,
    String detailLabel = '查看详情',
    VoidCallback? onDetail,
    Duration? autoDismiss,
  }) {
    final overlay = Overlay.maybeOf(context, rootOverlay: true);
    if (overlay == null) {
      return;
    }
    _bannerTimer?.cancel();
    _bannerEntry?.remove();
    _bannerEntry = null;

    late OverlayEntry entry;
    entry = OverlayEntry(
      builder: (ctx) => _BannerOverlayBody(
        message: message,
        detailLabel: detailLabel,
        onDetail: onDetail,
        onClose: () {
          _bannerTimer?.cancel();
          if (_bannerEntry == entry) {
            entry.remove();
            _bannerEntry = null;
          }
        },
      ),
    );
    _bannerEntry = entry;
    overlay.insert(entry);

    if (autoDismiss != null) {
      _bannerTimer = Timer(autoDismiss, () {
        if (_bannerEntry == entry) {
          entry.remove();
          _bannerEntry = null;
        }
      });
    }
  }

  /// 温和确认弹窗：主按钮色为 [AppColors.feedbackToastSuccess]。
  static Future<void> showConfirmDialog(
    BuildContext context, {
    required String title,
    required String message,
    String cancelLabel = '取消',
    String confirmLabel = '保存',
    VoidCallback? onConfirm,
    VoidCallback? onCancel,
  }) {
    return showDialog<void>(
      context: context,
      barrierDismissible: true,
      builder: (ctx) => _FeedbackConfirmDialog(
        title: title,
        message: message,
        cancelLabel: cancelLabel,
        confirmLabel: confirmLabel,
        onConfirm: onConfirm,
        onCancel: onCancel,
      ),
    );
  }
}

class _ToastOverlayBody extends StatelessWidget {
  const _ToastOverlayBody({
    required this.kind,
    required this.title,
    this.subtitle,
  });

  final FeedbackToastKind kind;
  final String title;
  final String? subtitle;

  @override
  Widget build(BuildContext context) {
    final media = MediaQuery.of(context);
    final bottom = media.padding.bottom + AppSpacing.s24;

    return Material(
      color: Colors.transparent,
      child: Stack(
        children: [
          Positioned(
            left: AppSpacing.s24,
            right: AppSpacing.s24,
            bottom: bottom,
            child: Center(
              child: _FeedbackToastCard(
                kind: kind,
                title: title,
                subtitle: subtitle,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _FeedbackToastCard extends StatelessWidget {
  const _FeedbackToastCard({
    required this.kind,
    required this.title,
    this.subtitle,
  });

  final FeedbackToastKind kind;
  final String title;
  final String? subtitle;

  bool get _onDark => kind == FeedbackToastKind.success ||
      kind == FeedbackToastKind.failure;

  @override
  Widget build(BuildContext context) {
    final Widget inner = _buildSurface(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _leading(),
          SizedBox(width: AppSpacing.s12),
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: AppTypography.titleSmall(
                    color: _onDark ? AppColors.textInverse : AppColors.textPrimary,
                  ).copyWith(fontWeight: FontWeight.w600),
                ),
                if (subtitle != null && subtitle!.isNotEmpty) ...[
                  SizedBox(height: AppSpacing.s4),
                  Text(
                    subtitle!,
                    style: AppTypography.bodySmall(
                      color: _onDark
                          ? AppColors.textInverse.withValues(alpha: 0.85)
                          : AppColors.textSecondary,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );

    return ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 400),
      child: inner,
    );
  }

  Widget _buildSurface({required Widget child}) {
    final radius = BorderRadius.circular(AppRadius.pill);
    switch (kind) {
      case FeedbackToastKind.hintFrosted:
        if (kIsWeb) {
          return Container(
            padding: AppSpacing.cardPaddingCompact,
            decoration: BoxDecoration(
              color: AppColors.feedbackToastHintFrosted,
              borderRadius: radius,
              boxShadow: AppShadows.shadowFloating,
            ),
            child: child,
          );
        }
        return ClipRRect(
          borderRadius: radius,
          child: BackdropFilter(
            filter: ImageFilter.blur(sigmaX: 16, sigmaY: 16),
            child: Container(
              padding: AppSpacing.cardPaddingCompact,
              decoration: BoxDecoration(
                color: AppColors.feedbackToastHintFrosted.withValues(alpha: 0.88),
                borderRadius: radius,
                boxShadow: AppShadows.shadowFloating,
              ),
              child: child,
            ),
          ),
        );
      case FeedbackToastKind.success:
      case FeedbackToastKind.failure:
      case FeedbackToastKind.hintWarm:
        final bg = switch (kind) {
          FeedbackToastKind.success => AppColors.feedbackToastSuccess,
          FeedbackToastKind.failure => AppColors.feedbackToastFailure,
          _ => AppColors.feedbackToastHintWarm,
        };
        return Container(
          padding: AppSpacing.cardPaddingCompact,
          decoration: BoxDecoration(
            color: bg,
            borderRadius: radius,
            boxShadow: AppShadows.shadowFloating,
          ),
          child: child,
        );
    }
  }

  Widget _leading() {
    switch (kind) {
      case FeedbackToastKind.success:
        return Container(
          width: 28,
          height: 28,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: AppColors.textInverse, width: 1.5),
          ),
          child: const Icon(Icons.check, size: 16, color: AppColors.textInverse),
        );
      case FeedbackToastKind.failure:
        return Icon(Icons.info_outline, color: AppColors.textInverse, size: 24);
      case FeedbackToastKind.hintFrosted:
        return Icon(Icons.notifications_none, color: AppColors.textSecondary, size: 22);
      case FeedbackToastKind.hintWarm:
        return Icon(Icons.lightbulb_outline, color: AppColors.textPrimary, size: 22);
    }
  }
}

class _BannerOverlayBody extends StatelessWidget {
  const _BannerOverlayBody({
    required this.message,
    required this.detailLabel,
    this.onDetail,
    required this.onClose,
  });

  final String message;
  final String detailLabel;
  final VoidCallback? onDetail;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final top = MediaQuery.paddingOf(context).top + AppSpacing.s12;

    return Material(
      color: Colors.transparent,
      child: Stack(
        children: [
          Positioned(
            left: AppSpacing.s24,
            right: AppSpacing.s24,
            top: top,
            child: Material(
              color: AppColors.feedbackBannerBackground,
              elevation: 0,
              shadowColor: Colors.transparent,
              borderRadius: BorderRadius.circular(AppRadius.xl),
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(AppRadius.xl),
                  boxShadow: AppShadows.shadowCard,
                ),
                padding: AppSpacing.cardPaddingCompact,
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Icon(Icons.auto_awesome, size: 18, color: AppColors.textSecondary),
                    SizedBox(width: AppSpacing.s12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            message,
                            style: AppTypography.bodyLarge(color: AppColors.textPrimary),
                          ),
                          if (onDetail != null) ...[
                            SizedBox(height: AppSpacing.s8),
                            GestureDetector(
                              onTap: onDetail,
                              child: Text(
                                detailLabel,
                                style: AppTypography.bodyMedium(
                                  color: AppColors.textSecondary,
                                ).copyWith(
                                  decoration: TextDecoration.underline,
                                  decorationColor: AppColors.textTertiary,
                                ),
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 20),
                      color: AppColors.textTertiary,
                      onPressed: onClose,
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _FeedbackConfirmDialog extends StatelessWidget {
  const _FeedbackConfirmDialog({
    required this.title,
    required this.message,
    required this.cancelLabel,
    required this.confirmLabel,
    this.onConfirm,
    this.onCancel,
  });

  final String title;
  final String message;
  final String cancelLabel;
  final String confirmLabel;
  final VoidCallback? onConfirm;
  final VoidCallback? onCancel;

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: AppColors.bgCard,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppRadius.xl),
      ),
      elevation: 8,
      shadowColor: Colors.black26,
      child: Padding(
        padding: AppSpacing.cardPadding,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(
                    title,
                    style: AppTypography.titleMedium(color: AppColors.textPrimary),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close, size: 22),
                  color: AppColors.textTertiary,
                  onPressed: () {
                    onCancel?.call();
                    Navigator.of(context).pop();
                  },
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                ),
              ],
            ),
            SizedBox(height: AppSpacing.s12),
            Text(
              message,
              style: AppTypography.bodyMedium(color: AppColors.textSecondary),
            ),
            SizedBox(height: AppSpacing.s24),
            Row(
              children: [
                Expanded(
                  child: _DialogPillButton(
                    label: cancelLabel,
                    filled: false,
                    onPressed: () {
                      onCancel?.call();
                      Navigator.of(context).pop();
                    },
                  ),
                ),
                SizedBox(width: AppSpacing.s12),
                Expanded(
                  child: _DialogPillButton(
                    label: confirmLabel,
                    filled: true,
                    onPressed: () {
                      onConfirm?.call();
                      Navigator.of(context).pop();
                    },
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _DialogPillButton extends StatelessWidget {
  const _DialogPillButton({
    required this.label,
    required this.filled,
    required this.onPressed,
  });

  final String label;
  final bool filled;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    final bg = filled ? AppColors.feedbackToastSuccess : AppColors.bgSecondary;
    final fg = filled ? AppColors.textInverse : AppColors.textPrimary;
    return SizedBox(
      height: 48,
      child: Material(
        color: bg,
        borderRadius: BorderRadius.circular(AppRadius.pill),
        child: InkWell(
          onTap: onPressed,
          borderRadius: BorderRadius.circular(AppRadius.pill),
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
