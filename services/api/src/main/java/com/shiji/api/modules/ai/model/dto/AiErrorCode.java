package com.shiji.api.modules.ai.model.dto;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public enum AiErrorCode implements ErrorCode {
    DASHSCOPE_NOT_CONFIGURED(31001, "未配置 DashScope API Key，请设置环境变量 DASHSCOPE_API_KEY"),
    DASHSCOPE_CALL_FAILED(31002, "DashScope 调用失败，请稍后重试"),
    UPSTREAM_AI_ERROR(31003, "上游 AI 服务暂时不可用，请稍后重试"),
    MODEL_OUTPUT_INVALID(31004, "模型输出格式异常，请稍后重试"),
    NOT_FOOD_IMAGE(31005, "上传内容不是食物图片，请更换后重试"),
    UNRECOGNIZABLE_IMAGE(31006, "图片无法识别，请拍清晰后重试"),
    VISION_REQUEST_INVALID(31007, "视觉识别请求参数不合法"),
    RECOGNITION_TASK_NOT_FOUND(31108, "识别任务不存在或已过期"),
    RECOGNITION_TASK_TIMEOUT(31109, "识别超时，请重新尝试"),
    RECOGNITION_FILE_READ_FAILED(31110, "无法读取图片文件，请重新上传"),
    RECOGNITION_FILE_INVALID(31111, "图片不可用，请重新上传");

    private final int code;
    private final String message;

    AiErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
