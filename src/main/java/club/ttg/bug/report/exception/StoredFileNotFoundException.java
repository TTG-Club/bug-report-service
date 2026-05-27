package club.ttg.bug.report.exception;

public class StoredFileNotFoundException extends RuntimeException {

    public StoredFileNotFoundException(String message) {
        super(message);
    }

    public StoredFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
