package com.blog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class RequiredTextValidator implements ConstraintValidator<RequiredText, String> {
    private RequiredText rule;

    @Override
    public void initialize(RequiredText constraintAnnotation) {
        this.rule = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String text = value == null ? "" : value.trim();
        context.disableDefaultConstraintViolation();
        if (text.isEmpty()) {
            if (!rule.required()) {
                return true;
            }
            return fail(context, rule.message());
        }
        if (text.length() < rule.min()) {
            return fail(context, first(rule.tooShortMessage(), rule.message()));
        }
        if (text.length() > rule.max()) {
            return fail(context, first(rule.tooLongMessage(), rule.message()));
        }
        if (StringUtils.hasText(rule.pattern()) && !text.matches(rule.pattern())) {
            return fail(context, first(rule.patternMessage(), rule.message()));
        }
        if (StringUtils.hasText(rule.forbidden()) && text.equalsIgnoreCase(rule.forbidden().trim())) {
            return fail(context, first(rule.forbiddenMessage(), rule.message()));
        }
        return true;
    }

    private static boolean fail(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    private static String first(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }
}
