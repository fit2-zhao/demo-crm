package io.demo.crm.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.AbstractSQL;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * 通用 Mapper接口，提供基本的增删改查方法。
 *
 * @param <E> 实体类型
 */
public interface BaseMapper<E> {

    /**
     * 插入一条记录。
     *
     * @param entity 要插入的实体
     * @return 插入的行数
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "invoke")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insert(E entity);

    /**
     * 批量插入记录。
     *
     * @param entities 要插入的实体列表
     * @return 插入的行数
     */
    @InsertProvider(type = BatchInsertSqlProvider.class, method = "invoke")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer batchInsert(List<E> entities);

    /**
     * 根据主键更新记录。
     *
     * @param entity 要更新的实体
     * @return 更新的行数
     */
    @UpdateProvider(type = UpdateSqlProvider.class, method = "invoke")
    Integer updateById(E entity);

    /**
     * 选择性更新记录（仅更新非空字段）。
     *
     * @param entity 要更新的实体
     * @return 更新的行数
     */
    @UpdateProvider(type = UpdateSelectiveSqlProvider.class, method = "invoke")
    Integer update(E entity);

    /**
     * 根据主键删除记录。
     *
     * @param id 主键值
     * @return 删除的行数
     */
    @DeleteProvider(type = DeleteSqlProvider.class, method = "invoke")
    Integer deleteByPrimaryKey(Serializable id);

    /**
     * 根据条件删除记录。
     *
     * @param criteria 删除的条件
     * @return 删除的行数
     */
    @DeleteProvider(type = DeleteByCriteriaSqlProvider.class, method = "invoke")
    Integer delete(E criteria);

    /**
     * 根据主键查询记录。
     *
     * @param id 主键值
     * @return 查询到的实体
     */
    @SelectProvider(type = SelectByIdSqlProvider.class, method = "invoke")
    E selectByPrimaryKey(Serializable id);

    /**
     * 查询所有记录。
     *
     * @param orderBy 排序条件
     * @return 查询到的实体列表
     */
    @SelectProvider(type = SelectAllSqlProvider.class, method = "invoke")
    List<E> selectAll(String orderBy);

    /**
     * 根据条件查询记录。
     *
     * @param criteria 查询条件
     * @return 查询到的实体列表
     */
    @SelectProvider(type = SelectByCriteriaSqlProvider.class, method = "invoke")
    List<E> select(E criteria);

    /**
     * 根据条件查询单条记录。
     *
     * @param criteria 查询条件
     * @return 查询到的实体
     */
    @SelectProvider(type = SelectByCriteriaSqlProvider.class, method = "invoke")
    E selectOne(E criteria);

    /**
     * 根据列和值查询记录。
     *
     * @param column 列名
     * @param ids    查询的值数组
     * @return 查询到的实体列表
     */
    @SelectProvider(type = SelectInSqlProvider.class, method = "invoke")
    List<E> selectByColumn(@Param("column") String column, @Param("array") Serializable[] ids);

    /**
     * 根据条件统计记录数。
     *
     * @param criteria 查询条件
     * @return 记录数
     */
    @SelectProvider(type = CountByCriteriaSqlProvider.class, method = "invoke")
    Long countByExample(E criteria);

    /**
     * 自定义SQL查询。
     *
     * @param sqlBuild SQL构建函数
     * @param criteria 查询条件
     * @return 查询到的实体列表
     */
    @SelectProvider(type = SelectBySqlProvider.class, method = "invoke")
    List<E> query(@Param("sqlBuild") Function<SQL, SQL> sqlBuild, @Param("entity") Object criteria);

    /**
     * 根据主键数组查询记录。
     *
     * @param ids 主键值数组
     * @return 查询到的实体列表
     */
    default List<E> selectByIds(@Param("array") Serializable[] ids) {
        return selectByColumn("id", ids);
    }

    /**
     * 判断记录是否存在。
     *
     * @param criteria 查询条件
     * @return 如果存在返回true，否则返回false
     */
    default boolean exist(E criteria) {
        Long count = countByExample(criteria);
        return count != null && count > 0;
    }

    /**
     * 插入或更新记录。
     *
     * @param criteria 查询条件
     * @return 执行的行数
     */
    default Integer upsert(E criteria) {
        return exist(criteria) ? updateById(criteria) : insert(criteria);
    }

