package com.shiji.api.modules.ai.service;

import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.repository.AiRecognitionTaskRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecognitionTaskStateService {

    private final AiRecognitionTaskRepository taskRepository;

    @Transactional
    public int markProcessingIfPending(String taskId, LocalDateTime now) {
        return taskRepository.markProcessingIfPending(taskId, now);
    }

    @Transactional
    public int markSuccessIfProcessing(
            String taskId, String json, String modelName, String promptVersion, LocalDateTime now) {
        return taskRepository.markSuccessIfProcessing(taskId, json, modelName, promptVersion, now);
    }

    @Transactional
    public int markFailedIfActive(String taskId, AiErrorCode code, LocalDateTime now) {
        return markFailedIfActive(taskId, code.getCode(), code.getMessage(), now);
    }

    @Transactional
    public int markFailedIfActive(String taskId, int errorCode, String errorMessage, LocalDateTime now) {
        return taskRepository.markFailedIfActive(taskId, errorCode, truncate(errorMessage), now);
    }

    @Transactional
    public int deleteFinishedBefore(LocalDateTime before) {
        return taskRepository.deleteFinishedBefore(before);
    }

    private static String truncate(String msg) {
        if (msg == null) {
            return "";
        }
        return msg.length() <= 255 ? msg : msg.substring(0, 255);
    }
}
