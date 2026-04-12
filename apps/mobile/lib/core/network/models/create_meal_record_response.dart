class CreateMealRecordResponse {
  CreateMealRecordResponse({required this.mealRecordId});

  factory CreateMealRecordResponse.fromJson(Map<String, dynamic> json) {
    return CreateMealRecordResponse(
      mealRecordId: (json['mealRecordId'] as num).toInt(),
    );
  }

  final int mealRecordId;
}
