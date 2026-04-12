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
}
