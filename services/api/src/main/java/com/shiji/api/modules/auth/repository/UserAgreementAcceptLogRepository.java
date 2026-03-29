package com.shiji.api.modules.auth.repository;

import com.shiji.api.modules.auth.model.entity.UserAgreementAcceptLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAgreementAcceptLogRepository extends JpaRepository<UserAgreementAcceptLogEntity, Long> {
}
