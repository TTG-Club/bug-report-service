package com.bugtracker.exception;

/**
 * Исключение при отсутствии баг-репорта.
 */
public class BugReportNotFoundException extends RuntimeException {

    public BugReportNotFoundException(String message) {
        super(message);
    }
}
