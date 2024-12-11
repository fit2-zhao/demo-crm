package io.demo.mybatis;

import javax.persistence.Column;
import javax.persistence.Id;
import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility class: EntityTableMapper
 * <p>
 * Provides field operations based on reflection, including field filtering, table information extraction, and other functionalities.
 * </p>
 */
public class EntityTableMapper {

    private static final String FIELD_SEP_TAG = "`";

    /**
     * Retrieves table information for the entity class.
     *
     * @param entityClass The entity class
     * @return The table information object
     */
    public static EntityTable extractTableInfo(Class<?> entityClass) {
        Predicate<Field> fieldFilter = field -> !"serialVersionUID".equals(field.getName());

        Field[] fields = filterFieldsWithoutNoColumnAnnotation(getAllFields(entityClass, fieldFilter));

        EntityTable info = new EntityTable();
        info.setEntityClass(entityClass);
        info.setFields(fields);
        info.setTableName(generateTableName(entityClass));
        info.setPrimaryKeyColumn(determinePrimaryKey(fields, "id"));
        info.setColumns(mapFieldsToColumnNames(fields));
        info.setSelectColumns(mapFieldsToSelectColumnNames(fields));
        return info;
    }

    /**
     * Retrieves the value of the specified field in the object.
     *
     * @param bean  The object instance
     * @param field The field
     * @return The field value
     */
    public static synchronized Object getFieldValue(Object bean, Field field) {
        if (bean == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access field: " + field.getName(), e);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * Determines the column name of the primary key field.
     *
     * @param fields           The array of fields in the entity class
     * @param defaultPrimaryKey The default primary key name
     * @return The primary key column name
     */
    public static String determinePrimaryKey(Field[] fields, String defaultPrimaryKey) {
        return Stream.of(fields)
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .map(EntityTableMapper::getColumnName)
                .orElse(defaultPrimaryKey);
    }

    /**
     * Generates the table name (default using camel case to underscore rule).
     *
     * @param entityType The entity class
     * @return The table name
     */
    public static String generateTableName(Class<?> entityType) {
        return camelToUnderscore(entityType.getSimpleName());
    }

    /**
     * Maps the array of fields to an array of database column names.
     *
     * @param fields The array of fields
     * @return The array of column names
     */
    public static String[] mapFieldsToColumnNames(Field[] fields) {
        return Stream.of(fields)
                .map(EntityTableMapper::getColumnName)
                .toArray(String[]::new);
    }

    /**
     * Filters out fields with the @NoColumn annotation.
     *
     * @param fields The array of fields
     * @return The filtered array of fields
     */
    public static Field[] filterFieldsWithoutNoColumnAnnotation(Field[] fields) {
        return Stream.of(fields)
                .filter(field -> !field.isAnnotationPresent(Transient.class))
                .toArray(Field[]::new);
    }

    /**
     * Maps the array of fields to an array of select column names (supports aliases).
     *
     * @param fields The array of fields
     * @return The array of select column names
     */
    public static String[] mapFieldsToSelectColumnNames(Field[] fields) {
        return Stream.of(fields)
                .map(EntityTableMapper::getSelectColumnName)
                .toArray(String[]::new);
    }

    /**
     * Retrieves the database column name of the field (with underscores).
     *
     * @param field The field
     * @return The database column name
     */
    public static String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        return FIELD_SEP_TAG + camelToUnderscore(field.getName()) + FIELD_SEP_TAG;
    }

    /**
     * Retrieves the select column name of the field (with alias).
     *
     * @param field The field
     * @return The select column name
     */
    public static String getSelectColumnName(Field field) {
        return getColumnName(field) + " AS " + FIELD_SEP_TAG + field.getName() + FIELD_SEP_TAG;
    }

    /**
     * Converts a camel case string to an underscore-separated string.
     *
     * @param camelStr The camel case string
     * @return The underscore-separated string
     */
    public static String camelToUnderscore(String camelStr) {
        return convertCamelToSeparator(camelStr, '_');
    }

    /**
     * Converts a camel case string to a string separated by the specified separator.
     *
     * @param camelStr  The camel case string
     * @param separator The separator
     * @return The separated string
     */
    public static String convertCamelToSeparator(String camelStr, char separator) {
        if (camelStr == null || camelStr.trim().isEmpty()) {
            return camelStr;
        }
        StringBuilder result = new StringBuilder();
        char[] chars = camelStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append(separator);
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Retrieves all fields of the specified class (including parent classes).
     *
     * @param clazz       The class
     * @param fieldFilter The field filter
     * @return The array of fields
     */
    public static Field[] getAllFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (fieldFilter == null || fieldFilter.test(field)) {
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}