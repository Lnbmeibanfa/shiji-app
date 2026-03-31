package com.shiji.api.modules.file.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.config.security.AuthPrincipal;
import com.shiji.api.modules.file.model.dto.response.FileUploadResponse;
import com.shiji.api.modules.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> upload(
            @AuthenticationPrincipal AuthPrincipal principal, @RequestParam("file") MultipartFile file) {
        long userId = principal.userId();
        return ApiResponse.success(fileStorageService.uploadMealPhoto(userId, file));
    }
}
