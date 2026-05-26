package com.bugtracker.exception;

/**
 * Исключение при превышении лимита запросов (rate limit).
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
