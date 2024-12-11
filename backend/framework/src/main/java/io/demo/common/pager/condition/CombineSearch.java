package io.demo.common.pager.condition;

import io.demo.common.constants.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Represents a combined search condition used to support complex search logic.
 * Contains match mode (all/any) and a list of filter conditions.
 */
@Data
public class CombineSearch {

    @Schema(description = "Match mode, supports 'all' or 'any'", allowableValues = {"AND", "OR"})
    @EnumValue(enumClass = SearchMode.class)
    private String searchMode = SearchMode.AND.name();

    @Schema(description = "List of filter conditions used to define multiple search criteria")
    @Valid
    private List<CombineCondition> conditions;

    /**
     * Get the current match mode. If not set, the default is "AND".
     *
     * @return Current match mode
     */
    public String getSearchMode() {
        return StringUtils.isBlank(searchMode) ? SearchMode.AND.name() : searchMode;
    }

    /**
     * Enum: Search mode, defines two match modes: "all" and "any".
     */
    public enum SearchMode {
        /**
         * All conditions match ("and" operation)
         */
        AND,

        /**
         * Any condition matches ("or" operation)
         */
        OR
    }
}