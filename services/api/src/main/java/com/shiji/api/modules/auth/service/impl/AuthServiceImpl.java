package com.shiji.api.modules.auth.service.impl;

import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import com.shiji.api.modules.auth.model.dto.request.AgreementAcceptanceDto;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SessionRestoreRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;
import com.shiji.api.modules.auth.model.dto.response.LoginResponse;
import com.shiji.api.modules.auth.model.dto.response.SessionRestoreResponse;
import com.shiji.api.modules.auth.model.entity.SmsCodeLogEntity;
import com.shiji.api.modules.auth.model.entity.UserAgreementAcceptLogEntity;
import com.shiji.api.modules.auth.model.entity.UserAuthEntity;
import com.shiji.api.modules.auth.model.entity.UserSessionEntity;
import com.shiji.api.modules.auth.model.enums.AuthType;
import com.shiji.api.modules.auth.model.enums.SessionStatus;
import com.shiji.api.modules.auth.model.enums.SmsCodeStatus;
import com.shiji.api.modules.auth.repository.SmsCodeLogRepository;
import com.shiji.api.modules.auth.repository.UserAgreementAcceptLogRepository;
import com.shiji.api.modules.auth.repository.UserAuthRepository;
import com.shiji.api.modules.auth.repository.UserSessionRepository;
import com.shiji.api.modules.auth.service.AuthService;
import com.shiji.api.modules.auth.service.SmsGateway;
import com.shiji.api.modules.auth.service.TokenService;
import com.shiji.api.modules.auth.service.exception.AuthBusinessException;
import com.shiji.api.modules.user.model.entity.UserEntity;
import com.shiji.api.modules.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int SMS_CODE_TTL_SECONDS = 60;
    private static final int SMS_MAX_VERIFY_ATTEMPTS = 10;
    private static final int SMS_HOURLY_SEND_LIMIT = 10;
    private static final int SESSION_TTL_DAYS = 30;

    private final SmsCodeLogRepository smsCodeLogRepository;
    private final UserAgreementAcceptLogRepository agreementLogRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;
    private final SmsGateway smsGateway;
    private final TokenService tokenService;

    @Override
    @Transactional
    public void sendSmsCode(SendSmsCodeRequest request, String requestIp) {
        LocalDateTime now = LocalDateTime.now();
        if (smsCodeLogRepository.countByPhoneAndSentAtAfter(request.getPhone(), now.minusMinutes(1)) > 0) {
            throw new AuthBusinessException(AuthErrorCode.SMS_SEND_TOO_FREQUENT);
        }
        if (smsCodeLogRepository.countByPhoneAndSentAtAfter(request.getPhone(), now.minusHours(1)) >= SMS_HOURLY_SEND_LIMIT) {
            throw new AuthBusinessException(AuthErrorCode.SMS_HOURLY_LIMIT_EXCEEDED);
        }

        String code = randomCode();
        smsGateway.sendCode(request.getPhone(), code);

        SmsCodeLogEntity entity = new SmsCodeLogEntity();
        entity.setPhone(request.getPhone());
        entity.setCodeHash(tokenService.hashToken(code));
        entity.setStatus(SmsCodeStatus.PENDING);
        entity.setExpireAt(now.plusSeconds(SMS_CODE_TTL_SECONDS));
        entity.setVerifyFailCount(0);
        entity.setRequestIp(requestIp);
        entity.setDeviceId(request.getDeviceId());
        entity.setSentAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        smsCodeLogRepository.save(entity);
    }

    @Override
    @Transactional
    public LoginResponse loginBySmsCode(SmsCodeLoginRequest request, String requestIp, String userAgent) {
        SmsCodeLogEntity smsCode = smsCodeLogRepository.findTopByPhoneAndStatusOrderBySentAtDesc(
                        request.getPhone(),
                        SmsCodeStatus.PENDING
                )
                .orElseThrow(() -> new AuthBusinessException(AuthErrorCode.SMS_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (smsCode.getExpireAt().isBefore(now)) {
            smsCode.setStatus(SmsCodeStatus.EXPIRED);
            smsCode.setUpdatedAt(now);
            throw new AuthBusinessException(AuthErrorCode.SMS_CODE_EXPIRED);
        }
        if (smsCode.getVerifyFailCount() >= SMS_MAX_VERIFY_ATTEMPTS) {
            smsCode.setStatus(SmsCodeStatus.INVALIDATED);
            smsCode.setUpdatedAt(now);
            throw new AuthBusinessException(AuthErrorCode.SMS_CODE_ATTEMPTS_EXCEEDED);
        }
        if (!smsCode.getCodeHash().equals(tokenService.hashToken(request.getCode()))) {
            smsCode.setVerifyFailCount(smsCode.getVerifyFailCount() + 1);
            if (smsCode.getVerifyFailCount() >= SMS_MAX_VERIFY_ATTEMPTS) {
                smsCode.setStatus(SmsCodeStatus.INVALIDATED);
            }
            smsCode.setUpdatedAt(now);
            throw new AuthBusinessException(AuthErrorCode.SMS_CODE_INVALID);
        }

        ensureAgreementsAccepted(request.getAgreements());

        smsCode.setStatus(SmsCodeStatus.VERIFIED);
        smsCode.setVerifiedAt(now);
        smsCode.setUpdatedAt(now);

        boolean newUser = false;
        UserEntity user = userRepository.findByPhone(request.getPhone()).orElseGet(() -> {
            UserEntity created = new UserEntity();
            created.setPhone(request.getPhone());
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            return userRepository.save(created);
        });
        if (user.getLastLoginAt() == null) {
            newUser = true;
        }

        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        userAuthRepository.findByAuthTypeAndAuthIdentifier(AuthType.PHONE_SMS, request.getPhone())
                .orElseGet(() -> {
                    UserAuthEntity userAuth = new UserAuthEntity();
                    userAuth.setUserId(user.getId());
                    userAuth.setAuthType(AuthType.PHONE_SMS);
                    userAuth.setAuthIdentifier(request.getPhone());
                    userAuth.setCreatedAt(now);
                    userAuth.setUpdatedAt(now);
                    return userAuthRepository.save(userAuth);
                });

        List<UserSessionEntity> activeSessions = userSessionRepository.findAllByUserIdAndSessionStatus(user.getId(), SessionStatus.ACTIVE);
        for (UserSessionEntity active : activeSessions) {
            active.setSessionStatus(SessionStatus.INVALIDATED);
            active.setUpdatedAt(now);
            active.setLogoutAt(now);
        }
        userSessionRepository.saveAll(activeSessions);

        String token = tokenService.generateToken();
        UserSessionEntity session = new UserSessionEntity();
        session.setUserId(user.getId());
        session.setTokenHash(tokenService.hashToken(token));
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setLoginType("PHONE_SMS");
        session.setExpireAt(now.plusDays(SESSION_TTL_DAYS));
        session.setDeviceId(request.getDeviceId());
        session.setClientIp(requestIp);
        session.setUserAgent(userAgent);
        session.setLoginAt(now);
        session.setLastActiveAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        userSessionRepository.save(session);

        for (AgreementAcceptanceDto agreement : request.getAgreements()) {
            UserAgreementAcceptLogEntity log = new UserAgreementAcceptLogEntity();
            log.setUserId(user.getId());
            log.setPhone(request.getPhone());
            log.setAgreementType(agreement.getAgreementType());
            log.setAgreementVersion(agreement.getAgreementVersion());
            log.setAccepted(agreement.getAccepted());
            log.setAcceptTime(now);
            log.setRequestIp(requestIp);
            log.setDeviceId(request.getDeviceId());
            log.setCreatedAt(now);
            agreementLogRepository.save(log);
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .token(token)
                .expireAt(session.getExpireAt())
                .newUser(newUser)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionRestoreResponse restoreSession(SessionRestoreRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UserSessionEntity session = userSessionRepository.findByTokenHashAndSessionStatusAndExpireAtAfter(
                        tokenService.hashToken(request.getToken()),
                        SessionStatus.ACTIVE,
                        now
                )
                .orElseThrow(() -> new AuthBusinessException(AuthErrorCode.SESSION_INVALID));

        UserEntity user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new AuthBusinessException(AuthErrorCode.UNAUTHORIZED));

        return SessionRestoreResponse.builder()
                .userId(user.getId())
                .phone(user.getPhone())
                .expireAt(session.getExpireAt())
                .build();
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserSessionEntity> activeSessions = userSessionRepository.findAllByUserIdAndSessionStatus(userId, SessionStatus.ACTIVE);
        for (UserSessionEntity session : activeSessions) {
            session.setSessionStatus(SessionStatus.LOGGED_OUT);
            session.setLogoutAt(now);
            session.setUpdatedAt(now);
        }
        userSessionRepository.saveAll(activeSessions);
    }

    private void ensureAgreementsAccepted(List<AgreementAcceptanceDto> agreements) {
        if (agreements == null || agreements.isEmpty()) {
            throw new AuthBusinessException(AuthErrorCode.AGREEMENT_NOT_ACCEPTED);
        }
        boolean allAccepted = agreements.stream().allMatch(item -> Boolean.TRUE.equals(item.getAccepted()));
        if (!allAccepted) {
            throw new AuthBusinessException(AuthErrorCode.AGREEMENT_NOT_ACCEPTED);
        }
    }

    private String randomCode() {
        int value = (int) (Math.random() * 900000) + 100000;
        return Integer.toString(value);
    }
}
