package io.demo.crm.dao;

import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 抽象 SQL 提供者支持类，提供动态 SQL 生成的基础功能。
 */
public abstract class AbstractSqlProviderSupport {

    /**
     * 用于缓存表信息结构，支持高效查询。
     */
    private static final Map<Class<?>, EntityTable> tableCache = new ConcurrentHashMap<>(256);

    /**
     * 生成 SQL 的抽象方法，由子类实现。
     *
     * @param criteria 查询条件
     * @return SQL 实例
     */
    abstract BaseMapper.SQL sql(Object criteria);

    /**
     * 当前表的元信息。
     */
    protected EntityTable table;

    /**
     * 根据查询条件和上下文构建 SQL。
     *
     * @param criteria 查询条件
     * @param context  MyBatis 提供的上下文
     * @return 构建的 SQL 脚本
     */
    String invoke(Object criteria, ProviderContext context) {
        return buildSql(criteria, tableInfo(context));
    }

    /**
     * 构建 SQL 脚本。
     *
     * @param criteria 查询条件
     * @param table    表的元信息
     * @return SQL 脚本字符串
     */
    String buildSql(Object criteria, EntityTable table) {
        this.table = table;
        BaseMapper.SQL sql = sql(criteria);
        beforeInterceptor(criteria, sql);
        return String.format("<script>%s</script>", sql.toString());
    }

    /**
     * SQL 执行前的拦截器，用于处理特定操作。
     *
     * @param obj 查询条件对象
     * @param sql 构建的 SQL 实例
     */
    void beforeInterceptor(Object obj, BaseMapper.SQL sql) {
        if (obj instanceof BaseMapper.Interceptor && this instanceof BaseMapper.WriteType) {
            ((BaseMapper.Interceptor) obj).prePersist();
        }
    }

    /**
     * 根据查询条件过滤空值并对字段进行操作。
     *
     * @param criteria 查询条件
     * @param func     对字段进行操作的函数
     * @param ignorePk 是否忽略主键字段
     * @return 操作后的字段数组
     */
    String[] ignoreNullAndCombined(Object criteria, Function<Field, String> func, boolean ignorePk) {
        return Stream.of(table.getFields())
                .filter(field -> {
                    Object value = value(criteria, field);
                    // 过滤空字符串
                    boolean noNull = value != null;
                    return ignorePk ? (noNull && !table.getPrimaryKeyColumn().equals(columnName(field))) : noNull;
                })
                .map(func)
                .toArray(String[]::new);
    }

    /**
     * 根据查询条件过滤空值并对字段进行操作（不过滤主键）。
     *
     * @param criteria 查询条件
     * @param func     对字段进行操作的函数
     * @return 操作后的字段数组
     */
    String[] ignoreNullAndCombined(Object criteria, Function<Field, String> func) {
        return ignoreNullAndCombined(criteria, func, false);
    }

    /**
     * 获取并缓存表信息结构。
     *
     * @param context MyBatis 提供的上下文
     * @return 表的元信息
     */
    EntityTable tableInfo(ProviderContext context) {
        return tableCache.computeIfAbsent(context.getMapperType(), t -> EntityTableMapper.extractTableInfo(entityType(context)));
    }

    /**
     * 获取 BaseMapper 接口中的泛型类型。
     *
     * @param context ProviderContext
     * @return 泛型类型对应的 Class 对象
     * @throws IllegalStateException 如果未找到 BaseMapper 的泛型类
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
                .orElseThrow(() -> new IllegalStateException("未找到 BaseMapper 的泛型类 " + context.getMapperType().getName() + "."));
    }

    /**
     * 绑定字段到参数。
     *
     * @param field 目标字段
     * @return 参数绑定表达式
     */
    String bindParameter(Field field) {
        return String.format("#{%s}", field.getName());
    }

    /**
     * 获取字段对应的列名。
     *
     * @param field 目标字段
     * @return 字段对应的列名
     */
    String columnName(Field field) {
        return EntityTableMapper.getColumnName(field);
    }

    /**
     * 获取指定对象中字段的值。
     *
     * @param bean  目标对象
     * @param field 目标字段
     * @return 字段的值
     */
    Object value(Object bean, Field field) {
        return EntityTableMapper.getFieldValue(bean, field);
    }
}
