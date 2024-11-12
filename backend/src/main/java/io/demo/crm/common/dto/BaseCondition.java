package io.demo.crm.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>表示基本查询条件的 DTO 类。</p>
 * <p>包含了关键字、过滤字段、视图ID等条件，以及对关键字的转义处理。</p>
 */
@Data
public class BaseCondition {

    /**
     * 关键字，用于搜索和过滤
     */
    @Schema(description = "关键字")
    private String keyword;

    /**
     * 过滤条件，使用字段名作为键，字段值列表作为值
     */
    @Schema(description = "过滤字段")
    private Map<String, List<String>> filter;

    /**
     * 视图 ID，用于标识特定的视图
     */
    @Schema(description = "视图ID")
    private String viewId;

    /**
     * 组合条件（废弃字段）
     *
     * @deprecated 请使用新的查询条件代替
     */
    @Deprecated
    private Map<String, Object> combine;

    /**
     * 旧版的搜索模式（废弃字段）
     *
     * @deprecated 请使用新的搜索模式
     */
    @Deprecated
    private String searchMode = "AND";

    /**
     * 设置关键字并处理转义字符
     * <p>当设置关键字时，会处理转义字符，避免在查询中出现错误。</p>
     *
     * @param keyword 关键字
     * @deprecated 使用 {@link #initKeyword(String)} 方法来直接初始化 keyword
     */
    @Deprecated
    public void setKeyword(String keyword) {
        this.keyword = transferKeyword(keyword);
    }

    /**
     * 处理并转义关键字中的特殊字符，例如反斜杠、百分号和下划线。
     *
     * @param keyword 传入的关键字
     * @return 转义后的关键字
     */
    public static String transferKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return keyword; // 如果 keyword 为空，直接返回
        }

        // 转义反斜杠字符
        if (StringUtils.contains(keyword, "\\") && !StringUtils.contains(keyword, "\\\\")) {
            keyword = StringUtils.replace(keyword, "\\", "\\\\");
        }

        // 转义百分号
        if (StringUtils.contains(keyword, "%") && !StringUtils.contains(keyword, "\\%")) {
            keyword = StringUtils.replace(keyword, "%", "\\%");
        }

        // 转义下划线
        if (StringUtils.contains(keyword, "_") && !StringUtils.contains(keyword, "\\_")) {
            keyword = StringUtils.replace(keyword, "_", "\\_");
        }

        return keyword;
    }

    /**
     * 初始化关键字，不进行转义处理。
     *
     * @param keyword 直接设置的关键字
     */
    public void initKeyword(String keyword) {
        this.keyword = keyword;
    }
}
