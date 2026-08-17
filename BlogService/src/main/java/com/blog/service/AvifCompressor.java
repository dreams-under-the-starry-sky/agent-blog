package com.blog.service;

import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

@Component
public class AvifCompressor {
    private static final Logger log = LoggerFactory.getLogger(AvifCompressor.class);
    private static final int MAX_EDGE = 1600;
    private static final String[] CODECS = { "libaom-av1", "libsvtav1" };

    public boolean compressToAvif(byte[] original, Path dest) {
        BufferedImage src = read(original);
        if (src == null) {
            return false;
        }
        try {
            BufferedImage scaled = toBgr(scale(src));
            Exception last = null;
            for (String codec : CODECS) {
                try {
                    encode(scaled, dest, codec);
                    if (Files.exists(dest) && Files.size(dest) > 0) {
                        return true;
                    }
                } catch (Exception e) {
                    last = e;
                    Files.deleteIfExists(dest);
                }
            }
            if (last != null) {
                log.warn("avif compress failed: {}", last.getMessage());
            }
            return false;
        } catch (Exception e) {
            log.warn("avif compress failed: {}", e.getMessage());
            return false;
        }
    }

    private BufferedImage read(byte[] original) {
        try {
            return ImageIO.read(new ByteArrayInputStream(original));
        } catch (IOException e) {
            return null;
        }
    }

    private BufferedImage scale(BufferedImage src) throws IOException {
        if (src.getWidth() <= MAX_EDGE && src.getHeight() <= MAX_EDGE) {
            return src;
        }
        return Thumbnails.of(src).size(MAX_EDGE, MAX_EDGE).asBufferedImage();
    }

    private BufferedImage toBgr(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return src;
        }
        BufferedImage bgr = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = bgr.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return bgr;
    }

    private void encode(BufferedImage src, Path dest, String codec) throws Exception {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(dest.toFile(), src.getWidth(), src.getHeight());
        recorder.setFormat("avif");
        recorder.setVideoCodecName(codec);
        recorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        recorder.setFrameRate(1);
        recorder.setVideoOption("crf", "32");
        recorder.setVideoOption("cpu-used", "6");
        recorder.setVideoOption("still-picture", "1");
        recorder.setVideoOption("usage", "allintra");
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            recorder.start();
            recorder.record(converter.convert(src));
            recorder.stop();
        } finally {
            recorder.release();
        }
    }
}
