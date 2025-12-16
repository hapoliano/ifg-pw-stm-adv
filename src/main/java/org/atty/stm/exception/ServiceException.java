package org.atty.stm.exception;

/**
 * Exceção base para erros na camada de serviço (Business Logic).
 * Usada para representar erros genéricos ou internos que não
 * se enquadram em NotFound ou Forbidden.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}