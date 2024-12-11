package io.demo.common.pager.condition;

import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * <p>Represents the DTO class for pagination requests, extending from the {@link BaseCondition} class,
 * and includes pagination parameters and sorting fields.</p>
 * <p>Used to pass the current page number, number of items per page, and sorting information during pagination queries.</p>
 */
@Data
public class BasePageRequest extends BaseCondition {

    /**
     * Current page number, minimum value is 1
     */
    @Min(value = 1, message = "Current page number must be greater than 0")
    @Schema(description = "Current page number")
    private int current;

    /**
     * Number of items per page, range is 5 to 500
     */
    @Min(value = 5, message = "Number of items per page must be at least 5")
    @Max(value = 500, message = "Number of items per page cannot exceed 500")
    @Schema(description = "Number of items per page")
    private int pageSize;

    /**
     * Sorting fields and sorting order (ascending or descending)
     */
    @Schema(description = "Sorting fields (field in model : asc/desc)")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;

    /**
     * Get the sorting string in the format "column_name ASC/DESC"
     *
     * @return Sorting field string, e.g., "column_name ASC"
     */
    public String getSortString() {
        return getSortString(null, null);
    }

    /**
     * Get the sorting string in the format "table_alias.column_name ASC/DESC"
     *
     * @param defaultColumn  Default sorting field
     * @param tableAliasName Table alias
     * @return Sorting field string, e.g., "table_alias.column_name ASC"
     */
    public String getSortString(String defaultColumn, String tableAliasName) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sort.entrySet()) {
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            if (tableAliasName != null) {
                sb.append(tableAliasName).append(".");
            }
            sb.append(column)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC")
                    .append(",");
        }

        // Remove the last comma
        sb.setLength(sb.length() - 1);

        // If a defaultColumn is provided, add the default sorting field
        if (defaultColumn != null) {
            sb.append(",").append(tableAliasName != null ? tableAliasName + "." : "").append(defaultColumn)
                    .append(StringUtils.SPACE).append("ASC");
        }

        return sb.toString();
    }
}