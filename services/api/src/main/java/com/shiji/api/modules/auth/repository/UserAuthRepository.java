package com.shiji.api.modules.auth.repository;

import com.shiji.api.modules.auth.model.entity.UserAuthEntity;
import com.shiji.api.modules.auth.model.enums.AuthType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, Long> {

    Optional<UserAuthEntity> findByAuthTypeAndAuthIdentifier(AuthType authType, String authIdentifier);
}
