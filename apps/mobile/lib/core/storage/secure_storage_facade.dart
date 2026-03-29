import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// 对 `flutter_secure_storage` 的薄封装，便于单测替换。
class SecureStorageFacade {
  SecureStorageFacade({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  Future<String?> read(String key) => _storage.read(key: key);

  Future<void> write(String key, String value) =>
      _storage.write(key: key, value: value);

  Future<void> delete(String key) => _storage.delete(key: key);
}
