package com.shiji.api.modules.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shiji.oss")
public class OssProperties {

    /** 为 false 时不上传 OSS，接口返回业务错误提示配置。 */
    private boolean enabled = false;

    /** 如 https://oss-cn-hangzhou.aliyuncs.com */
    private String endpoint = "https://oss-cn-guangzhou.aliyuncs.com";

    private String region = "oss-cn-guangzhou";

    private String bucket = "linwanqi12138";

    private String accessKeyId = "";

    private String accessKeySecret = "";

    /** 单文件最大字节数，默认 10MB */
    private long maxFileSizeBytes = 10 * 1024 * 1024;
}
