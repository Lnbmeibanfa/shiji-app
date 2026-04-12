import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/models/file_upload_response.dart';

class MealPhotoRepository {
  MealPhotoRepository(this._api);

  final ApiClient _api;

  Future<FileUploadResponse> uploadMealPhoto(XFile file) async {
    final bytes = await file.readAsBytes();
    return _api.uploadMealPhotoBytes(
      bytes: bytes,
      filename: file.name,
    );
  }
}
