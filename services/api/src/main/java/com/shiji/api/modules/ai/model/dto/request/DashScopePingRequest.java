package com.shiji.api.modules.ai.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashScopePingRequest {

    /** 自定义用户消息；为空则使用 classpath 默认 prompts */
    private String message;
}
