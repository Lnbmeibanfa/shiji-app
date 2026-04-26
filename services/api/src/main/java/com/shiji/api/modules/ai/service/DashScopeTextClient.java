package com.shiji.api.modules.ai.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.shiji.api.modules.ai.config.DashScopeProperties;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DashScopeTextClient {

    private final DashScopeProperties properties;

    /**
     * 调用 DashScope 文本生成，返回模型回复文本与所用模型名。
     *
     * @param userMessage 用户消息（非空）
     */
    public DashScopeTextReply complete(String userMessage) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new AiBusinessException(AiErrorCode.DASHSCOPE_NOT_CONFIGURED);
        }
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("userMessage must not be blank");
        }

        Message user = Message.builder().role(Role.USER.getValue()).content(userMessage).build();
        GenerationParam param =
                GenerationParam.builder()
                        .model(properties.getTextModel())
                        .messages(List.of(user))
                        .apiKey(properties.getApiKey())
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();

        try {
            Generation gen = new Generation();
            GenerationResult raw = gen.call(param);
            String reply = extractReplyText(raw.getOutput());
            return new DashScopeTextReply(reply, properties.getTextModel());
        } catch (ApiException e) {
            throw new AiBusinessException(AiErrorCode.DASHSCOPE_CALL_FAILED, e);
        } catch (AiBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new AiBusinessException(AiErrorCode.DASHSCOPE_CALL_FAILED, e);
        }
    }

    private static String extractReplyText(GenerationOutput output) {
        if (output == null) {
            return "";
        }
        if (output.getChoices() != null && !output.getChoices().isEmpty()) {
            var choice = output.getChoices().get(0);
            if (choice.getMessage() != null && StringUtils.hasText(choice.getMessage().getContent())) {
                return choice.getMessage().getContent();
            }
        }
        if (StringUtils.hasText(output.getText())) {
            return output.getText();
        }
        return "";
    }
}
