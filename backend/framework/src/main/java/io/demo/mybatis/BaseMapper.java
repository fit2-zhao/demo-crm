package io.demo.mybatis;

import io.demo.mybatis.lambda.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.AbstractSQL;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * General Mapper interface, providing basic CRUD methods.
 *
 * @param <E> Entity type
 */
public interface BaseMapper<E> {

    /**
     * Inserts a record.
     *
     * @param entity The entity to insert
     * @return The number of rows inserted
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "invoke")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insert(E entity);

    /**
     * Inserts multiple records.
     *
     * @param entities The list of entities to insert
     * @return The number of rows inserted
     */
    @InsertProvider(type = BatchInsertSqlProvider.class, method = "invoke")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer batchInsert(List<E> entities);

    /**
     * Updates a record by primary key.
     *
     * @param entity The entity to update
     * @return The number of rows updated
     */
    @UpdateProvider(type = UpdateSqlProvider.class, method = "invoke")
    Integer updateById(E entity);

    /**
     * Selectively updates a record (only non-null fields).
     *
     * @param entity The entity to update
     * @return The number of rows updated
     */
    @UpdateProvider(type = UpdateSelectiveSqlProvider.class, method = "invoke")
    Integer update(E entity);

    /**
     * Deletes a record by primary key.
     *
     * @param id The primary key value
     * @return The number of rows deleted
     */
    @DeleteProvider(type = DeleteSqlProvider.class, method = "invoke")
    Integer deleteByPrimaryKey(Serializable id);

    /**
     * Deletes records based on criteria.
     *
     * @param criteria The criteria for deletion
     * @return The number of rows deleted
     */
    @DeleteProvider(type = DeleteByCriteriaSqlProvider.class, method = "invoke")
    Integer delete(E criteria);

    /**
     * Selects a record by primary key.
     *
     * @param id The primary key value
     * @return The selected entity
     */
    @SelectProvider(type = SelectByIdSqlProvider.class, method = "invoke")
    E selectByPrimaryKey(Serializable id);

    /**
     * Selects all records.
     *
     * @param orderBy The order by clause
     * @return The list of selected entities
     */
    @SelectProvider(type = SelectAllSqlProvider.class, method = "invoke")
    List<E> selectAll(String orderBy);

    /**
     * Selects records based on criteria.
     *
     * @param criteria The criteria for selection
     * @return The list of selected entities
     */
    @SelectProvider(type = SelectByCriteriaSqlProvider.class, method = "invoke")
    List<E> select(E criteria);

    /**
     * Selects records using a LambdaQueryWrapper.
     *
     * @param wrapper The LambdaQueryWrapper with query conditions
     * @return The list of selected entities
     */
    @SelectProvider(type = SelectByLambdaSqlProvider.class, method = "invoke")
    List<E> selectListByLambda(@Param("wrapper") LambdaQueryWrapper<E> wrapper);

    /**
     * Selects a single record based on criteria.
     *
     * @param criteria The criteria for selection
     * @return The selected entity
     */
    @SelectProvider(type = SelectByCriteriaSqlProvider.class, method = "invoke")
    E selectOne(E criteria);

    /**
     * Selects records based on column and values.
     *
     * @param column The column name
     * @param ids    The array of values to query
     * @return The list of selected entities
     */
    @SelectProvider(type = SelectInSqlProvider.class, method = "invoke")
    List<E> selectByColumn(@Param("column") String column, @Param("array") Serializable[] ids);

    /**
     * Counts records based on criteria.
     *
     * @param criteria The criteria for counting
     * @return The number of records
     */
    @SelectProvider(type = CountByCriteriaSqlProvider.class, method = "invoke")
    Long countByExample(E criteria);

    /**
     * Custom SQL query.
     *
     * @param sqlBuild The SQL build function
     * @param criteria The criteria for selection
     * @return The list of selected entities
     */
    @SelectProvider(type = SelectBySqlProvider.class, method = "invoke")
    List<E> query(@Param("sqlBuild") Function<SQL, SQL> sqlBuild, @Param("entity") Object criteria);

    /**
     * Selects records by primary key array.
     *
     * @param ids The array of primary key values
     * @return The list of selected entities
     */
    default List<E> selectByIds(@Param("array") Serializable[] ids) {
        return selectByColumn("id", ids);
    }

    /**
     * Checks if a record exists.
     *
     * @param criteria The criteria for checking
     * @return True if the record exists, otherwise false
     */
    default boolean exist(E criteria) {
        Long count = countByExample(criteria);
        return count != null && count > 0;
    }

    /**
     * Inserts or updates a record.
     *
     * @param criteria The criteria for upsert
     * @return The number of rows affected
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

    class SelectByLambdaSqlProvider extends AbstractSqlProviderSupport {
        @Override
        public SQL sql(Object criteria) {
            LambdaQueryWrapper<?> wrapper = (LambdaQueryWrapper<?>) criteria;

            SQL sql = new SQL()
                    .SELECT(table.getSelectColumns())
                    .FROM(table.getTableName());

            // Parse the conditions of LambdaQueryWrapper into WHERE clause
            String whereClause = wrapper.getWhereClause();
            if (StringUtils.isNotBlank(whereClause)) {
                sql.WHERE(whereClause);
            }

            // Parse the order by clause
            String orderByClause = wrapper.getOrderByClause();
            if (StringUtils.isNotBlank(orderByClause)) {
                sql.ORDER_BY(orderByClause);
            }

            return sql;
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
         * Similar to @PrePersist
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
}