package com.shiji.api.modules.ai.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMealPhotoRecognitionTaskRequest {

    @NotNull(message = "fileId 不能为空")
    private Long fileId;
}
