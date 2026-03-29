import 'package:shared_preferences/shared_preferences.dart';

/// 对 `shared_preferences` 的薄封装。
class PrefsFacade {
  PrefsFacade(this._prefs);

  final SharedPreferences _prefs;

  static Future<PrefsFacade> create() async {
    final p = await SharedPreferences.getInstance();
    return PrefsFacade(p);
  }

  bool? getBool(String key) => _prefs.getBool(key);

  Future<void> setBool(String key, bool value) => _prefs.setBool(key, value);
}
