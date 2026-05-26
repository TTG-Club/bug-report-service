package com.bugtracker.service;

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
     * Получение публичного URL файла в S3.
     *
     * @param key ключ файла в бакете
     * @return публичный URL файла
     */
    String getFileUrl(String key);
}
