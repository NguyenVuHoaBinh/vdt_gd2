package Viettel.backend.service;

/**
 * Custom exception class for errors in the EmbeddingService.
 */
public class EmbeddingServiceException extends RuntimeException {
    public EmbeddingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
