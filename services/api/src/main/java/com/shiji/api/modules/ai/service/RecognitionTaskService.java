package com.shiji.api.modules.ai.service;

import com.shiji.api.modules.ai.config.RecognitionTaskProperties;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.model.dto.response.CreateMealPhotoRecognitionTaskResponse;
import com.shiji.api.modules.ai.model.dto.response.DishIngredientVisionResponse;
import com.shiji.api.modules.ai.model.dto.response.MealPhotoRecognitionTaskPollResponse;
import com.shiji.api.modules.ai.model.entity.AiRecognitionTaskEntity;
import com.shiji.api.modules.ai.repository.AiRecognitionTaskRepository;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import com.shiji.api.modules.file.repository.FileAssetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class RecognitionTaskService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AiRecognitionTaskRepository taskRepository;
    private final FileAssetRepository fileAssetRepository;
    private final RecognitionTaskProperties taskProperties;
    private final RecognitionTaskStateService stateService;
    private final RecognitionTaskAsyncWorker asyncWorker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public CreateMealPhotoRecognitionTaskResponse create(long userId, long fileId) {
        validateMealPhotoFile(userId, fileId);
        LocalDateTime now = LocalDateTime.now();
        String taskId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        AiRecognitionTaskEntity e = new AiRecognitionTaskEntity();
        e.setTaskId(taskId);
        e.setRequestId(requestId);
        e.setUserId(userId);
        e.setFileId(fileId);
        e.setScene("meal_record");
        e.setStatus("pending");
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        taskRepository.save(e);
        scheduleProcessAfterCommit(taskId);
        return CreateMealPhotoRecognitionTaskResponse.builder().taskId(taskId).status("pending").build();
    }

    private void scheduleProcessAfterCommit(String taskId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            asyncWorker.processTask(taskId);
                        }
                    });
        } else {
            asyncWorker.processTask(taskId);
        }
    }

    @Transactional
    public MealPhotoRecognitionTaskPollResponse poll(long userId, String taskId) {
        AiRecognitionTaskEntity e = taskRepository
                .findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new AiBusinessException(AiErrorCode.RECOGNITION_TASK_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        applyLazyTimeout(e.getTaskId(), e.getStatus(), e.getCreatedAt(), now);
        entityManager.clear();
        e = taskRepository
                .findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new AiBusinessException(AiErrorCode.RECOGNITION_TASK_NOT_FOUND));
        return toPollResponse(e);
    }

    private MealPhotoRecognitionTaskPollResponse toPollResponse(AiRecognitionTaskEntity e) {
        String status = e.getStatus();
        if ("success".equals(status)) {
            try {
                DishIngredientVisionResponse result =
                        objectMapper.readValue(e.getResultJson(), DishIngredientVisionResponse.class);
                return MealPhotoRecognitionTaskPollResponse.builder()
                        .taskId(e.getTaskId())
                        .status(status)
                        .result(result)
                        .errorCode(null)
                        .errorMessage(null)
                        .build();
            } catch (Exception ex) {
                throw new AiBusinessException(AiErrorCode.MODEL_OUTPUT_INVALID, ex);
            }
        }
        if ("failed".equals(status)) {
            return MealPhotoRecognitionTaskPollResponse.builder()
                    .taskId(e.getTaskId())
                    .status(status)
                    .result(null)
                    .errorCode(e.getErrorCode())
                    .errorMessage(e.getErrorMessage())
                    .build();
        }
        return MealPhotoRecognitionTaskPollResponse.builder()
                .taskId(e.getTaskId())
                .status(status)
                .result(null)
                .errorCode(null)
                .errorMessage(null)
                .build();
    }

    private void applyLazyTimeout(String taskId, String status, LocalDateTime createdAt, LocalDateTime now) {
        if (!"pending".equals(status) && !"processing".equals(status)) {
            return;
        }
        LocalDateTime limit = createdAt.plusSeconds(taskProperties.getTimeoutSeconds());
        if (now.isBefore(limit)) {
            return;
        }
        stateService.markFailedIfActive(taskId, AiErrorCode.RECOGNITION_TASK_TIMEOUT, now);
    }

    private void validateMealPhotoFile(long userId, long fileId) {
        Optional<FileAssetEntity> mine = fileAssetRepository.findByIdAndUserId(fileId, userId);
        if (mine.isPresent()) {
            FileAssetEntity file = mine.get();
            if (!"uploaded".equals(file.getStatus()) || file.getDeletedAt() != null) {
                throw new AiBusinessException(AiErrorCode.RECOGNITION_FILE_INVALID);
            }
            if (!"meal_photo".equals(file.getBizType())) {
                throw new AiBusinessException(AiErrorCode.RECOGNITION_FILE_INVALID);
            }
            return;
        }
        if (fileAssetRepository.existsById(fileId)) {
            throw new AiBusinessException(AiErrorCode.RECOGNITION_FILE_INVALID);
        }
        throw new AiBusinessException(AiErrorCode.RECOGNITION_FILE_INVALID);
    }
}
