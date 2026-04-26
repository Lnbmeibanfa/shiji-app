package com.shiji.api.modules.ai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.service.DashScopeVisionClient;
import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;
import com.shiji.api.modules.auth.model.enums.AgreementType;
import com.shiji.api.modules.auth.repository.SmsCodeLogRepository;
import com.shiji.api.modules.auth.repository.UserSessionRepository;
import com.shiji.api.modules.auth.service.SmsGateway;
import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import com.shiji.api.modules.file.repository.FileAssetRepository;
import com.shiji.api.modules.file.service.FileStorageService;
import com.shiji.api.modules.file.util.ObjectKeyHash;
import com.shiji.api.modules.meal.model.entity.DishAliasEntity;
import com.shiji.api.modules.meal.model.entity.DishEntity;
import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import com.shiji.api.modules.meal.repository.DishAliasRepository;
import com.shiji.api.modules.meal.repository.DishRepository;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import com.shiji.api.modules.meal.repository.FoodNutritionRepository;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Import({
    MealPhotoRecognitionTaskIntegrationTest.MockVisionClient.class,
    MealPhotoRecognitionTaskIntegrationTest.CapturingSmsConfig.class
})
class MealPhotoRecognitionTaskIntegrationTest {

    private static final String PHONE_A = "13900139008";
    private static final String PHONE_B = "13900139000";

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private DashScopeVisionClient dashScopeVisionClient;
    @Autowired private CapturingSmsGateway capturingSmsGateway;
    @Autowired private SmsCodeLogRepository smsCodeLogRepository;
    @Autowired private UserSessionRepository userSessionRepository;
    @Autowired private DishRepository dishRepository;
    @Autowired private DishAliasRepository dishAliasRepository;
    @Autowired private FoodItemRepository foodItemRepository;
    @Autowired private FoodNutritionRepository foodNutritionRepository;
    @Autowired private FileAssetRepository fileAssetRepository;
    @MockitoBean private FileStorageService fileStorageService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reset(dashScopeVisionClient, fileStorageService);
        capturingSmsGateway.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void create_requiresAuth() throws Exception {
        mockMvc.perform(
                        post("/api/ai/meal-photo/recognitions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"fileId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void create_invalidFileId_returnsBusinessError() throws Exception {
        String token = loginAndGetToken(PHONE_A);
        mockMvc.perform(
                        post("/api/ai/meal-photo/recognitions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"fileId\":999999999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AiErrorCode.RECOGNITION_FILE_INVALID.getCode()));
    }

    @Test
    void create_and_poll_success() throws Exception {
        String u = Long.toHexString(System.nanoTime());
        seedDishAndAlias("宫保鸡丁", "宫保鸡丁", u);
        long chickenId = seedFood("鸡胸肉");
        seedPer100gNutrition(chickenId, "165");
        when(fileStorageService.downloadMealPhotoBytes(anyLong(), anyLong())).thenReturn(buildTinyJpegBytes());
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenReturn(
                        "{\"outcome\":\"OK\",\"dishCandidates\":[{\"name\":\"宫保鸡丁_"
                                + u
                                + "\",\"confidence\":0.92}],\"ingredients\":[{\"name\":\"鸡胸肉\",\"confidence\":0.88}]}");

        String token = loginAndGetToken(PHONE_A);
        long userId = userSessionRepository.findAll().getFirst().getUserId();
        long fileId = seedFileAsset(userId, "b1", "k1");

        MvcResult created =
                mockMvc.perform(
                                post("/api/ai/meal-photo/recognitions")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"fileId\":" + fileId + "}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andExpect(jsonPath("$.data.taskId").isString())
                        .andExpect(jsonPath("$.data.status").value("pending"))
                        .andReturn();

        String taskId = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("taskId").asText();

        String pollStatus = "pending";
        for (int i = 0; i < 80 && !"success".equals(pollStatus); i++) {
            Thread.sleep(100);
            MvcResult r =
                    mockMvc.perform(
                                    get("/api/ai/meal-photo/recognitions/" + taskId)
                                            .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(0))
                            .andReturn();
            pollStatus =
                    objectMapper.readTree(r.getResponse().getContentAsString()).path("data").path("status").asText();
        }

        mockMvc.perform(get("/api/ai/meal-photo/recognitions/" + taskId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.result.requestId").isString())
                .andExpect(jsonPath("$.data.result.recognition.schemaVersion").value("1.0"))
                .andExpect(jsonPath("$.data.result.recognition.dish.dishId").isString())
                .andExpect(jsonPath("$.data.result.recognition.ingredients[0].foodName").value("鸡胸肉"))
                .andExpect(jsonPath("$.data.result.recognition.ingredients[0].caloriesPer100g").value(165.0));
    }

    @Test
    void poll_foreignTask_returnsNotFound() throws Exception {
        String u = Long.toHexString(System.nanoTime());
        when(fileStorageService.downloadMealPhotoBytes(anyLong(), anyLong())).thenReturn(buildTinyJpegBytes());
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenReturn(
                        "{\"outcome\":\"OK\",\"dishCandidates\":[{\"name\":\"宫保鸡丁_"
                                + u
                                + "\",\"confidence\":0.92}],\"ingredients\":[]}");

        seedDishAndAlias("宫保鸡丁", "宫保鸡丁", u);
        String tokenA = loginAndGetToken(PHONE_A);
        long userIdA = userSessionRepository.findAll().getFirst().getUserId();
        long fileId = seedFileAsset(userIdA, "b2", "k2");

        MvcResult created =
                mockMvc.perform(
                                post("/api/ai/meal-photo/recognitions")
                                        .header("Authorization", "Bearer " + tokenA)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"fileId\":" + fileId + "}"))
                        .andExpect(status().isOk())
                        .andReturn();
        String taskId = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("taskId").asText();

        String tokenB = loginAndGetToken(PHONE_B);
        mockMvc.perform(get("/api/ai/meal-photo/recognitions/" + taskId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AiErrorCode.RECOGNITION_TASK_NOT_FOUND.getCode()));
    }

    @Test
    void poll_lazyTimeout_whileVisionSlow() throws Exception {
        String u = Long.toHexString(System.nanoTime());
        when(fileStorageService.downloadMealPhotoBytes(anyLong(), anyLong())).thenReturn(buildTinyJpegBytes());
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenAnswer(
                        inv -> {
                            Thread.sleep(10_000);
                            return "{\"outcome\":\"OK\",\"dishCandidates\":[{\"name\":\"宫保鸡丁_"
                                    + u
                                    + "\",\"confidence\":0.92}],\"ingredients\":[]}";
                        });

        seedDishAndAlias("宫保鸡丁", "宫保鸡丁", u);
        String token = loginAndGetToken(PHONE_A);
        long userId = userSessionRepository.findAll().getFirst().getUserId();
        long fileId = seedFileAsset(userId, "b3", "k3");

        MvcResult created =
                mockMvc.perform(
                                post("/api/ai/meal-photo/recognitions")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"fileId\":" + fileId + "}"))
                        .andExpect(status().isOk())
                        .andReturn();
        String taskId = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("taskId").asText();

        Thread.sleep(3500);

        mockMvc.perform(get("/api/ai/meal-photo/recognitions/" + taskId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("failed"))
                .andExpect(jsonPath("$.data.errorCode").value(AiErrorCode.RECOGNITION_TASK_TIMEOUT.getCode()));
    }

    private void seedDishAndAlias(String dishName, String aliasName, String uniqueSuffix) {
        LocalDateTime now = LocalDateTime.now();
        DishEntity dish = new DishEntity();
        dish.setDishCode("T_" + uniqueSuffix);
        dish.setDishName(dishName);
        dish.setDishKind("dish");
        dish.setDishSourceType("system_standard");
        dish.setSupportFoodSplit(1);
        dish.setDefaultUnit("portion");
        dish.setEdibleStatus(1);
        dish.setCreatedAt(now);
        dish.setUpdatedAt(now);
        DishEntity savedDish = dishRepository.save(dish);

        DishAliasEntity alias = new DishAliasEntity();
        alias.setDishId(savedDish.getId());
        alias.setAliasName(aliasName + "_" + uniqueSuffix);
        alias.setAliasType("normal");
        alias.setCreatedAt(now);
        alias.setUpdatedAt(now);
        dishAliasRepository.save(alias);
    }

    private long seedFood(String name) {
        LocalDateTime now = LocalDateTime.now();
        FoodItemEntity fi = new FoodItemEntity();
        fi.setFoodCode("F_" + name + "_" + System.nanoTime());
        fi.setFoodName(name);
        fi.setFoodAlias(null);
        fi.setCategoryCode("test");
        fi.setDefaultUnit("g");
        fi.setEdibleStatus(1);
        fi.setSourceType("system");
        fi.setCreatedAt(now);
        fi.setUpdatedAt(now);
        return foodItemRepository.save(fi).getId();
    }

    private void seedPer100gNutrition(long foodItemId, String kcal) {
        LocalDateTime now = LocalDateTime.now();
        FoodNutritionEntity n = new FoodNutritionEntity();
        n.setFoodItemId(foodItemId);
        n.setNutrientBasis("per_100g");
        n.setCalories(new BigDecimal(kcal));
        n.setVersionNo(1);
        n.setDataSource("test");
        n.setCreatedAt(now);
        n.setUpdatedAt(now);
        foodNutritionRepository.save(n);
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

    private String loginAndGetToken(String phone) throws Exception {
        smsCodeLogRepository.deleteAll();
        userSessionRepository.deleteAll();

        SendSmsCodeRequest send = new SendSmsCodeRequest();
        send.setPhone(phone);
        mockMvc.perform(
                        post("/api/auth/sms/send")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(send)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String code = capturingSmsGateway.getLastCode();
        SmsCodeLoginRequest login = new SmsCodeLoginRequest();
        login.setPhone(phone);
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

    private static byte[] buildTinyJpegBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFFAA00);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    @TestConfiguration
    static class MockVisionClient {
        @Bean
        @Primary
        DashScopeVisionClient dashScopeVisionClient() {
            return Mockito.mock(DashScopeVisionClient.class);
        }
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
