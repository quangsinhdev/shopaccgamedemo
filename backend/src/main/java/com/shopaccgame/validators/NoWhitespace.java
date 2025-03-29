package com.shopaccgame.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoWhitespaceValidator.class)
public @interface NoWhitespace {
    String message() default "Không được chứa khoảng trắng";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

