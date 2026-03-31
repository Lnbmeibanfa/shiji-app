package com.shiji.api.modules.file.model.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

    private final long fileId;
    private final String url;
    private final String objectKey;
    private final String bucket;
    private final String contentType;
    private final long size;
}
