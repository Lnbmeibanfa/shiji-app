import '../../../core/network/api_client.dart';
import '../../../core/network/models/latest_meal_record_response.dart';

class HomeRepository {
  HomeRepository(this._api);

  final ApiClient _api;

  Future<LatestMealRecordResponse?> getLatestMeal() {
    return _api.getJson<LatestMealRecordResponse?>(
      '/api/meal-records/latest',
      parseData: (raw) {
        if (raw == null) {
          return null;
        }
        return LatestMealRecordResponse.fromJson(raw as Map<String, dynamic>);
      },
    );
  }
}
