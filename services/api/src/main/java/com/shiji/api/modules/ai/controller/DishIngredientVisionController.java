package com.shiji.api.modules.ai.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.ai.model.dto.request.DishIngredientVisionRequest;
import com.shiji.api.modules.ai.model.dto.response.DishIngredientVisionResponse;
import com.shiji.api.modules.ai.service.DishIngredientVisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/dashscope/vision")
public class DishIngredientVisionController {

    private final DishIngredientVisionService visionService;

    @PostMapping("/dish-ingredients")
    public ApiResponse<DishIngredientVisionResponse> recognize(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody DishIngredientVisionRequest request) {
        if (principal == null) {
            throw new IllegalStateException("expected authenticated user");
        }
        return ApiResponse.success(visionService.recognize(request.getImageBase64()));
    }
}
