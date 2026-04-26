package com.shiji.api.modules.ai.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishIngredientVisionRequest {

    /** 仅支持 base64 图片内容（可带 data:image 前缀） */
    @NotBlank
    private String imageBase64;
}
