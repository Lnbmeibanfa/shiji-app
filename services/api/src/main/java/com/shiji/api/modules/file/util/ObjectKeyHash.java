package com.shiji.api.modules.file.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.experimental.UtilityClass;

/**
 * 与 {@code file_asset.object_key_hash} 一致：对 {@code bucket + "\\n" + objectKey} 的 UTF-8 字节做 SHA-256，十六进制小写。
 */
@UtilityClass
public class ObjectKeyHash {

    public static String sha256Hex(String bucket, String objectKey) {
        String raw = bucket + "\n" + objectKey;
        byte[] digest = sha256(raw.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
