package com.shiji.api.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SessionRestoreRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;
import com.shiji.api.modules.auth.model.enums.AgreementType;
import com.shiji.api.modules.auth.model.enums.SessionStatus;
import com.shiji.api.modules.auth.model.enums.SmsCodeStatus;
import com.shiji.api.modules.auth.repository.SmsCodeLogRepository;
import com.shiji.api.modules.auth.repository.UserAgreementAcceptLogRepository;
import com.shiji.api.modules.auth.repository.UserSessionRepository;
import com.shiji.api.modules.auth.service.SmsGateway;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(AuthFlowIntegrationTest.CapturingSmsConfig.class)
class AuthFlowIntegrationTest {

    private static final String PHONE = "13800138000";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CapturingSmsGateway capturingSmsGateway;

    @Autowired
    private SmsCodeLogRepository smsCodeLogRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserAgreementAcceptLogRepository agreementLogRepository;

    @BeforeEach
    void resetCapture() {
        capturingSmsGateway.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void sendSmsCode_success_insertsLog() throws Exception {
        sendSmsExpectOk();

        assertThat(smsCodeLogRepository.findAll()).hasSize(1);
        assertThat(smsCodeLogRepository.findAll().getFirst().getStatus()).isEqualTo(SmsCodeStatus.PENDING);
    }

    @Test
    void sendSmsCode_rejectsSecondSendWithinOneMinute() throws Exception {
        sendSmsExpectOk();
        SendSmsCodeRequest again = new SendSmsCodeRequest();
        again.setPhone(PHONE);

        mockMvc.perform(
                        post("/api/auth/sms/send")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(again)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SMS_SEND_TOO_FREQUENT.getCode()));

        assertThat(smsCodeLogRepository.findAll()).hasSize(1);
    }

