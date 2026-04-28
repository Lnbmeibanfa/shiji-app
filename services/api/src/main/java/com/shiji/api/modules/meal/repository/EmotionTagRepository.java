package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.EmotionTagEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionTagRepository extends JpaRepository<EmotionTagEntity, Long> {

    Optional<EmotionTagEntity> findFirstByEmotionCodeAndEmotionStatusOrderByIdAsc(String emotionCode, Integer emotionStatus);
}
