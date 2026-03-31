package com.shiji.api.modules.file.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "file_asset")
public class FileAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "storage_provider", nullable = false, length = 32)
    private String storageProvider;

    @Column(nullable = false, length = 128)
    private String bucket;

    @Column(name = "object_key", nullable = false, length = 1024)
    private String objectKey;

    @Column(name = "object_key_hash", nullable = false, length = 64)
    private String objectKeyHash;

    @Column(length = 2048)
    private String url;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Column(name = "file_name", length = 512)
    private String fileName;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(length = 128)
    private String etag;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    private Integer width;

    private Integer height;

    @Column(name = "upload_source", nullable = false, length = 32)
    private String uploadSource;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "biz_type", nullable = false, length = 32)
    private String bizType;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "bound_at")
    private LocalDateTime boundAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    /** 与 MySQL JSON 列对应；H2 测试库使用 VARCHAR。 */
    @Column(name = "ext", length = 4000)
    private String ext;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
