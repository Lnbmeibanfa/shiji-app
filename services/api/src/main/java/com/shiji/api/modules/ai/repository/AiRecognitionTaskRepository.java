package com.shiji.api.modules.ai.repository;

import com.shiji.api.modules.ai.model.entity.AiRecognitionTaskEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiRecognitionTaskRepository extends JpaRepository<AiRecognitionTaskEntity, Long> {

    Optional<AiRecognitionTaskEntity> findByTaskId(String taskId);

    Optional<AiRecognitionTaskEntity> findByTaskIdAndUserId(String taskId, Long userId);

    @Query(
            """
            select t.taskId from AiRecognitionTaskEntity t
            where t.status in ('pending', 'processing') and t.createdAt < :cutoff
            """)
    List<String> findActiveTaskIdsCreatedBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update AiRecognitionTaskEntity t
            set t.status = 'processing', t.startedAt = :now, t.updatedAt = :now
            where t.taskId = :taskId and t.status = 'pending'
            """)
    int markProcessingIfPending(@Param("taskId") String taskId, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update AiRecognitionTaskEntity t
            set t.status = 'success', t.resultJson = :json, t.modelName = :modelName,
                t.promptVersion = :promptVersion, t.finishedAt = :now, t.updatedAt = :now
            where t.taskId = :taskId and t.status = 'processing'
            """)
    int markSuccessIfProcessing(
            @Param("taskId") String taskId,
            @Param("json") String json,
            @Param("modelName") String modelName,
            @Param("promptVersion") String promptVersion,
            @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update AiRecognitionTaskEntity t
            set t.status = 'failed', t.errorCode = :errorCode, t.errorMessage = :errorMessage,
                t.finishedAt = :now, t.updatedAt = :now
            where t.taskId = :taskId and t.status in ('pending', 'processing')
            """)
    int markFailedIfActive(
            @Param("taskId") String taskId,
            @Param("errorCode") int errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AiRecognitionTaskEntity t where t.finishedAt < :before")
    int deleteFinishedBefore(@Param("before") LocalDateTime before);
}
