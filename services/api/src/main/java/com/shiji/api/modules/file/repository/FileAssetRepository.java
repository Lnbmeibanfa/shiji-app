package com.shiji.api.modules.file.repository;

import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAssetEntity, Long> {}
