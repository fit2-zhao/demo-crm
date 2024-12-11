package io.demo.mybatis;

import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Abstract SQL provider support class, providing basic functionality for dynamic SQL generation.
 */
public abstract class AbstractSqlProviderSupport {

    /**
     * Cache for table information structure, supporting efficient queries.
     */
    private static final Map<Class<?>, EntityTable> tableCache = new ConcurrentHashMap<>(256);

    /**
     * Abstract method to generate SQL, implemented by subclasses.
     *
     * @param criteria Query criteria
     * @return SQL instance
     */
    abstract BaseMapper.SQL sql(Object criteria);

    /**
     * Metadata of the current table.
     */
    protected EntityTable table;

    /**
     * Builds SQL based on query criteria and context.
     *
     * @param criteria Query criteria
     * @param context  Context provided by MyBatis
     * @return Constructed SQL script
     */
    String invoke(Object criteria, ProviderContext context) {
        return buildSql(criteria, tableInfo(context));
    }

    /**
     * Constructs SQL script.
     *
     * @param criteria Query criteria
     * @param table    Table metadata
     * @return SQL script string
     */
    String buildSql(Object criteria, EntityTable table) {
        this.table = table;
        BaseMapper.SQL sql = sql(criteria);
        beforeInterceptor(criteria, sql);
        return String.format("<script>%s</script>", sql.toString());
    }

    /**
     * Interceptor before SQL execution, used for handling specific operations.
     *
     * @param obj Query criteria object
     * @param sql Constructed SQL instance
     */
    void beforeInterceptor(Object obj, BaseMapper.SQL sql) {
        if (obj instanceof BaseMapper.Interceptor && this instanceof BaseMapper.WriteType) {
            ((BaseMapper.Interceptor) obj).prePersist();
        }
    }

    /**
     * Filters null values based on query criteria and performs operations on fields.
     *
     * @param criteria Query criteria
     * @param func     Function to operate on fields
     * @param ignorePk Whether to ignore primary key fields
     * @return Array of operated fields
     */
    String[] ignoreNullAndCombined(Object criteria, Function<Field, String> func, boolean ignorePk) {
        return Stream.of(table.getFields())
                .filter(field -> {
                    Object value = value(criteria, field);
                    // Filter empty strings
                    boolean noNull = value != null;
                    return ignorePk ? (noNull && !table.getPrimaryKeyColumn().equals(columnName(field))) : noNull;
                })
                .map(func)
                .toArray(String[]::new);
    }

    /**
     * Filters null values based on query criteria and performs operations on fields (without filtering primary key).
     *
     * @param criteria Query criteria
     * @param func     Function to operate on fields
     * @return Array of operated fields
     */
    String[] ignoreNullAndCombined(Object criteria, Function<Field, String> func) {
        return ignoreNullAndCombined(criteria, func, false);
    }

    /**
     * Retrieves and caches table information structure.
     *
     * @param context Context provided by MyBatis
     * @return Table metadata
     */
    EntityTable tableInfo(ProviderContext context) {
        return tableCache.computeIfAbsent(context.getMapperType(), t -> EntityTableMapper.extractTableInfo(entityType(context)));
    }

    /**
     * Retrieves the generic type in the BaseMapper interface.
     *
     * @param context ProviderContext
     * @return Class object of the generic type
     * @throws IllegalStateException If the generic type of BaseMapper is not found
     */
    Class<?> entityType(ProviderContext context) {
        return Stream.of(context.getMapperType().getGenericInterfaces())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(type -> type.getRawType() == BaseMapper.class)
                .findFirst()
                .map(type -> type.getActualTypeArguments()[0])
                .filter(Class.class::isInstance)
                .map(Class.class::cast)
                .orElseThrow(() -> new IllegalStateException("Generic type of BaseMapper not found for " + context.getMapperType().getName() + "."));
    }

    /**
     * Binds field to parameter.
     *
     * @param field Target field
     * @return Parameter binding expression
     */
    String bindParameter(Field field) {
        return String.format("#{%s}", field.getName());
    }

    /**
     * Retrieves the column name corresponding to the field.
     *
     * @param field Target field
     * @return Column name corresponding to the field
     */
    String columnName(Field field) {
        return EntityTableMapper.getColumnName(field);
    }

    /**
     * Retrieves the value of the specified field in the object.
     *
     * @param bean  Target object
     * @param field Target field
     * @return Value of the field
     */
    Object value(Object bean, Field field) {
        return EntityTableMapper.getFieldValue(bean, field);
    }
}