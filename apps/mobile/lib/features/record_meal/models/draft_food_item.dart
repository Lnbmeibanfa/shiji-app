import 'food_item_page.dart';

/// 记录页面临时食物行（保存时映射为后端 `meal_food_item`）。
class DraftFoodItem {
  DraftFoodItem({
    required this.name,
    this.foodItemId,
    this.kcalPer100g,
    this.weightG = 100,
    required this.recognitionSource,
    this.fromAi = false,
  });

  final int? foodItemId;
  final String name;
  final double? kcalPer100g;
  final double weightG;
  final String recognitionSource;
  final bool fromAi;

  double? get estimatedCalories {
    final k = kcalPer100g;
    if (k == null) return null;
    return k * weightG / 100;
  }

  DraftFoodItem copyWith({double? weightG}) {
    return DraftFoodItem(
      name: name,
      foodItemId: foodItemId,
      kcalPer100g: kcalPer100g,
      weightG: weightG ?? this.weightG,
      recognitionSource: recognitionSource,
      fromAi: fromAi,
    );
  }

  factory DraftFoodItem.fromSearch(FoodItemSummary s) {
    return DraftFoodItem(
      name: s.foodName,
      foodItemId: s.id,
      kcalPer100g: s.caloriesPer100g,
      weightG: 100,
      recognitionSource: 'user_manual',
      fromAi: false,
    );
  }

  factory DraftFoodItem.aiStub({
    required int foodItemId,
    required String name,
    required double kcalPer100g,
  }) {
    return DraftFoodItem(
      name: name,
      foodItemId: foodItemId,
      kcalPer100g: kcalPer100g,
      weightG: 100,
      recognitionSource: 'ai',
      fromAi: true,
    );
  }

  /// 异步视觉识别返回的食材 ID（名称暂用占位，热量未知时可后续搜索替换）。
  factory DraftFoodItem.aiFromIngredientId(int foodItemId) {
    return DraftFoodItem(
      name: '食物 #$foodItemId',
      foodItemId: foodItemId,
      kcalPer100g: null,
      weightG: 100,
      recognitionSource: 'ai',
      fromAi: true,
    );
  }

  /// 使用视觉接口返回的展示字段构建草稿行（优先 `foodName` / `caloriesPer100g`）。
  factory DraftFoodItem.aiFromVision({
    required int foodItemId,
    required String foodName,
    double? caloriesPer100g,
    double weightG = 100,
  }) {
    final name =
        foodName.trim().isNotEmpty ? foodName.trim() : '食物 #$foodItemId';
    return DraftFoodItem(
      name: name,
      foodItemId: foodItemId,
      kcalPer100g: caloriesPer100g,
      weightG: weightG,
      recognitionSource: 'ai',
      fromAi: true,
    );
  }
}
