import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants/app_assets.dart';
import '../../../core/feedback/app_feedback.dart';
import '../../../core/network/api_exceptions.dart';
import '../../../core/providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/shiji_button.dart';
import '../constants/agreement_constants.dart';
import '../validators/phone_login_validators.dart';

/// 短信验证码登录页（接 [AuthRepository] / [AuthController]）。
class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  final _phoneController = TextEditingController();
  final _codeController = TextEditingController();

  late final TapGestureRecognizer _userAgreementTap;
  late final TapGestureRecognizer _privacyPolicyTap;

  bool _agreed = false;
  bool _sendingCode = false;
  bool _loggingIn = false;
  int _countdownSec = 0;
  Timer? _countdownTimer;

  @override
  void initState() {
    super.initState();
    _userAgreementTap = TapGestureRecognizer()..onTap = _onUserAgreementLinkTap;
    _privacyPolicyTap = TapGestureRecognizer()..onTap = _onPrivacyPolicyLinkTap;
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    _userAgreementTap.dispose();
    _privacyPolicyTap.dispose();
    _phoneController.dispose();
    _codeController.dispose();
    super.dispose();
  }

  void _onUserAgreementLinkTap() {
    if (!mounted) {
      return;
    }
    AppFeedback.showToast(
      context,
      kind: FeedbackToastKind.hintFrosted,
      title: '《用户协议》',
      subtitle: '正文待接入',
    );
  }

  void _onPrivacyPolicyLinkTap() {
    if (!mounted) {
      return;
    }
    AppFeedback.showToast(
      context,
      kind: FeedbackToastKind.hintFrosted,
      title: '《隐私政策》',
      subtitle: '正文待接入',
    );
  }

  InputDecoration _fieldDecoration(String hint) {
    final border = OutlineInputBorder(
      borderRadius: BorderRadius.circular(AppRadius.sm),
      borderSide: const BorderSide(color: AppColors.borderLight),
    );
    return InputDecoration(
      hintText: hint,
      hintStyle: AppTypography.bodyMedium(color: AppColors.textTertiary),
      filled: true,
      fillColor: AppColors.bgCard,
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.s16,
        vertical: AppSpacing.s16,
      ),
      enabledBorder: border,
      focusedBorder: border.copyWith(
        borderSide: const BorderSide(color: AppColors.primary, width: 1.5),
      ),
      border: border,
    );
  }

  String _formatError(Object e) {
    if (e is ApiBusinessException) {
      return e.message;
    }
    if (e is ApiHttpException) {
      return e.message ?? '网络异常，请稍后重试';
    }
    return '操作失败，请稍后重试';
  }

  bool get _phoneOk => PhoneLoginValidators.isValidPhone(_phoneController.text);

  bool get _codeOk => PhoneLoginValidators.isValidSmsCode(_codeController.text);

  bool get _canRequestCode => !_sendingCode && _countdownSec <= 0 && _phoneOk;

  Future<void> _onRequestCode() async {
    if (!_phoneOk) {
      AppFeedback.showToast(
        context,
        kind: FeedbackToastKind.failure,
        title: '手机号格式有误',
        subtitle: '请输入正确的 11 位手机号',
      );
      return;
    }
    setState(() => _sendingCode = true);
    try {
      await ref
          .read(authRepositoryProvider)
          .sendSmsCode(phone: _phoneController.text.trim());
      if (!mounted) {
        return;
      }
      _startCountdown(60);
      AppFeedback.showToast(
        context,
        kind: FeedbackToastKind.success,
        title: '验证码已发送',
        subtitle: '开发环境请查看后端日志',
      );
    } catch (e) {
      if (mounted) {
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: '发送失败',
          subtitle: _formatError(e),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _sendingCode = false);
      }
    }
  }

  void _startCountdown(int seconds) {
    _countdownTimer?.cancel();
    setState(() => _countdownSec = seconds);
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (t) {
      if (!mounted) {
        t.cancel();
        return;
      }
      if (_countdownSec <= 1) {
        t.cancel();
        setState(() => _countdownSec = 0);
        return;
      }
      setState(() => _countdownSec -= 1);
    });
  }

  Future<void> _onLogin() async {
    if (!_agreed) {
      AppFeedback.showToast(
        context,
        kind: FeedbackToastKind.failure,
        title: '请先同意协议',
        subtitle: '阅读并勾选用户协议与隐私政策',
      );
      return;
    }
    if (!_phoneOk) {
      AppFeedback.showToast(
        context,
        kind: FeedbackToastKind.failure,
        title: '手机号格式有误',
        subtitle: '请输入正确的 11 位手机号',
      );
      return;
    }
    if (!_codeOk) {
      AppFeedback.showToast(
        context,
        kind: FeedbackToastKind.failure,
        title: '验证码格式有误',
        subtitle: '请输入 4–8 位数字验证码',
      );
      return;
    }
    setState(() => _loggingIn = true);
    try {
      final login = await ref
          .read(authRepositoryProvider)
          .loginWithSmsCode(
            phone: _phoneController.text.trim(),
            code: _codeController.text.trim(),
            agreements: AgreementConstants.buildAcceptedList(),
          );
      await ref.read(authControllerProvider).setSessionToken(login.token);
    } catch (e) {
      if (mounted) {
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: '登录失败',
          subtitle: _formatError(e),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _loggingIn = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final linkStyle = AppTypography.bodyMedium(color: AppColors.primary);
    final bodyStyle = AppTypography.bodyMedium(color: AppColors.textSecondary);

    return Scaffold(
      backgroundColor: AppColors.bgPrimary,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: AppSpacing.pageHorizontal.copyWith(
            top: AppSpacing.s32,
            bottom: AppSpacing.s40,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: SizedBox(
                  width: 88,
                  height: 88,
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(AppRadius.sm),
                    child: Image.asset(
                      AppAssets.loginLogo,
                      fit: BoxFit.contain,
                      alignment: Alignment.center,
                      filterQuality: FilterQuality.high,
                      errorBuilder: (context, error, stackTrace) {
                        return ColoredBox(
                          color: AppColors.bgMuted,
                          child: Center(
                            child: Text(
                              '食迹',
                              style: AppTypography.titleSmall(
                                color: AppColors.textTertiary,
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ),
              ),
              SizedBox(height: AppSpacing.s24),
              Text(
                '欢迎来到食迹',
                textAlign: TextAlign.center,
                style: AppTypography.displayLarge(),
              ),
              SizedBox(height: AppSpacing.s12),
              Text(
                '记录每一餐，看见自己的饮食轨迹',
                textAlign: TextAlign.center,
                style: AppTypography.bodyLarge(color: AppColors.textSecondary),
              ),
              SizedBox(height: AppSpacing.s40),
              Text(
                '手机号',
                style: AppTypography.labelMedium(
                  color: AppColors.textSecondary,
                ),
              ),
              SizedBox(height: AppSpacing.s8),
              TextField(
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                maxLength: 11,
                onChanged: (_) => setState(() {}),
                inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                style: AppTypography.bodyLarge(),
                decoration: _fieldDecoration(
                  '请输入手机号',
                ).copyWith(counterText: ''),
              ),
              SizedBox(height: AppSpacing.s20),
              Text(
                '验证码',
                style: AppTypography.labelMedium(
                  color: AppColors.textSecondary,
                ),
              ),
              SizedBox(height: AppSpacing.s8),
              Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Expanded(
                    child: TextField(
                      controller: _codeController,
                      keyboardType: TextInputType.number,
                      maxLength: 8,
                      onChanged: (_) => setState(() {}),
                      inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                      style: AppTypography.bodyLarge(),
                      decoration: _fieldDecoration(
                        '请输入验证码',
                      ).copyWith(counterText: ''),
                    ),
                  ),
                  SizedBox(width: AppSpacing.s12),
                  TextButton(
                    onPressed: _canRequestCode ? _onRequestCode : null,
                    style: TextButton.styleFrom(
                      foregroundColor: AppColors.primary,
                      backgroundColor: AppColors.bgMuted,
                      disabledForegroundColor: AppColors.textTertiary,
                      disabledBackgroundColor: AppColors.bgSecondary,
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.s16,
                        vertical: AppSpacing.s12,
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppRadius.sm),
                      ),
                    ),
                    child: _sendingCode
                        ? SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: AppColors.primary,
                            ),
                          )
                        : Text(
                            _countdownSec > 0 ? '${_countdownSec}s' : '获取验证码',
                            style: AppTypography.labelMedium(
                              color: AppColors.primary,
                            ),
                          ),
                  ),
                ],
              ),
              SizedBox(height: AppSpacing.s24),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Checkbox(
                    value: _agreed,
                    onChanged: (v) => setState(() => _agreed = v ?? false),
                    side: const BorderSide(color: AppColors.borderLight),
                    fillColor: WidgetStateProperty.resolveWith((states) {
                      if (states.contains(WidgetState.selected)) {
                        return AppColors.primary;
                      }
                      return null;
                    }),
                  ),
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.only(top: AppSpacing.s12),
                      child: Text.rich(
                        TextSpan(
                          style: bodyStyle,
                          children: [
                            const TextSpan(text: '我已阅读并同意'),
                            TextSpan(
                              text: '《用户协议》',
                              style: linkStyle,
                              recognizer: _userAgreementTap,
                            ),
                            const TextSpan(text: '和'),
                            TextSpan(
                              text: '《隐私政策》',
                              style: linkStyle,
                              recognizer: _privacyPolicyTap,
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              SizedBox(height: AppSpacing.s32),
              ShijiButton(
                label: '登录',
                isLoading: _loggingIn,
                onPressed: (_agreed && _phoneOk && _codeOk && !_loggingIn)
                    ? _onLogin
                    : null,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
