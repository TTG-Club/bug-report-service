package club.ttg.bug.report.exception;

/**
 * Исключение при ошибке сохранения файла.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
