import 'package:flutter/material.dart';

/// 食迹 Design Token v1 — 颜色（唯一色源，业务组件禁止另行写 hex）。
abstract final class AppColors {
  static const Color primary = Color(0xFF9AAF99);
  static const Color primaryPressed = Color(0xFF8EA38D);
  static const Color primarySoft = Color(0xFFDCE7DC);
  static const Color primarySoftest = Color(0xFFEEF4EE);

  /// 底部导航 Tab 选中态（图标、标签、指示底）。
  static const Color navTabSelected = Color(0xFF8FA693);

  static const Color bgPrimary = Color(0xFFF6F7F4);
  static const Color bgCard = Color(0xFFFCFCFA);
  static const Color bgSecondary = Color(0xFFF0F2EE);
  static const Color bgMuted = Color(0xFFE9ECE7);

  static const Color textPrimary = Color(0xFF2F2A24);
  static const Color textSecondary = Color(0xFF6F685F);
  static const Color textTertiary = Color(0xFFA19A90);
  static const Color textInverse = Color(0xFFFFFFFF);

  static const Color borderLight = Color(0xFFE6E9E3);
  static const Color divider = Color(0xFFECEFE9);

  static const Color success = Color(0xFF8DAA8C);
  static const Color warning = Color(0xFFE2B48F);
  static const Color accentWarm = Color(0xFFF3E3CC);
  static const Color accentWarmInner = Color(0xFFFBF4EA);
  static const Color dangerSoft = Color(0xFFD9A8A0);

  static const Color tagGreenBg = Color(0xFFEEF5EE);
  static const Color tagGreenText = Color(0xFF7B9A7B);
  static const Color tagOrangeBg = Color(0xFFFBF0E5);
  static const Color tagOrangeText = Color(0xFFD49A6A);
  static const Color tagYellowBg = Color(0xFFF8F3E3);
  static const Color tagYellowText = Color(0xFFB89B4D);
  static const Color tagNeutralBg = Color(0xFFF1F2EF);
  static const Color tagNeutralText = Color(0xFF7A746C);

  /// Primary 按钮禁用态（规范定稿，待后续可收拢为独立 token 名）。
  static const Color buttonDisabledBackground = Color(0xFFC9D3C8);
  static const Color buttonDisabledForeground = Color(0xFFF7F8F6);

  /// 热量进度条轨道色（规范定稿）。
  static const Color progressTrack = Color(0xFFD9DED6);

  /// 已摄入达到或超过日目标时，进度条前景色（约定 A：满条 + 红色警示）。
  static const Color progressOverBudget = Color(0xFFC75C5C);

  /// AI 建议卡 icon 区背景（规范定稿）。
  static const Color aiInsightIconBackground = Color(0xFFF6E8D7);

  /// 拍照主卡 icon 圆底：白 20%（规范 rgba(255,255,255,0.2)）。
  static const Color captureCardIconBackdrop = Color(0x33FFFFFF);

  // --- 反馈表面（Toast / Banner / 确认弹窗主按钮），见 openspec client-feedback ---

  /// Toast 成功、确认 Dialog 主操作按钮背景。
  static const Color feedbackToastSuccess = Color(0xFF95AB99);

  /// Toast 失败（温和暖调，非刺红）。
  static const Color feedbackToastFailure = Color(0xFFE99B76);

  /// Toast 提示：浅底（可与毛玻璃叠加）。
  static const Color feedbackToastHintFrosted = Color(0xFFFAFAF9);

  /// Toast 提示：暖色实底。
  static const Color feedbackToastHintWarm = Color(0xFFE9BC9C);

  /// 顶区公告 Banner 背景。
  static const Color feedbackBannerBackground = Color(0xFFFFF4E6);
}
