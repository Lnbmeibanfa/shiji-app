package com.shiji.api.modules.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionRestoreRequest {

    @NotBlank
    private String token;
}
