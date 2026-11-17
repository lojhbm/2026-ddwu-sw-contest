package com.contest.una.notice.storage;

import com.contest.una.global.exception.ImageTooLargeException;
import com.contest.una.global.exception.ImageUnreadableException;
import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.global.exception.UnsupportedImageFormatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Component
public class NoticeImageStorage {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final Path uploadDirectory;

    public NoticeImageStorage(
            @Value("${app.upload-dir}") String uploadDirectory
    ) {
        this.uploadDirectory = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public String save(MultipartFile image) {
        // 필수 여부 및 빈 파일 검증
        if (image == null || image.isEmpty()) {
            throw new InvalidRequestException(
                    "image",
                    "공지 이미지는 필수입니다."
            );
        }

        // 파일 용량 검증
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ImageTooLargeException();
        }

        // 실제 이미지 형식 검증
        String extension = validateImage(image);

        // 원본 파일명 대신 서버에서 파일명 생성
        String filename = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(filename);

        // 파일 저장
        try {
            Files.createDirectories(uploadDirectory);

            try (InputStream input = image.getInputStream()) {
                Files.copy(input, destination);
            }

            // DB에 저장할 파일명 반환
            return filename;

        } catch (IOException e) {
            // 저장 중 실패했다면 부분적으로 생성된 파일 정리
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw new IllegalStateException(
                    "이미지 저장에 실패했습니다.",
                    e
            );
        }
    }

    private String validateImage(MultipartFile image) {
        try (
                InputStream input = image.getInputStream();
                ImageInputStream imageInput =
                        ImageIO.createImageInputStream(input)
        ) {
            // 이미지로 인식할 수 없는 파일 -> 415
            if (imageInput == null) {
                throw new UnsupportedImageFormatException();
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new UnsupportedImageFormatException();
            }

            ImageReader reader = readers.next();

            try {
                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                // PNG/JPEG가 아닌 형식 -> 415
                String extension = switch (format) {
                    case "jpeg", "jpg" -> "jpg";
                    case "png" -> "png";
                    default -> throw new UnsupportedImageFormatException();
                };

                reader.setInput(imageInput);

                // PNG/JPEG로 인식되지만 실제로 읽을 수 없는 이미지 -> 422
                // (try 안에서 우리가 던지는 예외까지 잡지 않도록 read만 감쌈)
                BufferedImage decoded;
                try {
                    decoded = reader.read(0);
                } catch (IOException | RuntimeException e) {
                    throw new ImageUnreadableException(e);
                }

                if (decoded == null) {
                    throw new ImageUnreadableException();
                }

                return extension;

            } finally {
                reader.dispose();
            }

        } catch (IOException e) {
            // 업로드된 파일 스트림 자체를 읽지 못한 경우
            throw new ImageUnreadableException(e);
        }
    }
}