    class InsertSqlProvider extends AbstractSqlProviderSupport implements WriteType {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .INSERT_INTO(table.getTableName())
                    .INTO_COLUMNS(table.getColumns())
                    .INTO_VALUES(Stream.of(table.getFields()).map(this::bindParameter).toArray(String[]::new));
        }
    }

    @SuppressWarnings("all")
    class BatchInsertSqlProvider extends AbstractSqlProviderSupport implements WriteType {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .INSERT_INTO(table.getTableName())
                    .INTO_COLUMNS(table.getColumns())
                    .INTO_VALUES(Stream.of(table.getFields()).map(this::bindParameter).toArray(String[]::new));
        }
    }

    @SuppressWarnings("all")
    class UpdateSqlProvider extends AbstractSqlProviderSupport implements WriteType {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .UPDATE(table.getTableName())
                    .SET(Stream.of(table.getFields())
                            .filter(field -> !table.getPrimaryKeyColumn().equals(columnName(field)))
                            .map(field -> columnName(field) + " = " + bindParameter(field)).toArray(String[]::new))
                    .WHERE(table.getPrimaryKeyColumn() + " = #{id}");
        }
    }

    @SuppressWarnings("all")
    class UpdateSelectiveSqlProvider extends AbstractSqlProviderSupport implements WriteType {
        @Override
        public SQL sql(Object entity) {
            return new SQL()
                    .UPDATE(table.getTableName())
                    .SET(ignoreNullAndCombined(entity, field -> columnName(field) + " = " + bindParameter(field), true))
                    .WHERE(table.getPrimaryKeyColumn() + " = #{id}");
        }
    }

    class DeleteSqlProvider extends AbstractSqlProviderSupport implements WriteType {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .DELETE_FROM(table.getTableName())
                    .WHERE(table.getPrimaryKeyColumn() + " = #{id}");
        }
    }

    class DeleteByCriteriaSqlProvider extends AbstractSqlProviderSupport implements WriteType {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .DELETE_FROM(table.getTableName())
                    .WHERE(ignoreNullAndCombined(criteria, field -> columnName(field) + " = " + bindParameter(field)));
        }
    }

    class SelectByIdSqlProvider extends AbstractSqlProviderSupport {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .SELECT(table.getSelectColumns())
                    .FROM(table.getTableName())
                    .WHERE(table.getPrimaryKeyColumn() + " = #{id}");
        }
    }

    class SelectAllSqlProvider extends AbstractSqlProviderSupport {
        @Override
        public SQL sql(Object criteria) {
            String orderBy = (String) criteria;
            SQL sql = new SQL()
                    .SELECT(table.getSelectColumns())
                    .FROM(table.getTableName());
            if (StringUtils.isBlank(orderBy)) {
                orderBy = table.getPrimaryKeyColumn() + " DESC";
            }
            return sql.ORDER_BY(orderBy);
        }
    }

    @SuppressWarnings("all")
    class SelectInSqlProvider extends AbstractSqlProviderSupport {
        @Override
        public SQL sql(Object entities) {
            Map<String, Object> param = (Map) entities;
            String inField = (String) param.get("column");
            Serializable[] ids = (Serializable[]) param.get("array");
            String idStr = " <foreach item='item' collection='array' open='(' separator=',' close=')'>#{item}</foreach> ";
            String where = (ids != null && ids.length > 0) ? (inField + " IN " + idStr) : " 1=1 ";

            return new SQL()
                    .SELECT(table.getSelectColumns())
                    .FROM(table.getTableName())
                    .WHERE(where);
        }
    }

    class SelectByCriteriaSqlProvider extends AbstractSqlProviderSupport {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .SELECT(table.getSelectColumns())
                    .FROM(table.getTableName())
                    .WHERE(ignoreNullAndCombined(criteria, field -> columnName(field) + " = " + bindParameter(field)))
                    .ORDER_BY(table.getPrimaryKeyColumn() + " DESC");
        }
    }

    class SelectBySqlProvider extends AbstractSqlProviderSupport {
        @SuppressWarnings("all")
        @Override
        public SQL sql(Object entities) {
            Map<String, Object> param = (Map) entities;
            Function<SQL, SQL> sqlBuild = (Function) param.get("sqlBuild");
            Object criteria = param.get("entity");

            return sqlBuild.apply(
                    new SQL().FROM(table.getTableName())
            );
        }
    }

    class CountByCriteriaSqlProvider extends AbstractSqlProviderSupport {
        @Override
        public SQL sql(Object criteria) {
            return new SQL()
                    .SELECT("COUNT(*)")
                    .FROM(table.getTableName())
                    .WHERE(ignoreNullAndCombined(criteria, field -> columnName(field) + " = " + bindParameter(field)));
        }
    }

    interface Interceptor {
        /**
         * 类似 @PrePersist
         **/
        default void prePersist() {
        }
    }

    interface WriteType {
    }

    class SQL extends AbstractSQL<SQL> {
        public SQL WHERE(boolean test, String condition) {
            return test ? super.WHERE(condition) : this;
        }

        @Override
        public SQL getSelf() {
            return this;
        }
    }

    abstract class AbstractSqlProviderSupport {
        private static final Map<Class<?>, EntityTable> tableCache = new ConcurrentHashMap<>(256);

        abstract SQL sql(Object criteria);

        protected EntityTable table;

        String invoke(Object criteria, ProviderContext context) {
            return buildSql(criteria, tableInfo(context));
        }

        String buildSql(Object criteria, EntityTable table) {
            this.table = table;
            SQL sql = sql(criteria);
            beforeInterceptor(criteria, sql);
            return String.format("<script>%s</script>", sql.toString());
        }

        void beforeInterceptor(Object obj, SQL sql) {
            if (obj instanceof Interceptor && this instanceof WriteType) {
                ((Interceptor) obj).prePersist();
            }
        }

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

        String[] ignoreNullAndCombined(Object criteria, Function<Field, String> func) {
            return ignoreNullAndCombined(criteria, func, false);
        }

        /**
         * 获取并缓存表信息结构
         */
        EntityTable tableInfo(ProviderContext context) {
            return tableCache.computeIfAbsent(context.getMapperType(), t -> EntityTableMapper.extractTableInfo(entityType(context)));
        }

        /**
         * 获取BaseMapper接口中的泛型类型
         *
         * @param context ProviderContext
         * @return clz
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

        String bindParameter(Field field) {
            return String.format("#{%s}", field.getName());
        }

        String columnName(Field field) {
            return EntityTableMapper.getColumnName(field);
        }

        Object value(Object bean, Field field) {
            return EntityTableMapper.getFieldValue(bean, field);
        }
    }
}