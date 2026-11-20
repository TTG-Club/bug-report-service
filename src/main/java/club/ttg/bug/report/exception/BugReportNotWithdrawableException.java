package club.ttg.bug.report.exception;

/**
 * Автор пытается отозвать баг-репорт, который уже взяли в работу (статус не NEW).
 */
public class BugReportNotWithdrawableException extends RuntimeException {

    public BugReportNotWithdrawableException(String message) {
        super(message);
    }
}
