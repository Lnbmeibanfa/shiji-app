package com.shiji.api.modules.ai.service;

import com.shiji.api.modules.ai.config.DashScopeProperties;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisionImagePreprocessor {

    private final DashScopeProperties properties;

    public ProcessedImage process(byte[] sourceBytes) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(sourceBytes));
            if (src == null) {
                throw new AiBusinessException(AiErrorCode.VISION_REQUEST_INVALID);
            }
            int originalWidth = src.getWidth();
            int originalHeight = src.getHeight();
            int maxEdge = properties.getVision().getMaxEdge();
            BufferedImage target = src;
            if (Math.max(originalWidth, originalHeight) > maxEdge) {
                double ratio = maxEdge / (double) Math.max(originalWidth, originalHeight);
                int w = Math.max(1, (int) Math.round(originalWidth * ratio));
                int h = Math.max(1, (int) Math.round(originalHeight * ratio));
                target = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = target.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(src, 0, 0, w, h, null);
                g.dispose();
            }
            byte[] encoded = writeJpeg(target, (float) properties.getVision().getJpegQuality());
            String imageHash = sha256Hex(sourceBytes);
            return new ProcessedImage(encoded, "image/jpeg", originalWidth, originalHeight, target.getWidth(), target.getHeight(), imageHash);
        } catch (AiBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new AiBusinessException(AiErrorCode.VISION_REQUEST_INVALID, e);
        }
    }

    private static byte[] writeJpeg(BufferedImage image, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(Math.max(0.1f, Math.min(quality, 1.0f)));
        writer.setOutput(new MemoryCacheImageOutputStream(baos));
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();
        return baos.toByteArray();
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }

    public record ProcessedImage(
            byte[] bytes,
            String mimeType,
            int originalWidth,
            int originalHeight,
            int processedWidth,
            int processedHeight,
            String originalSha256) {}
}
