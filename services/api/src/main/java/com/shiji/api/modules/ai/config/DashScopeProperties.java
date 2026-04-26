package com.shiji.api.modules.ai.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    /** 来自环境变量 {@code DASHSCOPE_API_KEY}，未设置时为空字符串 */
    private String apiKey = "";

    /** 文本模型名，与百炼控制台可用模型对齐 */
    private String textModel = "qwen-turbo";

    /** 视觉模型配置 */
    private Vision vision = new Vision();

    private Duration connectTimeout = Duration.ofSeconds(15);

    private Duration readTimeout = Duration.ofSeconds(120);

    @Getter
    @Setter
    public static class Vision {

        /** 默认使用 qwen3.6-flash（按团队约定） */
        private String model = "qwen3.6-flash";

        /** dish / ingredient 统一置信度阈值 */
        private double confidenceThreshold = 0.7d;

        /** 输入图最长边限制，超出则等比缩放 */
        private int maxEdge = 1440;

        /** JPEG 压缩质量（0-1），在需要重编码时使用 */
        private double jpegQuality = 0.92d;

        /** 单次视觉请求超时，超时走 UPSTREAM_AI_ERROR */
        private Duration timeout = Duration.ofSeconds(45);
    }
}
