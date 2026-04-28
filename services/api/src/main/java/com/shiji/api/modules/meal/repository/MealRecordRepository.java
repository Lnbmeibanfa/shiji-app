package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.MealRecordEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecordRepository extends JpaRepository<MealRecordEntity, Long> {

    Optional<MealRecordEntity> findFirstByUserIdAndDeletedAtIsNullAndVisibilityStatusAndRecordedAtLessThanEqualOrderByRecordedAtDescIdDesc(
            long userId, int visibilityStatus, LocalDateTime now);
}
