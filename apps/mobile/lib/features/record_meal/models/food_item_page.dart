/// 与后端 `FoodItemPageResponse` 对齐。
class FoodItemPage {
  FoodItemPage({
    required this.items,
    required this.hasNext,
    required this.page,
    required this.size,
  });

  final List<FoodItemSummary> items;
  final bool hasNext;
  final int page;
  final int size;

  factory FoodItemPage.fromJson(Map<String, dynamic> json) {
    final raw = json['items'];
    final list = raw is List
        ? raw
            .map((e) => FoodItemSummary.fromJson(e as Map<String, dynamic>))
            .toList()
        : <FoodItemSummary>[];
    return FoodItemPage(
      items: list,
      hasNext: json['hasNext'] as bool? ?? false,
      page: (json['page'] as num?)?.toInt() ?? 0,
      size: (json['size'] as num?)?.toInt() ?? 20,
    );
  }
}

class FoodItemSummary {
  FoodItemSummary({
    required this.id,
    required this.foodName,
    this.caloriesPer100g,
  });

  final int id;
  final String foodName;
  final double? caloriesPer100g;

  factory FoodItemSummary.fromJson(Map<String, dynamic> json) {
    final cal = json['caloriesPer100g'];
    return FoodItemSummary(
      id: (json['id'] as num).toInt(),
      foodName: json['foodName'] as String,
      caloriesPer100g: cal == null ? null : (cal as num).toDouble(),
    );
  }
}
