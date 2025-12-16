package org.atty.stm.exception;

// Estende RuntimeException para que o JAX-RS (Quarkus) possa interceptar
// o erro e tratá-lo no ControllerBase
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}