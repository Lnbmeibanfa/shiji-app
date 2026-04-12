/// 记录页面临时食物行（保存时映射为后端 `meal_food_item`）。
class DraftFoodItem {
  DraftFoodItem({
    required this.name,
    this.estimatedCalories,
  });

  final String name;
  final double? estimatedCalories;
}
