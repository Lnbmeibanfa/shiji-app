package com.shiji.api.modules.meal.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.modules.meal.model.dto.response.FoodItemPageResponse;
import com.shiji.api.modules.meal.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/food-items")
public class FoodItemController {

    private final FoodItemService foodItemService;

    @GetMapping
    public ApiResponse<FoodItemPageResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(foodItemService.search(q, page, size));
    }
}
