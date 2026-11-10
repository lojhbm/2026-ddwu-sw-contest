package com.contest.una.notice.storage;

import com.contest.una.global.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
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
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "이미지 용량 제한을 초과했습니다."
            );
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
            if (imageInput == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();

            try {
                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                String extension = switch (format) {
                    case "jpeg", "jpg" -> "jpg";
                    case "png" -> "png";
                    default -> throw invalidImage();
                };

                reader.setInput(imageInput);

                // 실제로 이미지를 읽을 수 있는지 검증
                if (reader.read(0) == null) {
                    throw invalidImage();
                }

                return extension;

            } finally {
                reader.dispose();
            }

        } catch (IOException e) {
            throw invalidImage();
        }
    }

    private InvalidRequestException invalidImage() {
        return new InvalidRequestException(
                "image",
                "읽을 수 있는 PNG 또는 JPEG 이미지를 업로드해 주세요."
        );
    }
}
