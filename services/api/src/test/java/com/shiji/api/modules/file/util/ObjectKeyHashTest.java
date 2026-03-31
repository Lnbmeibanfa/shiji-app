package com.shiji.api.modules.file.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class ObjectKeyHashTest {

    @Test
    void sha256Hex_matchesRawUtf8Newline() throws Exception {
        String bucket = "my-bucket";
        String key = "users/1/meals/20260330/x.jpg";
        String expected =
                HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                        .digest((bucket + "\n" + key).getBytes(StandardCharsets.UTF_8)));
        assertThat(ObjectKeyHash.sha256Hex(bucket, key)).isEqualTo(expected);
    }
}
