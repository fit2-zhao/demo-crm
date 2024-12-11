package io.demo.mybatis;

import lombok.Getter;

import java.lang.reflect.Field;

/**
 * Mapping class for entity classes and database table information.
 * <p>
 * This class is used to describe the mapping relationship between database tables and entity classes, including table names, fields, primary keys, and other metadata.
 * Mainly used for data mapping and operations in the persistence layer.
 * </p>
 */
public class EntityTable {

    /**
     * The entity type corresponding to the table.
     */
    @Getter
    private Class<?> entityClass;

    /**
     * Fields in the entity type that do not contain the {@code @NoColumn} annotation.
     */
    private Field[] fields;

    /**
     * The name of the database table.
     */
    private String tableName;

    /**
     * The name of the primary key column.
     */
    private String primaryKeyColumn;

    /**
     * All column names of the database table.
     */
    private String[] columns;

    /**
     * Collection of column names used to generate SELECT SQL.
     * <p>
     * If the field name contains an underscore, it will be converted to the form "aa\_bb AS aaBb".
     * </p>
     */
    private String[] selectColumns;

    /**
     * Sets the entity type.
     *
     * @param entityClass The Class object of the entity class
     */
    void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Gets the fields that do not contain the {@code @NoColumn} annotation.
     *
     * @return Array of fields
     */
    Field[] getFields() {
        return fields;
    }

    /**
     * Sets the array of fields.
     *
     * @param fields Array of fields
     */
    void setFields(Field[] fields) {
        this.fields = fields;
    }

    /**
     * Gets the name of the database table.
     *
     * @return Table name
     */
    String getTableName() {
        return tableName;
    }

    /**
     * Sets the name of the database table.
     *
     * @param tableName Table name
     */
    void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Gets the name of the primary key column.
     *
     * @return Primary key column name
     */
    String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    /**
     * Sets the name of the primary key column.
     *
     * @param primaryKeyColumn Primary key column name
     */
    void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    /**
     * Gets all column names of the database table.
     *
     * @return Array of column names
     */
    String[] getColumns() {
        return columns;
    }

    /**
     * Sets all column names of the database table.
     *
     * @param columns Array of column names
     */
    void setColumns(String[] columns) {
        this.columns = columns;
    }

    /**
     * Gets the collection of column names used to generate SELECT SQL.
     *
     * @return Array of column names
     */
    String[] getSelectColumns() {
        return selectColumns;
    }

    /**
     * Sets the collection of column names used to generate SELECT SQL.
     *
     * @param selectColumns Array of column names
     */
    void setSelectColumns(String[] selectColumns) {
        this.selectColumns = selectColumns;
    }
}