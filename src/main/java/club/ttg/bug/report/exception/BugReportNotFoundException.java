package club.ttg.bug.report.exception;

/**
 * Исключение при отсутствии баг-репорта.
 */
public class BugReportNotFoundException extends RuntimeException {

    public BugReportNotFoundException(String message) {
        super(message);
    }
}
