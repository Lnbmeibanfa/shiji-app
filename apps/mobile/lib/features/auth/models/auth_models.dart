/// 与后端 `LoginResponse` 对齐。
class LoginResponse {
  LoginResponse({
    required this.userId,
    required this.token,
    required this.expireAt,
    required this.newUser,
  });

  final int userId;
  final String token;
  final DateTime expireAt;
  final bool newUser;

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      userId: (json['userId'] as num).toInt(),
      token: json['token'] as String,
      expireAt: DateTime.parse(json['expireAt'] as String),
      newUser: json['newUser'] as bool,
    );
  }
}

/// 登录请求中的单条协议（与后端 `AgreementAcceptanceDto` 一致）。
class AgreementAcceptance {
  AgreementAcceptance({
    required this.agreementType,
    required this.agreementVersion,
    required this.accepted,
  });

  final String agreementType;
  final String agreementVersion;
  final bool accepted;

  Map<String, dynamic> toJson() => <String, dynamic>{
        'agreementType': agreementType,
        'agreementVersion': agreementVersion,
        'accepted': accepted,
      };
}
