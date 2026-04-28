package com.shiji.api.modules.meal.service;

import com.shiji.api.modules.meal.model.dto.request.CreateMealRecordRequest;
import com.shiji.api.modules.meal.model.dto.response.CreateMealRecordResponse;
import com.shiji.api.modules.meal.model.dto.response.LatestMealRecordResponse;

public interface MealRecordService {

    CreateMealRecordResponse create(long userId, CreateMealRecordRequest request);

    LatestMealRecordResponse getLatestMeal(long userId);
}
