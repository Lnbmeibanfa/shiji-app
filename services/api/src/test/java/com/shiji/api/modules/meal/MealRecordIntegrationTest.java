package com.shiji.api.modules.meal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;
import com.shiji.api.modules.auth.model.enums.AgreementType;
import com.shiji.api.modules.auth.repository.SmsCodeLogRepository;
import com.shiji.api.modules.auth.repository.UserSessionRepository;
import com.shiji.api.modules.auth.service.SmsGateway;
import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import com.shiji.api.modules.file.repository.FileAssetRepository;
import com.shiji.api.modules.file.util.ObjectKeyHash;
import com.shiji.api.modules.meal.model.dto.MealErrorCode;
import com.shiji.api.modules.meal.model.entity.EmotionTagEntity;
import com.shiji.api.modules.meal.repository.EmotionTagRepository;
import com.shiji.api.modules.meal.repository.MealRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MealRecordIntegrationTest.CapturingSmsConfig.class)
class MealRecordIntegrationTest {

    private static final String PHONE = "13900139000";

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CapturingSmsGateway capturingSmsGateway;

    @Autowired
    private SmsCodeLogRepository smsCodeLogRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private EmotionTagRepository emotionTagRepository;

    @Autowired
    private FileAssetRepository fileAssetRepository;

    @Autowired
    private MealRecordRepository mealRecordRepository;

    @BeforeEach
    void setUp() {
        capturingSmsGateway.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void createMealRecord_requiresAuth() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "lunch");
        body.put("recordedAt", "2026-04-12T12:00:00");
        mockMvc.perform(
                        post("/api/meal-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void createMealRecord_success_recordDateTotalsPrimaryEmotion() throws Exception {
        String token = loginAndGetToken();
        long userId = userIdFromLastLogin();

        EmotionTagEntity lowSort = seedEmotion("e_low", "后", 2);
        EmotionTagEntity highSort = seedEmotion("e_high", "先", 1);

        long fileId = seedFileAsset(userId, "b1", "k1");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "lunch");
        body.put("recordedAt", "2026-04-12T15:30:00");
        body.put("note", "测试");
        ArrayNode foods = body.putArray("foodItems");
        ObjectNode f1 = foods.addObject();
        f1.put("foodNameSnapshot", "饭");
        f1.put("recognitionSource", "ai");
        f1.put("displayUnit", "g");
        f1.put("sortOrder", 0);
        f1.put("estimatedCalories", new BigDecimal("100"));
        ObjectNode f2 = foods.addObject();
        f2.put("foodNameSnapshot", "汤");
        f2.put("recognitionSource", "ai");
        f2.put("displayUnit", "g");
        f2.put("sortOrder", 1);
        f2.putNull("estimatedCalories");

        ArrayNode imgs = body.putArray("images");
        ObjectNode im = imgs.addObject();
        im.put("fileId", fileId);
        im.put("isPrimary", 1);
        im.put("sortOrder", 0);

        ArrayNode emos = body.putArray("emotions");
        ObjectNode e1 = emos.addObject();
        e1.put("emotionTagId", lowSort.getId());
        ObjectNode e2 = emos.addObject();
        e2.put("emotionTagId", highSort.getId());

        String responseJson =
                mockMvc.perform(
                                post("/api/meal-records")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andExpect(jsonPath("$.data.mealRecordId").isNumber())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long mealId = objectMapper.readTree(responseJson).path("data").path("mealRecordId").asLong();

        var meal = mealRecordRepository.findById(mealId).orElseThrow();
        assertThat(meal.getRecordDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        assertThat(meal.getTotalEstimatedCalories()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(meal.getPrimaryEmotionCode()).isEqualTo("e_high");
    }

    @Test
    void createMealRecord_rejectsMissingFile() throws Exception {
        String token = loginAndGetToken();
        ObjectNode body = minimalBody();
        ArrayNode imgs = body.putArray("images");
        ObjectNode im = imgs.addObject();
        im.put("fileId", 999999L);
        im.put("isPrimary", 0);
        im.put("sortOrder", 0);

        mockMvc.perform(
                        post("/api/meal-records")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MealErrorCode.MEAL_FILE_NOT_FOUND.getCode()));

        assertThat(mealRecordRepository.count()).isZero();
    }

    private ObjectNode minimalBody() {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "breakfast");
        body.put("recordedAt", "2026-01-02T08:00:00");
        body.putArray("foodItems");
        body.putArray("images");
        body.putArray("emotions");
        return body;
    }

    private EmotionTagEntity seedEmotion(String code, String name, int sortOrder) {
        LocalDateTime now = LocalDateTime.now();
        EmotionTagEntity t = new EmotionTagEntity();
        t.setEmotionCode(code);
        t.setEmotionName(name);
        t.setEmotionCategory("emotion");
        t.setEmotionStatus(1);
        t.setSortOrder(sortOrder);
        t.setIsSystem(1);
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        return emotionTagRepository.save(t);
    }

    private long seedFileAsset(long userId, String bucket, String objectKey) {
        LocalDateTime now = LocalDateTime.now();
        FileAssetEntity f = new FileAssetEntity();
        f.setUserId(userId);
        f.setStorageProvider("aliyun_oss");
        f.setBucket(bucket);
        f.setObjectKey(objectKey);
        f.setObjectKeyHash(ObjectKeyHash.sha256Hex(bucket, objectKey));
        f.setUrl("http://example/" + objectKey);
        f.setUploadSource("backend_proxy");
        f.setStatus("uploaded");
        f.setBizType("meal_photo");
        f.setUploadedAt(now);
        f.setCreatedAt(now);
        f.setUpdatedAt(now);
        return fileAssetRepository.save(f).getId();
    }

    private long userIdFromLastLogin() {
        return userSessionRepository.findAll().getFirst().getUserId();
    }

    private String loginAndGetToken() throws Exception {
        smsCodeLogRepository.deleteAll();
        userSessionRepository.deleteAll();

        SendSmsCodeRequest send = new SendSmsCodeRequest();
        send.setPhone(PHONE);
        mockMvc.perform(
                        post("/api/auth/sms/send")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(send)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String code = capturingSmsGateway.getLastCode();
        SmsCodeLoginRequest login = new SmsCodeLoginRequest();
        login.setPhone(PHONE);
        login.setCode(code);
        var a1 = new com.shiji.api.modules.auth.model.dto.request.AgreementAcceptanceDto();
        a1.setAgreementType(AgreementType.USER_AGREEMENT);
        a1.setAgreementVersion("v1");
        a1.setAccepted(true);
        var a2 = new com.shiji.api.modules.auth.model.dto.request.AgreementAcceptanceDto();
        a2.setAgreementType(AgreementType.PRIVACY_POLICY);
        a2.setAgreementVersion("v1");
        a2.setAccepted(true);
        login.setAgreements(List.of(a1, a2));

        String response =
                mockMvc.perform(
                                post("/api/auth/login/sms")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(login)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
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
