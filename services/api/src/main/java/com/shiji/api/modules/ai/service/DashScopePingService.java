package com.shiji.api.modules.ai.service;

import com.shiji.api.modules.ai.model.dto.response.DashScopePingResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DashScopePingService {

    private static final String DEFAULT_PROMPT_PATH = "prompts/dashscope/default-ping-message.txt";

    private final DashScopeTextClient dashScopeTextClient;

    private volatile String cachedDefaultMessage;

    public DashScopePingResponse ping(String optionalMessage) {
        String userMessage =
                StringUtils.hasText(optionalMessage) ? optionalMessage.trim() : loadDefaultPingMessage();
        DashScopeTextReply reply = dashScopeTextClient.complete(userMessage);
        return new DashScopePingResponse(reply.reply(), reply.model());
    }

    private String loadDefaultPingMessage() {
        if (cachedDefaultMessage != null) {
            return cachedDefaultMessage;
        }
        synchronized (this) {
            if (cachedDefaultMessage != null) {
                return cachedDefaultMessage;
            }
            ClassPathResource res = new ClassPathResource(DEFAULT_PROMPT_PATH);
            if (!res.exists()) {
                throw new IllegalStateException("Missing classpath resource: " + DEFAULT_PROMPT_PATH);
            }
            try {
                cachedDefaultMessage =
                        StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + DEFAULT_PROMPT_PATH, e);
            }
            if (!StringUtils.hasText(cachedDefaultMessage)) {
                throw new IllegalStateException("Default ping message is empty: " + DEFAULT_PROMPT_PATH);
            }
            return cachedDefaultMessage;
        }
    }
}
