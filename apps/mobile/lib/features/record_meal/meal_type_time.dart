import 'widgets/meal_type_selector.dart';

/// 左闭右开：`[05:00,10:00)` 早餐、`[10:00,15:00)` 午餐、`[15:00,20:00)` 晚餐，其余为夜宵（`snack`）。
String mealTypeForNow() {
  final n = DateTime.now();
  final minutes = n.hour * 60 + n.minute;
  if (minutes >= 5 * 60 && minutes < 10 * 60) {
    return MealTypes.breakfast;
  }
  if (minutes >= 10 * 60 && minutes < 15 * 60) {
    return MealTypes.lunch;
  }
  if (minutes >= 15 * 60 && minutes < 20 * 60) {
    return MealTypes.dinner;
  }
  return MealTypes.snack;
}
