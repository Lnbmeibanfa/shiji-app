package com.shiji.api.modules.ai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import java.util.Base64;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({
    DishIngredientVisionIntegrationTest.MockVisionClient.class,
    DishIngredientVisionIntegrationTest.CapturingSmsConfig.class
})
class DishIngredientVisionIntegrationTest {

    private static final String PHONE = "13900139008";

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private DashScopeVisionClient dashScopeVisionClient;
    @Autowired private CapturingSmsGateway capturingSmsGateway;
    @Autowired private SmsCodeLogRepository smsCodeLogRepository;
    @Autowired private UserSessionRepository userSessionRepository;
    @Autowired private DishRepository dishRepository;
    @Autowired private DishAliasRepository dishAliasRepository;
    @Autowired private FoodItemRepository foodItemRepository;
    @Autowired private FoodNutritionRepository foodNutritionRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reset(dashScopeVisionClient);
        capturingSmsGateway.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void recognize_requiresAuth() throws Exception {
        mockMvc.perform(
                        post("/api/ai/dashscope/vision/dish-ingredients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"imageBase64\":\"" + buildTinyJpegBase64() + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void recognize_success_withDishAndIngredients() throws Exception {
        seedDishAndAlias("宫保鸡丁", "宫保鸡丁");
        long chickenId = seedFood("鸡胸肉");
        seedFood("花生米");
        seedPer100gNutrition(chickenId, "165");
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenReturn(
                        """
                        {"outcome":"OK","dishCandidates":[{"name":"宫保鸡丁","confidence":0.92}],"ingredients":[{"name":"鸡胸肉","confidence":0.88},{"name":"花生米","confidence":0.83}]}
                        """);

        String token = loginAndGetToken();
        mockMvc.perform(
                        post("/api/ai/dashscope/vision/dish-ingredients")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"imageBase64\":\"" + buildTinyJpegBase64() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.requestId").isString())
                .andExpect(jsonPath("$.data.recognition.schemaVersion").value("1.0"))
                .andExpect(jsonPath("$.data.recognition.dish.dishId").isString())
                .andExpect(jsonPath("$.data.recognition.ingredients.length()").value(2))
                .andExpect(jsonPath("$.data.recognition.ingredients[0].foodName").value("鸡胸肉"))
                .andExpect(jsonPath("$.data.recognition.ingredients[0].defaultUnit").value("g"))
                .andExpect(jsonPath("$.data.recognition.ingredients[0].caloriesPer100g").value(165.0))
                .andExpect(jsonPath("$.data.recognition.ingredients[1].foodName").value("花生米"))
                .andExpect(jsonPath("$.data.recognition.ingredients[1].defaultUnit").value("g"));
    }

    @Test
    void recognize_lowDishConfidence_fallbackIngredients() throws Exception {
        seedDishAndAlias("番茄炒蛋", "番茄炒蛋");
        seedFood("鸡蛋");
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenReturn(
                        """
                        {"outcome":"OK","dishCandidates":[{"name":"番茄炒蛋","confidence":0.55}],"ingredients":[{"name":"鸡蛋","confidence":0.81}]}
                        """);

        String token = loginAndGetToken();
        mockMvc.perform(
                        post("/api/ai/dashscope/vision/dish-ingredients")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"imageBase64\":\"" + buildTinyJpegBase64() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.recognition.dish").isEmpty())
                .andExpect(jsonPath("$.data.recognition.dishRejection.code").value("LOW_CONFIDENCE"))
                .andExpect(jsonPath("$.data.recognition.ingredients.length()").value(1))
                .andExpect(jsonPath("$.data.recognition.ingredients[0].foodName").value("鸡蛋"))
                .andExpect(jsonPath("$.data.recognition.ingredients[0].defaultUnit").value("g"));
    }

    @Test
    void recognize_notFood_returnsBusinessError() throws Exception {
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenReturn("{\"outcome\":\"NOT_FOOD\",\"dishCandidates\":[],\"ingredients\":[]}");
        String token = loginAndGetToken();

        mockMvc.perform(
                        post("/api/ai/dashscope/vision/dish-ingredients")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"imageBase64\":\"" + buildTinyJpegBase64() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AiErrorCode.NOT_FOOD_IMAGE.getCode()));
    }

    @Test
    void recognize_invalidModelJson_returnsBusinessError() throws Exception {
        when(dashScopeVisionClient.inferJson(anyString(), any(byte[].class), anyString()))
                .thenReturn("not-json");
        String token = loginAndGetToken();

        mockMvc.perform(
                        post("/api/ai/dashscope/vision/dish-ingredients")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"imageBase64\":\"" + buildTinyJpegBase64() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AiErrorCode.MODEL_OUTPUT_INVALID.getCode()));
    }

    private void seedDishAndAlias(String dishName, String aliasName) {
        LocalDateTime now = LocalDateTime.now();
        DishEntity dish = new DishEntity();
        dish.setDishCode("T_" + dishName);
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
        alias.setAliasName(aliasName);
        alias.setAliasType("normal");
        alias.setCreatedAt(now);
        alias.setUpdatedAt(now);
        dishAliasRepository.save(alias);
    }

    private long seedFood(String name) {
        LocalDateTime now = LocalDateTime.now();
        FoodItemEntity fi = new FoodItemEntity();
        fi.setFoodCode("F_" + name);
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

    private static String buildTinyJpegBase64() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFFAA00);
        image.setRGB(1, 0, 0x00AAFF);
        image.setRGB(0, 1, 0xFFFFFF);
        image.setRGB(1, 1, 0x222222);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
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
