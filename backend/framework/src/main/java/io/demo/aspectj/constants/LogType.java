package io.demo.aspectj.constants;

/**
 * 操作日志类型枚举类。
 * 用于定义不同操作的日志类型，便于日志分类和处理。
 */
public enum LogType {
    SELECT,
    /**
     * 添加操作
     */
    ADD,

    /**
     * 删除操作
     */
    DELETE,

    /**
     * 更新操作
     */
    UPDATE,

    /**
     * 审核操作
     */
    REVIEW,

    /**
     * 登出操作
     */
    LOGOUT,
    /**
     * 登录操作
     */
    LOGIN,

    /**
     * 复制操作
     */
    COPY;

    /**
     * 判断当前日志类型是否包含给定的关键字。
     *
     * @param keyword 待匹配的日志类型
     * @return 如果当前日志类型包含给定关键字，则返回 true，否则返回 false
     */
    public boolean contains(LogType keyword) {
        return this.name().contains(keyword.name());
    }
}
