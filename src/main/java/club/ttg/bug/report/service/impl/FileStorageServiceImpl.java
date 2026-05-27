package club.ttg.bug.report.service.impl;

import club.ttg.bug.report.config.S3Properties;
import club.ttg.bug.report.exception.FileStorageException;
import club.ttg.bug.report.exception.StoredFileNotFoundException;
import club.ttg.bug.report.service.FileStorageService;
import club.ttg.bug.report.service.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
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
        } catch (S3Exception e) {
            log.error(
                    "S3 upload failed for file '{}': status={}, requestId={}, errorCode={}, errorMessage={}",
                    originalFilename,
                    e.statusCode(),
                    e.requestId(),
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : null
            );
            throw new FileStorageException("S3 upload failed for file: " + originalFilename, e);
        } catch (SdkException | IOException e) {
            throw new FileStorageException("Не удалось загрузить файл в S3: " + originalFilename, e);
        }
    }

    @Override
    public StoredFile get(String key) {
        String normalizedKey = normalizeKey(key);

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(normalizedKey)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(getRequest);
            String contentType = response.response().contentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return new StoredFile(response.asByteArray(), contentType);
        } catch (NoSuchKeyException e) {
            throw new StoredFileNotFoundException("File not found in S3: " + normalizedKey, e);
        } catch (S3Exception e) {
            log.error(
                    "S3 download failed for key '{}': status={}, requestId={}, errorCode={}, errorMessage={}",
                    normalizedKey,
                    e.statusCode(),
                    e.requestId(),
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : null
            );
            throw new FileStorageException("S3 download failed for key: " + normalizedKey, e);
        } catch (SdkException e) {
            throw new FileStorageException("Failed to download file from S3: " + normalizedKey, e);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }

        if (!key.startsWith("http://") && !key.startsWith("https://")) {
            return key;
        }

        String path = URI.create(key).getPath();
        String bucketPrefix = "/" + s3Properties.bucket() + "/";
        if (path.startsWith(bucketPrefix)) {
            return path.substring(bucketPrefix.length());
        }

        return path.startsWith("/") ? path.substring(1) : path;
    }
}
