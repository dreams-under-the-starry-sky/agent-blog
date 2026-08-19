package com.blog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequiredTextValidator.class)
public @interface RequiredText {
    String message() default "请求参数不正确";

    boolean required() default true;

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String tooShortMessage() default "";

    String tooLongMessage() default "";

    String pattern() default "";

    String patternMessage() default "";

    String forbidden() default "";

    String forbiddenMessage() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
