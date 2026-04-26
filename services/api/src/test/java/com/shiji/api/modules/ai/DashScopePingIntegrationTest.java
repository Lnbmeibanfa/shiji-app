package com.shiji.api.modules.ai;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.ai.service.DashScopeTextClient;
import com.shiji.api.modules.ai.service.DashScopeTextReply;
import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import com.shiji.api.modules.auth.service.SmsGateway;
import java.util.Collections;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({DashScopePingIntegrationTest.MockDashScopeClient.class, DashScopePingIntegrationTest.StubSmsConfig.class})
class DashScopePingIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DashScopeTextClient dashScopeTextClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(dashScopeTextClient);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void ping_requiresAuth() throws Exception {
        mockMvc.perform(post("/api/ai/dashscope/ping").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void ping_withAuth_returnsModelReply() throws Exception {
        when(dashScopeTextClient.complete(anyString()))
                .thenReturn(new DashScopeTextReply("连通性回复", "qwen-turbo"));

        mockMvc.perform(
                        post("/api/ai/dashscope/ping")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .with(
                                        authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        new AuthPrincipal(1L),
                                                        null,
                                                        Collections.singletonList(
                                                                new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reply").value("连通性回复"))
                .andExpect(jsonPath("$.data.model").value("qwen-turbo"));
    }

    @Test
    void ping_withCustomMessage_passesToClient() throws Exception {
        when(dashScopeTextClient.complete(eq("自定义")))
                .thenReturn(new DashScopeTextReply("ok", "qwen-turbo"));

        mockMvc.perform(
                        post("/api/ai/dashscope/ping")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\":\"自定义\"}")
                                .with(
                                        authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        new AuthPrincipal(2L),
                                                        null,
                                                        Collections.singletonList(
                                                                new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reply").value("ok"));
    }

    @TestConfiguration
    static class MockDashScopeClient {

        @Bean
        @Primary
        DashScopeTextClient dashScopeTextClient() {
            return Mockito.mock(DashScopeTextClient.class);
        }
    }

    /** 与 {@link com.shiji.api.modules.meal.FoodItemIntegrationTest} 一致，提供 SMS Bean 以启动完整上下文 */
    @TestConfiguration
    static class StubSmsConfig {

        @Bean
        @Primary
        SmsGateway smsGateway() {
            return (phone, code) -> {};
        }
    }
}
