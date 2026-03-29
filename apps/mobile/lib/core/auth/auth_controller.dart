import 'package:flutter/foundation.dart';

import '../storage/auth_storage.dart';

/// 全局登录态：与 [GoRouter] 的 `refreshListenable` 联动。
class AuthController extends ChangeNotifier {
  AuthController({required AuthStorage storage}) : _storage = storage;

  final AuthStorage _storage;

  String? _token;
  bool _ready = false;

  bool get isReady => _ready;

  bool get isAuthenticated =>
      _token != null && _token!.isNotEmpty;

  String? get token => _token;

  /// 启动时从安全存储恢复会话。
  Future<void> hydrate() async {
    _token = await _storage.readToken();
    _ready = true;
    notifyListeners();
  }

  Future<void> setSessionToken(String token) async {
    await _storage.writeToken(token);
    _token = token;
    notifyListeners();
  }

  Future<void> signOut() async {
    await _storage.clear();
    _token = null;
    notifyListeners();
  }
}
