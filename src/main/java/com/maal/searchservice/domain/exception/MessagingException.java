package com.maal.searchservice.domain.exception;
/**
 * Exceção de domínio para falhas de publicação de eventos.
 * É unchecked (extends RuntimeException) para não poluir o código
 * com try-catch onde não faz sentido tratar.
 */
public class MessagingException extends RuntimeException {
    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}