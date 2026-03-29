package com.shiji.api.modules.auth.service;

import com.shiji.api.modules.auth.model.dto.response.LoginResponse;
import com.shiji.api.modules.auth.model.dto.response.SessionRestoreResponse;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SessionRestoreRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;

public interface AuthService {

    void sendSmsCode(SendSmsCodeRequest request, String requestIp);

    LoginResponse loginBySmsCode(SmsCodeLoginRequest request, String requestIp, String userAgent);

    SessionRestoreResponse restoreSession(SessionRestoreRequest request);

    void logoutAll(Long userId);
}
