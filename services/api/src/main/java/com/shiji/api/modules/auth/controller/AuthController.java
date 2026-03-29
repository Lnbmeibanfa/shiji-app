package com.shiji.api.modules.auth.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SessionRestoreRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;
import com.shiji.api.modules.auth.model.dto.response.LoginResponse;
import com.shiji.api.modules.auth.model.dto.response.SessionRestoreResponse;
import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSmsCode(
            @Valid @RequestBody SendSmsCodeRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.sendSmsCode(request, httpRequest.getRemoteAddr());
        return ApiResponse.success();
    }

    @PostMapping("/login/sms")
    public ApiResponse<LoginResponse> loginBySmsCode(
            @Valid @RequestBody SmsCodeLoginRequest request,
            HttpServletRequest httpRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        LoginResponse response = authService.loginBySmsCode(
                request,
                httpRequest.getRemoteAddr(),
                userAgent
        );
        return ApiResponse.success(response);
    }

    @PostMapping("/session/restore")
    public ApiResponse<SessionRestoreResponse> restoreSession(
            @Valid @RequestBody SessionRestoreRequest request
    ) {
        return ApiResponse.success(authService.restoreSession(request));
    }

    @PostMapping("/logout/all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal AuthPrincipal principal) {
        authService.logoutAll(principal.userId());
        return ApiResponse.success();
    }
}
