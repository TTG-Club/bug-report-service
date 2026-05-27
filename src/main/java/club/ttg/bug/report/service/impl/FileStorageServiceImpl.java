package club.ttg.bug.report.service.impl;

import club.ttg.bug.report.config.S3Properties;
import club.ttg.bug.report.exception.FileStorageException;
import club.ttg.bug.report.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Реализация сервиса хранения файлов в S3-совместимом хранилище (Beget Cloud).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename);
        String key = "screenshots/" + UUID.randomUUID() + extension;

        try {
            byte[] content = file.getBytes();
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength((long) content.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(content));

            log.info("Файл загружен в S3: {}", key);
            return key;
        } catch (SdkException | IOException e) {
            throw new FileStorageException("Не удалось загрузить файл в S3: " + originalFilename, e);
        }
    }

    @Override
    public String getFileUrl(String key) {
        return s3Properties.endpoint() + "/" + s3Properties.bucket() + "/" + key;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }
}
