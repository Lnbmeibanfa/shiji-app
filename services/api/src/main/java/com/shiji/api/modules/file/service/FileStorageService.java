package com.shiji.api.modules.file.service;

import com.shiji.api.modules.file.model.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadMealPhoto(long userId, MultipartFile file);

    /**
     * 下载用户已上传且可用的餐图对象字节，用于服务端识别等场景。
     *
     * @throws com.shiji.api.modules.file.service.exception.FileBusinessException 文件不存在、无权或 OSS 未配置
     */
    byte[] downloadMealPhotoBytes(long userId, long fileId);
}
