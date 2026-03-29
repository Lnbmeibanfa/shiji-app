/// 登录页手机号、验证码校验（与后端约束对齐）。
abstract final class PhoneLoginValidators {
  static final RegExp phonePattern = RegExp(r'^1\d{10}$');

  /// 后端要求 4–8 位数字。
  static final RegExp smsCodePattern = RegExp(r'^\d{4,8}$');

  static bool isValidPhone(String raw) => phonePattern.hasMatch(raw.trim());

  static bool isValidSmsCode(String raw) =>
      smsCodePattern.hasMatch(raw.trim());
}
