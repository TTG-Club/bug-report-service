package club.ttg.bug.report.service;

/**
 * Сервис для конвертации изображений в формат WebP.
 */
public interface ImageConversionService {

    /**
     * Проверяет, является ли файл изображением, которое можно конвертировать в WebP.
     *
     * @param contentType MIME-тип файла
     * @return true если файл — изображение (не WebP)
     */
    boolean isConvertibleImage(String contentType);

    /**
     * Конвертирует изображение в формат WebP.
     *
     * @param imageBytes исходные байты изображения
     * @return байты изображения в формате WebP
     */
    byte[] convertToWebp(byte[] imageBytes);
}
