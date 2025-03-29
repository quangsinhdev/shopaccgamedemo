package com.shopaccgame.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class EnumValidator implements ConstraintValidator<EnumValid, Enum<?>> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        Enum<?>[] enumValues = enumClass.getEnumConstants();
        if (enumValues == null) {
            return false;
        }
        for (Enum<?> enumValue : enumValues) {
            if (enumValue.equals(value)) {
                return true;
            }
        }
        return false;
    }
}