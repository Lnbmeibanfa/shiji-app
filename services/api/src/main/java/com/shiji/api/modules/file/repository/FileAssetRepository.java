package com.shiji.api.modules.file.repository;

import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAssetEntity, Long> {

    Optional<FileAssetEntity> findByIdAndUserId(Long id, Long userId);
}
