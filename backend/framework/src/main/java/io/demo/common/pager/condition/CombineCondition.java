package io.demo.common.pager.condition;

import io.demo.common.constants.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Represents a combined condition used to support complex filtering and query logic.
 * Contains information such as field name, operator, and expected value.
 */
@Data
public class CombineCondition {

    @Schema(description = "Parameter name of the condition")
    @NotNull(message = "Parameter name cannot be null")
    private String name;

    @Schema(description = "Expected value, if the operator is BETWEEN, IN, NOT_IN, it is an array, otherwise it is a single value")
    private Object value;

    @Schema(description = "Indicates whether it is a custom field")
    @NotNull(message = "Custom field flag cannot be null")
    private Boolean customField = false;

    @Schema(description = "Type of the custom field")
    private String customFieldType;

    @Schema(description = "Operator",
            allowableValues = {"IN", "NOT_IN", "BETWEEN", "GT", "LT", "COUNT_GT", "COUNT_LT", "EQUALS", "NOT_EQUALS", "CONTAINS", "NOT_CONTAINS", "EMPTY", "NOT_EMPTY"})
    @EnumValue(enumClass = CombineConditionOperator.class)
    private String operator;

    /**
     * Validate whether the condition is valid, checking the validity of the field name, operator, and value.
     *
     * @return true if the condition is valid, otherwise false
     */
    public boolean valid() {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(operator)) {
            return false;
        }

        // Check operator for empty value
        if (StringUtils.equalsAny(operator, CombineConditionOperator.EMPTY.name(), CombineConditionOperator.NOT_EMPTY.name())) {
            return true;
        }

        if (value == null) {
            return false;
        }

        // Validate value if it is a collection type
        if (value instanceof List<?> valueList && CollectionUtils.isEmpty(valueList)) {
            return false;
        }

        // Validate value if it is a string
        if (value instanceof String valueStr && StringUtils.isBlank(valueStr)) {
            return false;
        }

        return true;
    }

    /**
     * Enum: Combined condition operators, defining various possible query operators.
     */
    public enum CombineConditionOperator {
        /**
         * Belongs to a set
         */
        IN,

        /**
         * Does not belong to a set
         */
        NOT_IN,

        /**
         * Range operation
         */
        BETWEEN,

        /**
         * Greater than
         */
        GT,

        /**
         * Less than
         */
        LT,

        /**
         * Count greater than
         */
        COUNT_GT,

        /**
         * Count less than
         */
        COUNT_LT,

        /**
         * Equals
         */
        EQUALS,

        /**
         * Not equals
         */
        NOT_EQUALS,

        /**
         * Contains
         */
        CONTAINS,

        /**
         * Does not contain
         */
        NOT_CONTAINS,

        /**
         * Is empty
         */
        EMPTY,

        /**
         * Is not empty
         */
        NOT_EMPTY
    }
}