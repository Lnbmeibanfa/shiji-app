import '../../../core/network/api_client.dart';
import '../models/food_item_page.dart';

class FoodItemRepository {
  FoodItemRepository(this._api);

  final ApiClient _api;

  Future<FoodItemPage> search({
    String? q,
    required int page,
    required int size,
  }) {
    return _api.getJson<FoodItemPage>(
      '/api/food-items',
      queryParameters: <String, dynamic>{
        if (q != null && q.isNotEmpty) 'q': q,
        'page': page,
        'size': size,
      },
      parseData: (raw) =>
          FoodItemPage.fromJson(raw! as Map<String, dynamic>),
    );
  }
}
