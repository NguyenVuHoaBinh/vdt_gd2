package Viettel.backend.exception;

/**
 * Custom exception class for handling cache-related errors in CacheService.
 */
public class CacheServiceException extends RuntimeException {

    public CacheServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}