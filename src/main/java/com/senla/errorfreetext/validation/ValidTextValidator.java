package com.senla.errorfreetext.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTextValidator implements ConstraintValidator<ValidText, String> {

    public static final int MIN_TEXT_LENGTH = 3;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        if (value.length() < MIN_TEXT_LENGTH) {
            return false;
        }
        return value.codePoints().anyMatch(Character::isLetter);
    }
}
