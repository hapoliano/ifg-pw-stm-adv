package org.atty.stm.exception;

// Estende RuntimeException para garantir a transação e o tratamento no Controller
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}