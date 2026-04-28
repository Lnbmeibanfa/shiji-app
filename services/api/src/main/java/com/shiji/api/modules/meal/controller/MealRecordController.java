package com.shiji.api.modules.meal.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.meal.model.dto.request.CreateMealRecordRequest;
import com.shiji.api.modules.meal.model.dto.response.CreateMealRecordResponse;
import com.shiji.api.modules.meal.model.dto.response.LatestMealRecordResponse;
import com.shiji.api.modules.meal.service.MealRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal-records")
public class MealRecordController {

    private final MealRecordService mealRecordService;

    @GetMapping("/latest")
    public ApiResponse<LatestMealRecordResponse> latest(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.success(mealRecordService.getLatestMeal(principal.userId()));
    }

    @PostMapping
    public ApiResponse<CreateMealRecordResponse> create(
            @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody CreateMealRecordRequest request) {
        return ApiResponse.success(mealRecordService.create(principal.userId(), request));
    }
}
