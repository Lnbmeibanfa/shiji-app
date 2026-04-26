package com.shiji.api.modules.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.model.dto.response.DishIngredientVisionResponse;
import com.shiji.api.modules.ai.model.entity.AiRecognitionTaskEntity;
import com.shiji.api.modules.ai.repository.AiRecognitionTaskRepository;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import com.shiji.api.modules.file.service.FileStorageService;
import com.shiji.api.modules.file.service.exception.FileBusinessException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecognitionTaskAsyncWorker {

    private final RecognitionTaskStateService stateService;
    private final AiRecognitionTaskRepository taskRepository;
    private final FileStorageService fileStorageService;
    private final DishIngredientVisionService visionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async("recognitionTaskExecutor")
    public void processTask(String taskId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = stateService.markProcessingIfPending(taskId, now);
        if (updated == 0) {
            return;
        }
        AiRecognitionTaskEntity task = taskRepository.findByTaskId(taskId).orElse(null);
        if (task == null) {
            return;
        }
        try {
            byte[] bytes = fileStorageService.downloadMealPhotoBytes(task.getUserId(), task.getFileId());
            DishIngredientVisionResponse response =
                    visionService.recognizeFromBytes(bytes, task.getRequestId());
            String json = objectMapper.writeValueAsString(response);
            Map<String, Object> meta = response.recognition().modelMeta();
            String modelName = meta == null ? null : stringOrNull(meta.get("model"));
            String promptVersion = meta == null ? null : stringOrNull(meta.get("prompt_version"));
            int ok = stateService.markSuccessIfProcessing(
                    taskId, json, modelName, promptVersion, LocalDateTime.now());
            if (ok == 0) {
                log.debug("recognition task {} finished late, result ignored", taskId);
            }
        } catch (AiBusinessException e) {
            stateService.markFailedIfActive(
                    taskId, e.getErrorCode().getCode(), e.getMessage(), LocalDateTime.now());
        } catch (FileBusinessException e) {
            stateService.markFailedIfActive(
                    taskId,
                    AiErrorCode.RECOGNITION_FILE_READ_FAILED.getCode(),
                    AiErrorCode.RECOGNITION_FILE_READ_FAILED.getMessage(),
                    LocalDateTime.now());
        } catch (Exception e) {
            log.warn("recognition task failed: taskId={}", taskId, e);
            stateService.markFailedIfActive(
                    taskId,
                    AiErrorCode.MODEL_OUTPUT_INVALID.getCode(),
                    AiErrorCode.MODEL_OUTPUT_INVALID.getMessage(),
                    LocalDateTime.now());
        }
    }

    private static String stringOrNull(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
