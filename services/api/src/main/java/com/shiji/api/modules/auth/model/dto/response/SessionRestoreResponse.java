package com.shiji.api.modules.auth.model.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionRestoreResponse {

    private Long userId;
    private String phone;
    private LocalDateTime expireAt;
}
