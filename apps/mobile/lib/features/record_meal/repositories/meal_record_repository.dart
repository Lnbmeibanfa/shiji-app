import '../../../core/network/api_client.dart';
import '../../../core/network/models/create_meal_record_response.dart';

class MealRecordRepository {
  MealRecordRepository(this._api);

  final ApiClient _api;

  Future<CreateMealRecordResponse> createMealRecord(Map<String, dynamic> body) {
    return _api.postJson<CreateMealRecordResponse>(
      '/api/meal-records',
      body: body,
      parseData: (raw) =>
          CreateMealRecordResponse.fromJson(raw! as Map<String, dynamic>),
    );
  }
}
