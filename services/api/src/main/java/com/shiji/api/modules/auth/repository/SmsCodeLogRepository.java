package com.shiji.api.modules.auth.repository;

import com.shiji.api.modules.auth.model.entity.SmsCodeLogEntity;
import com.shiji.api.modules.auth.model.enums.SmsCodeStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsCodeLogRepository extends JpaRepository<SmsCodeLogEntity, Long> {

    long countByPhoneAndSentAtAfter(String phone, LocalDateTime since);

    Optional<SmsCodeLogEntity> findTopByPhoneAndStatusOrderBySentAtDesc(String phone, SmsCodeStatus status);
}
