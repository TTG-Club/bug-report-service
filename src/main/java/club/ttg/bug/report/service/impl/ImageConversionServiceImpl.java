package club.ttg.bug.report.service.impl;

import club.ttg.bug.report.service.ImageConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Реализация сервиса конвертации изображений в WebP.
 * Использует webp-imageio для записи в формат WebP через стандартный ImageIO API.
 */
@Slf4j
@Service
public class ImageConversionServiceImpl implements ImageConversionService {

    private static final Set<String> CONVERTIBLE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/bmp",
            "image/gif",
            "image/tiff"
    );

    @Override
    public boolean isConvertibleImage(String contentType) {
        if (contentType == null) {
            return false;
        }
        return CONVERTIBLE_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public byte[] convertToWebp(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                log.warn("Не удалось прочитать изображение для конвертации в WebP, загружаем оригинал");
                return imageBytes;
            }

            // Убираем альфа-канал для JPEG-совместимости при необходимости
            BufferedImage outputImage = image;
            if (image.getType() == BufferedImage.TYPE_INT_ARGB || image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                outputImage.getGraphics().drawImage(image, 0, 0, null);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean written = ImageIO.write(outputImage, "webp", outputStream);

            if (!written) {
                log.warn("WebP ImageWriter не найден, загружаем оригинал");
                return imageBytes;
            }

            byte[] webpBytes = outputStream.toByteArray();
            double compressionPercent = (1.0 - (double) webpBytes.length / imageBytes.length) * 100;
            log.info("Изображение конвертировано в WebP: {} -> {} байт (сжатие {}%)",
                    imageBytes.length, webpBytes.length, String.format("%.1f", compressionPercent));
            return webpBytes;
        } catch (IOException e) {
            log.error("Ошибка конвертации изображения в WebP, загружаем оригинал", e);
            return imageBytes;
        }
    }
}
