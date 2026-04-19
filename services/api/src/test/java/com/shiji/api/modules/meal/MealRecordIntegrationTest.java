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
import com.shiji.api.modules.meal.model.entity.DishEntity;
import com.shiji.api.modules.meal.model.entity.DishFoodItemRelEntity;
import com.shiji.api.modules.meal.model.entity.EmotionTagEntity;
import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import com.shiji.api.modules.meal.repository.DishFoodItemRelRepository;
import com.shiji.api.modules.meal.repository.DishRepository;
import com.shiji.api.modules.meal.repository.EmotionTagRepository;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import com.shiji.api.modules.meal.repository.FoodNutritionRepository;
import com.shiji.api.modules.meal.repository.MealFoodItemRepository;
import com.shiji.api.modules.meal.repository.MealRecognitionItemRepository;
import com.shiji.api.modules.meal.repository.MealRecognitionResultRepository;
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

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private FoodNutritionRepository foodNutritionRepository;

    @Autowired
    private DishFoodItemRelRepository dishFoodItemRelRepository;

    @Autowired
    private MealFoodItemRepository mealFoodItemRepository;

    @Autowired
    private MealRecognitionResultRepository mealRecognitionResultRepository;

    @Autowired
    private MealRecognitionItemRepository mealRecognitionItemRepository;

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
    void createMealRecord_dishNameSnapshotWithoutDishId() throws Exception {
        String token = loginAndGetToken();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "lunch");
        body.put("recordedAt", "2026-04-12T12:00:00");
        body.put("dishNameSnapshot", "用户看到的菜名");
        body.put("dishMatchSource", "ai_match");
        ArrayNode foods = body.putArray("foodItems");
        ObjectNode f1 = foods.addObject();
        f1.put("foodNameSnapshot", "饭");
        f1.put("recognitionSource", "ai");
        f1.put("displayUnit", "g");
        f1.put("sortOrder", 0);
        f1.put("estimatedCalories", new BigDecimal("50"));
        body.putArray("images");
        body.putArray("emotions");

        String responseJson =
                mockMvc.perform(
                                post("/api/meal-records")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long mealId = objectMapper.readTree(responseJson).path("data").path("mealRecordId").asLong();
        var meal = mealRecordRepository.findById(mealId).orElseThrow();
        assertThat(meal.getDishId()).isNull();
        assertThat(meal.getDishNameSnapshot()).isEqualTo("用户看到的菜名");
        assertThat(meal.getDishMatchSource()).isEqualTo("ai_match");
    }

    @Test
    void createMealRecord_rejectsUnknownDishId() throws Exception {
        String token = loginAndGetToken();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "lunch");
        body.put("recordedAt", "2026-04-12T12:00:00");
        body.put("dishId", 999999L);
        ArrayNode foods = body.putArray("foodItems");
        ObjectNode f1 = foods.addObject();
        f1.put("foodNameSnapshot", "饭");
        f1.put("recognitionSource", "ai");
        f1.put("displayUnit", "g");
        f1.put("sortOrder", 0);
        body.putArray("images");
        body.putArray("emotions");

        mockMvc.perform(
                        post("/api/meal-records")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MealErrorCode.MEAL_DISH_NOT_FOUND.getCode()));
    }

    @Test
    void createMealRecord_persistsRecognitionProcess() throws Exception {
        String token = loginAndGetToken();
        DishEntity dish = seedDish("T_DISH_R1", "测试菜");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "lunch");
        body.put("recordedAt", "2026-04-12T12:00:00");
        body.put("dishId", dish.getId());
        body.put("dishNameSnapshot", dish.getDishName());
        ObjectNode rec = body.putObject("recognition");
        rec.put("recognitionMode", "dish_first");
        rec.put("resultSource", "dish_match");
        rec.put("matchedDishId", dish.getId());
        rec.put("matchedDishName", dish.getDishName());
        rec.put("overallConfidence", 0.92);
        rec.put("needUserConfirm", 0);
        rec.put("rawAiResponse", "{\"labels\":[\"饭\"]}");
        rec.put("status", "success");
        ArrayNode rItems = rec.putArray("items");
        ObjectNode ri = rItems.addObject();
        ri.put("foodNameSnapshot", "米饭");
        ri.put("displayUnit", "g");
        ri.put("sortOrder", 0);
        ri.put("sourceType", "ai");

        ArrayNode foods = body.putArray("foodItems");
        ObjectNode f1 = foods.addObject();
        f1.put("foodNameSnapshot", "米饭");
        f1.put("recognitionSource", "ai");
        f1.put("displayUnit", "g");
        f1.put("sortOrder", 0);
        f1.put("estimatedCalories", new BigDecimal("200"));
        body.putArray("images");
        body.putArray("emotions");

        String responseJson =
                mockMvc.perform(
                                post("/api/meal-records")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long mealId = objectMapper.readTree(responseJson).path("data").path("mealRecordId").asLong();
        var head = mealRecognitionResultRepository.findByMealRecordId(mealId).orElseThrow();
        assertThat(head.getMatchedDishId()).isEqualTo(dish.getId());
        assertThat(head.getRawAiResponse()).contains("labels");
        assertThat(mealRecognitionItemRepository.findByRecognitionResultIdOrderBySortOrderAsc(head.getId()))
                .hasSize(1);
    }

    @Test
    void createMealRecord_dishRename_doesNotChangeHistoricalSnapshot() throws Exception {
        String token = loginAndGetToken();
        DishEntity dish = seedDish("T_DISH_SNAP", "旧名称");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "dinner");
        body.put("recordedAt", "2026-04-12T19:00:00");
        body.put("dishId", dish.getId());
        body.put("dishNameSnapshot", "旧名称");
        ArrayNode foods = body.putArray("foodItems");
        ObjectNode f1 = foods.addObject();
        f1.put("foodNameSnapshot", "饭");
        f1.put("recognitionSource", "user_manual");
        f1.put("displayUnit", "g");
        f1.put("sortOrder", 0);
        body.putArray("images");
        body.putArray("emotions");

        String responseJson =
                mockMvc.perform(
                                post("/api/meal-records")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long mealId = objectMapper.readTree(responseJson).path("data").path("mealRecordId").asLong();

        dish.setDishName("新名称");
        dishRepository.save(dish);

        var meal = mealRecordRepository.findById(mealId).orElseThrow();
        assertThat(meal.getDishNameSnapshot()).isEqualTo("旧名称");
    }

    @Test
    void createMealRecord_expandDish_generatesFoodRows() throws Exception {
        String token = loginAndGetToken();
        FoodItemEntity rice = seedFoodItem("T_RICE", "米饭");
        seedPer100gNutrition(rice.getId(), "200");
        DishEntity dish = seedDish("T_DISH_EXP", "炒饭");
        DishFoodItemRelEntity rel = new DishFoodItemRelEntity();
        rel.setDishId(dish.getId());
        rel.setFoodItemId(rice.getId());
        rel.setRoleType("main");
        rel.setDefaultWeightG(new BigDecimal("150"));
        rel.setIsOptional(0);
        rel.setSortOrder(0);
        LocalDateTime now = LocalDateTime.now();
        rel.setCreatedAt(now);
        rel.setUpdatedAt(now);
        dishFoodItemRelRepository.save(rel);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("mealType", "lunch");
        body.put("recordedAt", "2026-04-12T12:30:00");
        body.put("dishId", dish.getId());
        body.put("expandDishToFoodItems", true);
        body.putArray("foodItems");
        body.putArray("images");
        body.putArray("emotions");

        String responseJson =
                mockMvc.perform(
                                post("/api/meal-records")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long mealId = objectMapper.readTree(responseJson).path("data").path("mealRecordId").asLong();
        var rows = mealFoodItemRepository.findByMealRecordIdOrderBySortOrderAsc(mealId);
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getRecognitionSource()).isEqualTo("dish_expand");
        assertThat(rows.getFirst().getEstimatedCalories()).isNotNull();
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

    private DishEntity seedDish(String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        DishEntity d = new DishEntity();
        d.setDishCode(code);
        d.setDishName(name);
        d.setDishKind("dish");
        d.setDishSourceType("system_standard");
        d.setSupportFoodSplit(1);
        d.setDefaultUnit("portion");
        d.setEdibleStatus(1);
        d.setCreatedAt(now);
        d.setUpdatedAt(now);
        return dishRepository.save(d);
    }

    private FoodItemEntity seedFoodItem(String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        FoodItemEntity fi = new FoodItemEntity();
        fi.setFoodCode(code);
        fi.setFoodName(name);
        fi.setCategoryCode("staple");
        fi.setDefaultUnit("g");
        fi.setEdibleStatus(1);
        fi.setSourceType("manual");
        fi.setCreatedAt(now);
        fi.setUpdatedAt(now);
        return foodItemRepository.save(fi);
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
