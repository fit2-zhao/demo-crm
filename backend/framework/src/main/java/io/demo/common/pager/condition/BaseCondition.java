package io.demo.common.pager.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Represents the base condition class in the CRM system, used to support filtering and search operations.
 */
@Data
public class BaseCondition {

    @Schema(description = "Keyword used for search matching")
    private String keyword;

    @Schema(description = "Filter fields, including fields and their corresponding filter values")
    private Map<String, List<String>> filter;

    @Schema(description = "View ID, specifies the view to be used")
    private String viewId;

    @Schema(description = "Advanced search conditions, supports combined search")
    @Valid
    private CombineSearch combineSearch;

    /**
     * Escape special characters in the keyword.
     *
     * @param keyword Input keyword
     * @return Escaped keyword
     */
    public static String transferKeyword(String keyword) {
        if (StringUtils.contains(keyword, "\\") && !StringUtils.contains(keyword, "\\\\")) {
            keyword = StringUtils.replace(keyword, "\\", "\\\\");
        }
        // Check if it has already been escaped, and escape if not.
        if (StringUtils.contains(keyword, "%") && !StringUtils.contains(keyword, "\\%")) {
            keyword = StringUtils.replace(keyword, "%", "\\%");
        }
        if (StringUtils.contains(keyword, "_") && !StringUtils.contains(keyword, "\\_")) {
            keyword = StringUtils.replace(keyword, "_", "\\_");
        }
        return keyword;
    }

    /**
     * Initialize the keyword by directly setting the field value.
     *
     * @param keyword Initialized keyword
     */
    public void initKeyword(String keyword) {
        this.keyword = keyword;
    }
}