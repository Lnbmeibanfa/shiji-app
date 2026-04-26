package com.shiji.api.modules.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.shiji.api.modules.file.config.OssProperties;
import com.shiji.api.modules.file.model.dto.FileErrorCode;
import com.shiji.api.modules.file.model.dto.response.FileUploadResponse;
import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import com.shiji.api.modules.file.repository.FileAssetRepository;
import com.shiji.api.modules.file.service.FilePublicUrl;
import com.shiji.api.modules.file.service.FileStorageService;
import com.shiji.api.modules.file.service.exception.FileBusinessException;
import com.shiji.api.modules.file.util.ObjectKeyHash;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");
    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final OssProperties ossProperties;
    private final FileAssetRepository fileAssetRepository;
    private final OSS ossClient;

    @Autowired
    public FileStorageServiceImpl(
            OssProperties ossProperties,
            FileAssetRepository fileAssetRepository,
            @Autowired(required = false) OSS ossClient) {
        this.ossProperties = ossProperties;
        this.fileAssetRepository = fileAssetRepository;
        this.ossClient = ossClient;
    }

    @Override
    @Transactional
    public FileUploadResponse uploadMealPhoto(long userId, MultipartFile file) {
        
        if (!ossProperties.isEnabled() || ossClient == null) {
            throw new FileBusinessException(FileErrorCode.OSS_NOT_CONFIGURED);
        }
        if (file == null || file.isEmpty()) {
            throw new FileBusinessException(FileErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > ossProperties.getMaxFileSizeBytes()) {
            throw new FileBusinessException(FileErrorCode.FILE_TOO_LARGE);
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileBusinessException(FileErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        String bucket = ossProperties.getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new FileBusinessException(FileErrorCode.OSS_NOT_CONFIGURED);
        }

        String originalName = file.getOriginalFilename();
        String objectKey = buildObjectKey(userId, originalName, contentType);
        String objectKeyHash = ObjectKeyHash.sha256Hex(bucket, objectKey);
        String url = FilePublicUrl.build(bucket, ossProperties.getEndpoint(), objectKey);

        PutObjectResult putResult;
        try (InputStream in = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            meta.setContentType(contentType);
            putResult = ossClient.putObject(bucket, objectKey, in, meta);
        } catch (OSSException | ClientException e) {
            log.warn("OSS putObject failed: userId={}, key={}", userId, objectKey, e);
            throw new FileBusinessException(FileErrorCode.OSS_UPLOAD_FAILED);
        } catch (IOException e) {
            log.warn("read file failed: userId={}, key={}", userId, objectKey, e);
            throw new FileBusinessException(FileErrorCode.OSS_UPLOAD_FAILED);
        }

        String etag = putResult.getETag();
        if (etag != null) {
            etag = etag.replace("\"", "");
        }

        LocalDateTime now = LocalDateTime.now();
        FileAssetEntity entity = new FileAssetEntity();
        entity.setUserId(userId);
        entity.setStorageProvider("aliyun_oss");
        entity.setBucket(bucket);
        entity.setObjectKey(objectKey);
        entity.setObjectKeyHash(objectKeyHash);
        entity.setUrl(url);
        entity.setFileName(originalName);
        entity.setContentType(contentType);
        entity.setFileSize(file.getSize());
        entity.setEtag(etag);
        entity.setUploadSource("backend_proxy");
        entity.setStatus("uploaded");
        entity.setBizType("meal_photo");
        entity.setUploadedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        fileAssetRepository.save(entity);

        return FileUploadResponse.builder()
                .fileId(entity.getId())
                .url(url)
                .objectKey(objectKey)
                .bucket(bucket)
                .contentType(contentType)
                .size(file.getSize())
                .build();
    }

    @Override
    public byte[] downloadMealPhotoBytes(long userId, long fileId) {
        Optional<FileAssetEntity> mine = fileAssetRepository.findByIdAndUserId(fileId, userId);
        if (mine.isEmpty()) {
            if (fileAssetRepository.existsById(fileId)) {
                throw new FileBusinessException(FileErrorCode.FILE_ACCESS_DENIED);
            }
            throw new FileBusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        FileAssetEntity file = mine.get();
        if (!"uploaded".equals(file.getStatus()) || file.getDeletedAt() != null) {
            throw new FileBusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        if (!"meal_photo".equals(file.getBizType())) {
            throw new FileBusinessException(FileErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        if (!ossProperties.isEnabled() || ossClient == null) {
            throw new FileBusinessException(FileErrorCode.OSS_NOT_CONFIGURED);
        }
        String bucket = file.getBucket();
        String objectKey = file.getObjectKey();
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(objectKey)) {
            throw new FileBusinessException(FileErrorCode.OSS_UPLOAD_FAILED);
        }
        try (InputStream in = ossClient.getObject(bucket, objectKey).getObjectContent();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        } catch (OSSException | ClientException e) {
            log.warn("OSS getObject failed: userId={}, fileId={}, key={}", userId, fileId, objectKey, e);
            throw new FileBusinessException(FileErrorCode.OSS_UPLOAD_FAILED);
        } catch (IOException e) {
            log.warn("read OSS object failed: userId={}, fileId={}", userId, fileId, e);
            throw new FileBusinessException(FileErrorCode.OSS_UPLOAD_FAILED);
        }
    }

    private static String normalizeContentType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        int semi = lower.indexOf(';');
        if (semi >= 0) {
            lower = lower.substring(0, semi).trim();
        }
        return lower;
    }

    private static String buildObjectKey(long userId, String originalFilename, String contentType) {
        String day = LocalDateTime.now(SHANGHAI).format(DAY);
        String id = UUID.randomUUID().toString().replace("-", "");
        String ext = resolveExtension(originalFilename, contentType);
        return String.format(Locale.ROOT, "users/%d/meals/%s/%s.%s", userId, day, id, ext);
    }

    private static String resolveExtension(String originalFilename, String contentType) {
        String fromName = "";
        if (StringUtils.hasText(originalFilename)) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                fromName = originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
            }
        }
        if (fromName.matches("jpg|jpeg")) {
            return "jpg";
        }
        if (fromName.equals("png")) {
            return "png";
        }
        if (fromName.equals("webp")) {
            return "webp";
        }
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
