package com.shiji.api.modules.file.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.web.util.UriUtils;

/**
 * 虚拟主机风格公网访问 URL：{@code https://{bucket}.{endpointHost}/{objectKey}}，路径分段 URL 编码。
 */
public final class FilePublicUrl {

    private FilePublicUrl() {}

    public static String build(String bucket, String endpoint, String objectKey) {
        String host = endpoint.replace("https://", "").replace("http://", "").trim();
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        String encodedPath =
                Arrays.stream(objectKey.split("/")).map(s -> UriUtils.encodePathSegment(s, StandardCharsets.UTF_8)).collect(Collectors.joining("/"));
        return "https://" + bucket + "." + host + "/" + encodedPath;
    }
}
