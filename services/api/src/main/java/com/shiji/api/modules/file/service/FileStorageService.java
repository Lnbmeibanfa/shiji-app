package com.shiji.api.modules.file.service;

import com.shiji.api.modules.file.model.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadMealPhoto(long userId, MultipartFile file);
}
