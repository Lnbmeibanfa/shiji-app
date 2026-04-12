/// 与 `services/api` 种子数据顺序一致时的典型主键（开发/联调用）。
/// 若本地库未跑种子，保存可能因 `foodItemId` 不存在而失败。
abstract final class AiStubFoods {
  static const List<({int id, String name, double kcalPer100g})> rows = [
    (id: 1, name: '白米饭', kcalPer100g: 116),
    (id: 7, name: '鸡胸肉', kcalPer100g: 165),
    (id: 8, name: '牛肉', kcalPer100g: 220),
  ];
}
