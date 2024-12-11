package io.demo.mybatis.lambda;

import java.util.ArrayList;
import java.util.List;

/**
 * LambdaQueryWrapper is used to construct SQL query conditions, supporting chained calls.
 * It dynamically specifies query fields through Lambda expressions, avoiding hard-coded field names, enhancing code maintainability and type safety.
 *
 * @param <T> The entity type
 */
public class LambdaQueryWrapper<T> {
    // Stores query conditions
    private final List<String> conditions = new ArrayList<>();

    // Stores order by clauses
    private final List<String> orderByClauses = new ArrayList<>();

    /**
     * Adds an equality condition (=).
     *
     * @param column The Lambda expression of the column name
     * @param value  The value
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> eq(XFunction<T, ?> column, Object value) {
        addCondition(columnToString(column) + " = " + formatValue(value));
        return this;
    }

    /**
     * Adds a like condition (LIKE).
     *
     * @param column The Lambda expression of the column name
     * @param value  The value
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> like(XFunction<T, ?> column, Object value) {
        addCondition(columnToString(column) + " LIKE " + formatValue("%" + value + "%"));
        return this;
    }

    /**
     * Adds a greater than condition (>).
     *
     * @param column The Lambda expression of the column name
     * @param value  The value
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> gt(XFunction<T, ?> column, Object value) {
        addCondition(columnToString(column) + " > " + formatValue(value));
        return this;
    }

    /**
     * Adds a less than condition (<).
     *
     * @param column The Lambda expression of the column name
     * @param value  The value
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> lt(XFunction<T, ?> column, Object value) {
        addCondition(columnToString(column) + " < " + formatValue(value));
        return this;
    }

    /**
     * Adds a between condition (BETWEEN).
     *
     * @param column The Lambda expression of the column name
     * @param value1 The start value
     * @param value2 The end value
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> between(XFunction<T, ?> column, Object value1, Object value2) {
        addCondition(columnToString(column) + " BETWEEN " + formatValue(value1) + " AND " + formatValue(value2));
        return this;
    }

    /**
     * Adds an IN condition.
     *
     * @param column The Lambda expression of the column name
     * @param values The collection of values
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> in(XFunction<T, ?> column, List<?> values) {
        String inValues = String.join(", ", values.stream().map(this::formatValue).toArray(String[]::new));
        addCondition(columnToString(column) + " IN (" + inValues + ")");
        return this;
    }

    /**
     * Adds an ascending order condition.
     *
     * @param column The Lambda expression of the column name
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> orderByAsc(XFunction<T, ?> column) {
        orderByClauses.add(columnToString(column) + " ASC");
        return this;
    }

    /**
     * Adds a descending order condition.
     *
     * @param column The Lambda expression of the column name
     * @return The current LambdaQueryWrapper instance
     */
    public LambdaQueryWrapper<T> orderByDesc(XFunction<T, ?> column) {
        orderByClauses.add(columnToString(column) + " DESC");
        return this;
    }

    /**
     * Gets the string of the WHERE clause.
     *
     * @return The string of the WHERE clause
     */
    public String getWhereClause() {
        return String.join(" AND ", conditions);
    }

    /**
     * Gets the string of the ORDER BY clause.
     *
     * @return The string of the ORDER BY clause
     */
    public String getOrderByClause() {
        return orderByClauses.isEmpty() ? "" : String.join(", ", orderByClauses);
    }

    /**
     * Gets the final SQL query string, including WHERE and ORDER BY clauses.
     *
     * @return The complete SQL query string
     */
    public String getSql() {
        String where = getWhereClause();
        String orderBy = getOrderByClause();
        return (where.isEmpty() ? "" : "WHERE " + where) +
                (orderBy.isEmpty() ? "" : " " + orderBy);
    }

    /**
     * Internal method: Adds a condition to the list of query conditions.
     *
     * @param condition The condition string
     */
    private void addCondition(String condition) {
        if (condition != null && !condition.trim().isEmpty()) {
            conditions.add(condition);
        }
    }

    /**
     * Internal method: Converts a Lambda expression to a field name.
     *
     * @param column The Lambda expression of the column name
     * @return The converted field name
     */
    private String columnToString(XFunction<T, ?> column) {
        return LambdaUtils.extract(column);
    }

    /**
     * Internal method: Formats a value for correct use in SQL queries.
     *
     * @param value The value to format
     * @return The formatted value
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
    }
}