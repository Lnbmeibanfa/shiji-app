package com.shiji.api.modules.ai.service;

import com.shiji.api.modules.ai.config.RecognitionTaskProperties;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.repository.AiRecognitionTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecognitionTaskScheduler {

    private final AiRecognitionTaskRepository taskRepository;
    private final RecognitionTaskStateService stateService;
    private final RecognitionTaskProperties properties;

    /** 兜底：无轮询时仍将超时的任务置为失败 */
    @Scheduled(fixedDelayString = "${recognition.task.scan-delay-ms:30000}")
    @Transactional
    public void markTimeouts() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(properties.getTimeoutSeconds());
        List<String> ids = taskRepository.findActiveTaskIdsCreatedBefore(cutoff);
        LocalDateTime now = LocalDateTime.now();
        for (String id : ids) {
            stateService.markFailedIfActive(id, AiErrorCode.RECOGNITION_TASK_TIMEOUT, now);
        }
    }

    @Scheduled(cron = "${recognition.task.cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupOldTasks() {
        LocalDateTime before = LocalDateTime.now().minusDays(properties.getRetentionDays());
        int n = stateService.deleteFinishedBefore(before);
        if (n > 0) {
            log.info("removed {} finished recognition task row(s) older than retention", n);
        }
    }
}
