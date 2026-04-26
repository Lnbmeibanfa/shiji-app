package com.shiji.api.modules.ai.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.ai.model.dto.request.CreateMealPhotoRecognitionTaskRequest;
import com.shiji.api.modules.ai.model.dto.response.CreateMealPhotoRecognitionTaskResponse;
import com.shiji.api.modules.ai.model.dto.response.MealPhotoRecognitionTaskPollResponse;
import com.shiji.api.modules.ai.service.RecognitionTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/meal-photo")
public class MealPhotoRecognitionTaskController {

    private final RecognitionTaskService recognitionTaskService;

    @PostMapping("/recognitions")
    public ApiResponse<CreateMealPhotoRecognitionTaskResponse> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateMealPhotoRecognitionTaskRequest request) {
        if (principal == null) {
            throw new IllegalStateException("expected authenticated user");
        }
        return ApiResponse.success(
                recognitionTaskService.create(principal.userId(), request.getFileId()));
    }

    @GetMapping("/recognitions/{taskId}")
    public ApiResponse<MealPhotoRecognitionTaskPollResponse> poll(
            @AuthenticationPrincipal AuthPrincipal principal, @PathVariable String taskId) {
        if (principal == null) {
            throw new IllegalStateException("expected authenticated user");
        }
        return ApiResponse.success(recognitionTaskService.poll(principal.userId(), taskId));
    }
}
