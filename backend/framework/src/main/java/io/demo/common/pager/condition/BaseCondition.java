package io.demo.common.pager.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 表示 CRM 系统中的基础条件类，用于支持过滤和搜索操作。
 */
@Data
public class BaseCondition {

    @Schema(description = "关键字，用于搜索匹配")
    private String keyword;

    @Schema(description = "过滤字段，包含字段及其对应的过滤值")
    private Map<String, List<String>> filter;

    @Schema(description = "视图 ID，指定使用的视图")
    private String viewId;

    @Schema(description = "高级搜索条件，支持组合搜索")
    @Valid
    private CombineSearch combineSearch;

    /**
     * 转义关键字中的特殊字符。
     *
     * @param keyword 输入的关键字
     * @return 转义后的关键字
     */
    public static String transferKeyword(String keyword) {
        if (StringUtils.contains(keyword, "\\") && !StringUtils.contains(keyword, "\\\\")) {
            keyword = StringUtils.replace(keyword, "\\", "\\\\");
        }
        // 判断是否已经转义过，未转义才进行转义。
        if (StringUtils.contains(keyword, "%") && !StringUtils.contains(keyword, "\\%")) {
            keyword = StringUtils.replace(keyword, "%", "\\%");
        }
        if (StringUtils.contains(keyword, "_") && !StringUtils.contains(keyword, "\\_")) {
            keyword = StringUtils.replace(keyword, "_", "\\_");
        }
        return keyword;
    }

    /**
     * 初始化关键字，直接设置字段值。
     *
     * @param keyword 初始化的关键字
     */
    public void initKeyword(String keyword) {
        this.keyword = keyword;
    }
}
