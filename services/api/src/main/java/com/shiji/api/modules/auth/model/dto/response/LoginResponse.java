package com.shiji.api.modules.auth.model.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private Long userId;
    private String token;
    private LocalDateTime expireAt;
    private boolean newUser;
}
