package com.shiji.api.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shiji.api.modules.ai.config.DashScopeProperties;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeVisionClient {

    private static final String DEFAULT_ENDPOINT =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final DashScopeProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String inferJson(String prompt, byte[] imageBytes, String mimeType) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new AiBusinessException(AiErrorCode.DASHSCOPE_NOT_CONFIGURED);
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", properties.getVision().getModel());
            root.put("temperature", properties.getVision().getConfidenceThreshold());
            ObjectNode responseFormat = root.putObject("response_format");
            responseFormat.put("type", "json_object");

            ArrayNode messages = root.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            ArrayNode content = userMessage.putArray("content");
            ObjectNode text = content.addObject();
            text.put("type", "text");
            text.put("text", prompt);
            ObjectNode image = content.addObject();
            image.put("type", "image_url");
            ObjectNode imageUrl = image.putObject("image_url");
            imageUrl.put("url", "data:" + mimeType + ";base64," + base64);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(DEFAULT_ENDPOINT))
                            .timeout(resolveTimeout())
                            .header("Authorization", "Bearer " + properties.getApiKey())
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                            .build();

            HttpClient client = HttpClient.newBuilder().connectTimeout(properties.getConnectTimeout()).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                String snippet = abbreviateForLog(response.body(), 800);
                log.warn(
                        "DashScope vision HTTP {} — body (truncated): {}",
                        response.statusCode(),
                        snippet);
                throw new AiBusinessException(AiErrorCode.UPSTREAM_AI_ERROR);
            }
            JsonNode body = objectMapper.readTree(response.body());
            JsonNode contentNode = body.path("choices").path(0).path("message").path("content");
            if (!contentNode.isTextual() || !StringUtils.hasText(contentNode.asText())) {
                throw new AiBusinessException(AiErrorCode.MODEL_OUTPUT_INVALID);
            }
            return contentNode.asText();
        } catch (AiBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("DashScope vision call failed: {}", e.toString());
            throw new AiBusinessException(AiErrorCode.UPSTREAM_AI_ERROR, e);
        }
    }

    private static String abbreviateForLog(String body, int maxLen) {
        if (body == null || body.isEmpty()) {
            return "(empty)";
        }
        String t = body.replace('\n', ' ').trim();
        return t.length() <= maxLen ? t : t.substring(0, maxLen) + "…";
    }

    private Duration resolveTimeout() {
        return properties.getVision().getTimeout() != null ? properties.getVision().getTimeout() : properties.getReadTimeout();
    }
}
