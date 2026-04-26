package com.shiji.api.modules.ai.model.dto.response;

import lombok.Builder;

@Builder
public record CreateMealPhotoRecognitionTaskResponse(String taskId, String status) {}
