package club.ttg.bug.report.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис для хранения файлов (скриншотов) в S3-совместимом хранилище.
 */
public interface FileStorageService {

    /**
     * Загрузка файла в S3-хранилище.
     *
     * @param file загруженный файл
     * @return ключ (имя) файла в бакете
     */
    String store(MultipartFile file);

    /**
     * Получение файла из S3.
     *
     * @param key ключ файла в бакете
     * @return содержимое файла
     */
    StoredFile get(String key);
}
