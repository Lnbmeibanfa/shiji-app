// 餐图异步识别任务 API 模型（与 MealPhotoRecognitionTaskPollResponse / DishIngredientVisionResponse 对齐）。

String _visionStringId(Object? raw) {
  if (raw is String) return raw;
  if (raw is int) return '$raw';
  if (raw is num) return raw.toInt().toString();
  return '';
}

class MealPhotoRecognitionCreateResult {
  MealPhotoRecognitionCreateResult({
    required this.taskId,
    required this.status,
  });

  final String taskId;
  final String status;

  factory MealPhotoRecognitionCreateResult.fromJson(Map<String, dynamic> json) {
    return MealPhotoRecognitionCreateResult(
      taskId: json['taskId'] as String,
      status: json['status'] as String,
    );
  }
}

class MealPhotoRecognitionPollResult {
  MealPhotoRecognitionPollResult({
    required this.taskId,
    required this.status,
    this.result,
    this.errorCode,
    this.errorMessage,
  });

  final String taskId;
  final String status;
  final DishIngredientVisionData? result;
  final int? errorCode;
  final String? errorMessage;

  factory MealPhotoRecognitionPollResult.fromJson(Map<String, dynamic> json) {
    final rawResult = json['result'];
    return MealPhotoRecognitionPollResult(
      taskId: json['taskId'] as String,
      status: json['status'] as String,
      result: rawResult is Map<String, dynamic>
          ? DishIngredientVisionData.fromJson(rawResult)
          : null,
      errorCode: json['errorCode'] as int?,
      errorMessage: json['errorMessage'] as String?,
    );
  }
}

class DishIngredientVisionData {
  DishIngredientVisionData({
    required this.requestId,
    required this.recognition,
  });

  final String requestId;
  final VisionRecognition recognition;

  factory DishIngredientVisionData.fromJson(Map<String, dynamic> json) {
    return DishIngredientVisionData(
      requestId: json['requestId'] as String,
      recognition: VisionRecognition.fromJson(
        json['recognition'] as Map<String, dynamic>,
      ),
    );
  }
}

class VisionRecognition {
  VisionRecognition({
    this.schemaVersion,
    this.dish,
    this.ingredients,
    this.dishRejection,
    this.modelMeta,
  });

  final String? schemaVersion;
  final VisionDish? dish;
  final List<VisionIngredient>? ingredients;
  final VisionDishRejection? dishRejection;
  final Map<String, dynamic>? modelMeta;

  factory VisionRecognition.fromJson(Map<String, dynamic> json) {
    final ing = json['ingredients'];
    return VisionRecognition(
      schemaVersion: json['schemaVersion'] as String?,
      dish: json['dish'] is Map<String, dynamic>
          ? VisionDish.fromJson(json['dish'] as Map<String, dynamic>)
          : null,
      ingredients: ing is List<dynamic>
          ? ing
              .whereType<Map<String, dynamic>>()
              .map(VisionIngredient.fromJson)
              .toList()
          : null,
      dishRejection: json['dishRejection'] is Map<String, dynamic>
          ? VisionDishRejection.fromJson(
              json['dishRejection'] as Map<String, dynamic>,
            )
          : null,
      modelMeta: json['modelMeta'] is Map<String, dynamic>
          ? Map<String, dynamic>.from(json['modelMeta'] as Map)
          : null,
    );
  }
}

class VisionDish {
  VisionDish({
    required this.dishId,
    required this.confidence,
    this.match,
  });

  final String dishId;
  final double confidence;
  final VisionDishMatch? match;

  factory VisionDish.fromJson(Map<String, dynamic> json) {
    return VisionDish(
      dishId: _visionStringId(json['dishId']),
      confidence: (json['confidence'] as num).toDouble(),
      match: json['match'] is Map<String, dynamic>
          ? VisionDishMatch.fromJson(json['match'] as Map<String, dynamic>)
          : null,
    );
  }
}

class VisionDishMatch {
  VisionDishMatch({this.via, this.aliasId});

  final String? via;
  final String? aliasId;

  factory VisionDishMatch.fromJson(Map<String, dynamic> json) {
    final rawAlias = json['aliasId'];
    return VisionDishMatch(
      via: json['via'] as String?,
      aliasId: rawAlias == null ? null : _visionStringId(rawAlias),
    );
  }
}

class VisionIngredient {
  VisionIngredient({
    required this.ingredientId,
    required this.confidence,
    required this.foodName,
    required this.defaultUnit,
    this.caloriesPer100g,
  });

  final String ingredientId;
  final double confidence;
  final String foodName;
  final String defaultUnit;
  final double? caloriesPer100g;

  factory VisionIngredient.fromJson(Map<String, dynamic> json) {
    final kcal = json['caloriesPer100g'];
    return VisionIngredient(
      ingredientId: _visionStringId(json['ingredientId']),
      confidence: (json['confidence'] as num).toDouble(),
      foodName: (json['foodName'] as String?)?.trim() ?? '',
      defaultUnit: (json['defaultUnit'] as String?)?.trim().isNotEmpty == true
          ? (json['defaultUnit'] as String).trim()
          : 'g',
      caloriesPer100g: kcal is num ? kcal.toDouble() : null,
    );
  }
}

class VisionDishRejection {
  VisionDishRejection({this.code, this.detail});

  final String? code;
  final String? detail;

  factory VisionDishRejection.fromJson(Map<String, dynamic> json) {
    return VisionDishRejection(
      code: json['code'] as String?,
      detail: json['detail'] as String?,
    );
  }
}
