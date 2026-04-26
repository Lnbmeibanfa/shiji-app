import '../../../core/network/api_client.dart';
import '../../../core/network/models/meal_photo_recognition_models.dart';

class MealPhotoRecognitionRepository {
  MealPhotoRecognitionRepository(this._api);

  final ApiClient _api;

  Future<MealPhotoRecognitionCreateResult> createTask(int fileId) {
    return _api.postJson<MealPhotoRecognitionCreateResult>(
      '/api/ai/meal-photo/recognitions',
      body: <String, dynamic>{'fileId': fileId},
      parseData: (raw) => MealPhotoRecognitionCreateResult.fromJson(
        raw! as Map<String, dynamic>,
      ),
    );
  }

  Future<MealPhotoRecognitionPollResult> poll(String taskId) {
    return _api.getJson<MealPhotoRecognitionPollResult>(
      '/api/ai/meal-photo/recognitions/$taskId',
      parseData: (raw) => MealPhotoRecognitionPollResult.fromJson(
        raw! as Map<String, dynamic>,
      ),
    );
  }
}
