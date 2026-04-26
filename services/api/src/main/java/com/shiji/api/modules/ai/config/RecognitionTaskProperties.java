package com.shiji.api.modules.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "recognition.task")
public class RecognitionTaskProperties {

    /** 任务从创建起超过该秒数仍未终态则标记失败 */
    private int timeoutSeconds = 60;

    /** 终态任务保留天数，超过则可删除 */
    private int retentionDays = 7;
}
