import 'secure_storage_facade.dart';

/// 访问令牌唯一持久化出口（业务代码禁止直接操作 secure storage key）。
class AuthStorage {
  AuthStorage(this._secure);

  static const _tokenKey = 'auth_token';

  final SecureStorageFacade _secure;

  Future<String?> readToken() => _secure.read(_tokenKey);

  Future<void> writeToken(String token) => _secure.write(_tokenKey, token);

  Future<void> clear() => _secure.delete(_tokenKey);
}
