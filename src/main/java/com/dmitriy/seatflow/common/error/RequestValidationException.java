package com.dmitriy.seatflow.common.error;

/**
 * Исключение для ошибок проверки входных данных,
 * которые требуют бизнес-валидации.
 */
public class RequestValidationException extends RuntimeException {

    public RequestValidationException(String message) {
        super(message);
    }
}