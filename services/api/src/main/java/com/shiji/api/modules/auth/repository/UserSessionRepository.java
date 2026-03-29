package com.shiji.api.modules.auth.repository;

import com.shiji.api.modules.auth.model.entity.UserSessionEntity;
import com.shiji.api.modules.auth.model.enums.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {

    Optional<UserSessionEntity> findByTokenHashAndSessionStatusAndExpireAtAfter(
            String tokenHash,
            SessionStatus sessionStatus,
            LocalDateTime now
    );

    List<UserSessionEntity> findAllByUserIdAndSessionStatus(Long userId, SessionStatus sessionStatus);
}
