import '../models/auth_models.dart';

/// 与 `services/api/docs/auth-api.md` 中 curl 示例一致。
abstract final class AgreementConstants {
  static const String userAgreementType = 'USER_AGREEMENT';
  static const String privacyPolicyType = 'PRIVACY_POLICY';
  static const String currentVersion = 'v1';

  /// 用户已勾选协议时用于登录请求（两项均须 accepted: true）。
  static List<AgreementAcceptance> buildAcceptedList() {
    return <AgreementAcceptance>[
      AgreementAcceptance(
        agreementType: userAgreementType,
        agreementVersion: currentVersion,
        accepted: true,
      ),
      AgreementAcceptance(
        agreementType: privacyPolicyType,
        agreementVersion: currentVersion,
        accepted: true,
      ),
    ];
  }
}
