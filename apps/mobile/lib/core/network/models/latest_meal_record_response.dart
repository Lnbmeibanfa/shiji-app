class LatestMealRecordResponse {
  LatestMealRecordResponse({
    required this.mealType,
    required this.recordedAt,
    required this.totalEstimatedCalories,
    required this.mood,
  });

  factory LatestMealRecordResponse.fromJson(Map<String, dynamic> json) {
    return LatestMealRecordResponse(
      mealType: (json['mealType'] as String?) ?? '',
      recordedAt: DateTime.parse(json['recordedAt'] as String),
      totalEstimatedCalories: (json['totalEstimatedCalories'] as num?)?.toDouble(),
      mood: json['mood'] is Map<String, dynamic>
          ? LatestMealMood.fromJson(json['mood'] as Map<String, dynamic>)
          : null,
    );
  }

  final String mealType;
  final DateTime recordedAt;
  final double? totalEstimatedCalories;
  final LatestMealMood? mood;
}

class LatestMealMood {
  LatestMealMood({
    required this.emotionCode,
    required this.emotionName,
  });

  factory LatestMealMood.fromJson(Map<String, dynamic> json) {
    return LatestMealMood(
      emotionCode: (json['emotionCode'] as String?) ?? '',
      emotionName: (json['emotionName'] as String?) ?? '',
    );
  }

  final String emotionCode;
  final String emotionName;
}
