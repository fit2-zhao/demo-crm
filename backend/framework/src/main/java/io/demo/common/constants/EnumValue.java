package io.demo.common.constants;

import io.demo.common.util.Translator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum value validation annotation to ensure the value is a valid value in the specified enum class.
 * Optional exclusion of certain enum values.
 * <p>
 * Author: jianxing
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValue.EnumValueValidator.class)
public @interface EnumValue {

    /**
     * Error message to be used when validation fails.
     *
     * @return Error message
     */
    String message() default "{enum_value_valid_message}";

    /**
     * Required attribute for group validation.
     *
     * @return Group validation classes
     */
    Class<?>[] groups() default {};

    /**
     * Validation payload.
     *
     * @return Validation payload classes
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Enum class to be used for validation.
     *
     * @return Enum class
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * Enum values to be excluded during validation, only supports string type.
     *
     * @return Excluded enum values
     */
    String[] excludeValues() default {};

    /**
     * Implementation class for enum value validation.
     *
     * @see EnumValue
     */
    class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

        private Class<? extends Enum<?>> enumClass;
        private String[] excludeValues;

        @Override
        public void initialize(EnumValue enumValue) {
            this.enumClass = enumValue.enumClass();
            this.excludeValues = enumValue.excludeValues();
        }

        /**
         * Validate if the parameter is among the enum values.
         * If exclusion values are set, the validation value should not be in the exclusion list.
         *
         * @param value   Value to be validated
         * @param context Validation context
         * @return Validation result, returns true if the value is valid, otherwise false
         */
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            // If the value is null, consider it valid
            if (value == null) {
                return true;
            }

            // Get all instances of the enum class
            Enum<?>[] enums = enumClass.getEnumConstants();
            List<Object> values = new ArrayList<>();

            // Get all valid values of the enum class
            for (Enum<?> item : enums) {
                if (item instanceof ValueEnum) {
                    values.add(((ValueEnum<?>) item).getValue());
                } else {
                    values.add(item.name());
                }
            }

            // Determine if the value is excluded
            boolean isExcludeValue = excludeValues != null && Arrays.stream(excludeValues).anyMatch(value::equals);
            boolean valid = values.contains(value) && !isExcludeValue;

            // If validation fails, generate a custom error message
            if (!valid) {
                context.disableDefaultConstraintViolation();
                String errorValues = CollectionUtils.subtract(values, Arrays.asList(excludeValues)).toString();
                context.buildConstraintViolationWithTemplate(Translator.get("enum_value_valid_message") + errorValues).addConstraintViolation();
            }

            return valid;
        }
    }
}