    @Test
    void sendSmsCode_rejectsWhenHourlyLimitReached() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            var row = new com.shiji.api.modules.auth.model.entity.SmsCodeLogEntity();
            row.setPhone(PHONE);
            row.setCodeHash("x" + i);
            row.setStatus(SmsCodeStatus.VERIFIED);
            row.setExpireAt(now.plusMinutes(5));
            row.setVerifyFailCount(0);
            row.setSentAt(now.minusMinutes(30));
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            smsCodeLogRepository.save(row);
        }

        SendSmsCodeRequest again = new SendSmsCodeRequest();
        again.setPhone(PHONE);
        mockMvc.perform(
                        post("/api/auth/sms/send")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(again)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SMS_HOURLY_LIMIT_EXCEEDED.getCode()));
    }

    @Test
    void loginBySms_success_returnsToken() throws Exception {
        sendSmsExpectOk();
        String code = capturingSmsGateway.getLastCode();
        assertThat(code).isNotBlank();

        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(code, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.userId").isNumber());

        assertThat(smsCodeLogRepository.findAll().getFirst().getStatus()).isEqualTo(SmsCodeStatus.VERIFIED);
        assertThat(userSessionRepository.findAll()).hasSize(1);
    }

    @Test
    void loginBySms_rejectsWhenCodeExpired() throws Exception {
        sendSmsExpectOk();
        var row = smsCodeLogRepository.findAll().getFirst();
        row.setExpireAt(LocalDateTime.now().minusMinutes(1));
        smsCodeLogRepository.save(row);

        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(capturingSmsGateway.getLastCode(), true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SMS_CODE_EXPIRED.getCode()));
    }

    @Test
    void loginBySms_rejectsAfterTooManyWrongAttempts() throws Exception {
        sendSmsExpectOk();
        String wrongBody = buildLoginJson("000000", true);
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(
                            post("/api/auth/login/sms")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(wrongBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(AuthErrorCode.SMS_CODE_INVALID.getCode()));
        }
        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SMS_CODE_NOT_FOUND.getCode()));
    }

    @Test
    void loginBySms_cannotReuseVerifiedCode() throws Exception {
        sendSmsExpectOk();
        String code = capturingSmsGateway.getLastCode();
        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(code, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(code, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SMS_CODE_NOT_FOUND.getCode()));
    }

    @Test
    void loginBySms_rejectsWhenAgreementNotAccepted() throws Exception {
        sendSmsExpectOk();
        String code = capturingSmsGateway.getLastCode();

        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(code, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.AGREEMENT_NOT_ACCEPTED.getCode()));
    }

    @Test
    void loginBySms_writesAgreementLogs() throws Exception {
        sendSmsExpectOk();
        String code = capturingSmsGateway.getLastCode();

        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(code, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertThat(agreementLogRepository.findAll()).hasSize(2);
    }

    @Test
    void sessionRestore_successAndInvalidToken() throws Exception {
        String token = loginAndGetToken();

        SessionRestoreRequest ok = new SessionRestoreRequest();
        ok.setToken(token);
        mockMvc.perform(
                        post("/api/auth/session/restore")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.phone").value(PHONE));

        SessionRestoreRequest bad = new SessionRestoreRequest();
        bad.setToken("invalid-token");
        mockMvc.perform(
                        post("/api/auth/session/restore")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SESSION_INVALID.getCode()));
    }

    @Test
    void sessionRestore_failsWhenSessionExpired() throws Exception {
        String token = loginAndGetToken();
        var session = userSessionRepository.findAll().getFirst();
        session.setExpireAt(LocalDateTime.now().minusDays(1));
        userSessionRepository.save(session);

        SessionRestoreRequest req = new SessionRestoreRequest();
        req.setToken(token);
        mockMvc.perform(
                        post("/api/auth/session/restore")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SESSION_INVALID.getCode()));
    }

    @Test
    void singleDevice_newLoginInvalidatesPreviousToken() throws Exception {
        String token1 = loginAndGetToken();

        capturingSmsGateway.clear();
        // 第二次发码需避开 1 分钟频控：把该号码历史发送时间拨到 2 分钟前
        LocalDateTime older = LocalDateTime.now().minusMinutes(2);
        smsCodeLogRepository.findAll().stream()
                .filter(log -> PHONE.equals(log.getPhone()))
                .forEach(log -> {
                    log.setSentAt(older);
                    smsCodeLogRepository.save(log);
                });
        sendSmsExpectOk();
        String code2 = capturingSmsGateway.getLastCode();
        mockMvc.perform(
                        post("/api/auth/login/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLoginJson(code2, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        SessionRestoreRequest req = new SessionRestoreRequest();
        req.setToken(token1);
        mockMvc.perform(
                        post("/api/auth/session/restore")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.SESSION_INVALID.getCode()));
    }

    @Test
    void logoutAll_invalidatesSessionsAndSetsLogoutAt() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(
                        post("/api/auth/logout/all")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        var sessions = userSessionRepository.findAll();
        assertThat(sessions).hasSize(1);
        assertThat(sessions.getFirst().getSessionStatus()).isEqualTo(SessionStatus.LOGGED_OUT);
        assertThat(sessions.getFirst().getLogoutAt()).isNotNull();
    }

    @Test
    void logoutAll_requiresBearerToken() throws Exception {
        loginAndGetToken();
        mockMvc.perform(post("/api/auth/logout/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    private void sendSmsExpectOk() throws Exception {
        SendSmsCodeRequest req = new SendSmsCodeRequest();
        req.setPhone(PHONE);
        mockMvc.perform(
                        post("/api/auth/sms/send")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private String loginAndGetToken() throws Exception {
        sendSmsExpectOk();
        String code = capturingSmsGateway.getLastCode();
        String response =
                mockMvc.perform(
                                post("/api/auth/login/sms")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(buildLoginJson(code, true)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }

    private String buildLoginJson(String code, boolean accepted) throws Exception {
        SmsCodeLoginRequest req = new SmsCodeLoginRequest();
        req.setPhone(PHONE);
        req.setCode(code);
        var a1 = new com.shiji.api.modules.auth.model.dto.request.AgreementAcceptanceDto();
        a1.setAgreementType(AgreementType.USER_AGREEMENT);
        a1.setAgreementVersion("v1");
        a1.setAccepted(accepted);
        var a2 = new com.shiji.api.modules.auth.model.dto.request.AgreementAcceptanceDto();
        a2.setAgreementType(AgreementType.PRIVACY_POLICY);
        a2.setAgreementVersion("v1");
        a2.setAccepted(accepted);
        req.setAgreements(List.of(a1, a2));
        return objectMapper.writeValueAsString(req);
    }

    @TestConfiguration
    static class CapturingSmsConfig {
        @Bean
        @Primary
        CapturingSmsGateway capturingSmsGateway() {
            return new CapturingSmsGateway();
        }
    }

    static class CapturingSmsGateway implements SmsGateway {
        private volatile String lastCode;

        @Override
        public void sendCode(String phone, String code) {
            this.lastCode = code;
        }

        String getLastCode() {
            return lastCode;
        }

        void clear() {
            this.lastCode = null;
        }
    }
}
