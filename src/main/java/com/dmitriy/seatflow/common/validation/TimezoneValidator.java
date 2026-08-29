package com.dmitriy.seatflow.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DateTimeException;
import java.time.ZoneId;

public class TimezoneValidator
        implements ConstraintValidator<ValidTimezone, String> {

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            ZoneId.of(value);
            return true;
        } catch (DateTimeException exception) {
            return false;
        }
    }
}