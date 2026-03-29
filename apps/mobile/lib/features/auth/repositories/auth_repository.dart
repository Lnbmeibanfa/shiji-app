import '../../../core/network/api_client.dart';
import '../models/auth_models.dart';

/// 认证 API：页面与 Widget 仅通过本类访问网络，禁止直接 import `dio`。
class AuthRepository {
  AuthRepository(this._api);

  final ApiClient _api;

  static const _sendSms = '/api/auth/sms/send';
  static const _loginSms = '/api/auth/login/sms';
  static const _logoutAll = '/api/auth/logout/all';

  Future<void> sendSmsCode({
    required String phone,
    String? deviceId,
  }) {
    return _api.postVoid(
      _sendSms,
      body: <String, dynamic>{
        'phone': phone,
        'deviceId': ?deviceId,
      },
    );
  }

  /// [agreements] 须与 UI 勾选一致，禁止硬编码为已同意。
  Future<LoginResponse> loginWithSmsCode({
    required String phone,
    required String code,
    required List<AgreementAcceptance> agreements,
    String? deviceId,
  }) {
    return _api.postJson<LoginResponse>(
      _loginSms,
      body: <String, dynamic>{
        'phone': phone,
        'code': code,
        'agreements': agreements.map((e) => e.toJson()).toList(),
        'deviceId': ?deviceId,
      },
      parseData: (raw) =>
          LoginResponse.fromJson(raw! as Map<String, dynamic>),
    );
  }

  Future<void> logoutAll() {
    return _api.postVoid(_logoutAll);
  }
}
