package com.shiji.api.modules.meal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import com.shiji.api.modules.auth.model.dto.request.SendSmsCodeRequest;
import com.shiji.api.modules.auth.model.dto.request.SmsCodeLoginRequest;
import com.shiji.api.modules.auth.model.enums.AgreementType;
import com.shiji.api.modules.auth.repository.SmsCodeLogRepository;
import com.shiji.api.modules.auth.repository.UserSessionRepository;
import com.shiji.api.modules.auth.service.SmsGateway;
import com.shiji.api.modules.meal.model.dto.MealErrorCode;
import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import com.shiji.api.modules.meal.repository.FoodNutritionRepository;
import java.math.BigDecimal;
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
@Import(FoodItemIntegrationTest.CapturingSmsConfig.class)
class FoodItemIntegrationTest {

    private static final String PHONE = "13900139001";

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
    private FoodItemRepository foodItemRepository;

    @Autowired
    private FoodNutritionRepository foodNutritionRepository;

    @BeforeEach
    void setUp() {
        capturingSmsGateway.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void searchFoodItems_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/food-items").param("page", "0").param("size", "20"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void searchFoodItems_invalidPage() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(
                        get("/api/food-items")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "-1")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MealErrorCode.MEAL_REQUEST_INVALID.getCode()));
    }

    @Test
    void searchFoodItems_pagingAndQuery() throws Exception {
        seedFood("T_X1", "搜索米饭一号", "116");
        seedFood("T_X2", "搜索面条二号", "137");

        String token = loginAndGetToken();

        mockMvc.perform(
                        get("/api/food-items")
                                .header("Authorization", "Bearer " + token)
                                .param("q", "米饭")
                                .param("page", "0")
                                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].foodName").value("搜索米饭一号"))
                .andExpect(jsonPath("$.data.items[0].caloriesPer100g").value(116))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        mockMvc.perform(
                        get("/api/food-items")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(true));

        mockMvc.perform(
                        get("/api/food-items")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    private void seedFood(String code, String name, String kcal) {
        LocalDateTime now = LocalDateTime.now();
        FoodItemEntity fi = new FoodItemEntity();
        fi.setFoodCode(code);
        fi.setFoodName(name);
        fi.setFoodAlias(null);
        fi.setCategoryCode("staple");
        fi.setBrandName(null);
        fi.setDefaultUnit("g");
        fi.setEdibleStatus(1);
        fi.setSourceType("manual");
        fi.setRemark(null);
        fi.setCreatedAt(now);
        fi.setUpdatedAt(now);
        FoodItemEntity saved = foodItemRepository.save(fi);

        FoodNutritionEntity n = new FoodNutritionEntity();
        n.setFoodItemId(saved.getId());
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
