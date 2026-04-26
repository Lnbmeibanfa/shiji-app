package com.shiji.api.modules.ai.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.ai.model.dto.request.DashScopePingRequest;
import com.shiji.api.modules.ai.model.dto.response.DashScopePingResponse;
import com.shiji.api.modules.ai.service.DashScopePingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/dashscope")
public class DashScopePingController {

    private final DashScopePingService dashScopePingService;

    /**
     * 连通性验证：调用 DashScope 文本模型。需登录。
     *
     * @param principal 当前用户（保证已认证）
     */
    @PostMapping("/ping")
    public ApiResponse<DashScopePingResponse> ping(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody(required = false) DashScopePingRequest request) {
        if (principal == null) {
            throw new IllegalStateException("expected authenticated user");
        }
        String message = request != null ? request.getMessage() : null;
        return ApiResponse.success(dashScopePingService.ping(message));
    }
}
