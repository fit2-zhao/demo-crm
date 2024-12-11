package io.demo.mybatis;

import io.demo.common.util.LogUtils;
import io.demo.mybatis.lambda.LambdaQueryWrapper;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Data Access Layer (DAL), providing methods for database interaction using MyBatis for SQL query execution.
 * Supports various CRUD operations.
 */
@Component
public class DataAccessLayer implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private SqlSession sqlSession;
    private Configuration configuration;
    private final Map<Class<?>, EntityTable> cachedTableInfo = new ConcurrentHashMap<>();

    private DataAccessLayer() {
    }

    /**
     * Initializes SqlSession for executing DAL operations.
     *
     * @param sqlSession SqlSession for database interaction.
     */
    private void initSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        this.configuration = sqlSession.getConfiguration();
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    /**
     * Singleton holder to ensure the uniqueness of the Dal instance.
     */
    private static class Holder {
        private static final DataAccessLayer instance = new DataAccessLayer();
    }

    /**
     * Gets the Dal instance and prepares an Executor for the specified entity class.
     *
     * @param clazz Entity class type.
     * @param <T>   Type of the entity class.
     * @return Executor prepared for the specified entity class.
     */
    public static <T> Executor<T> with(Class<T> clazz) {
        return with(clazz, applicationContext.getBean(SqlSession.class));
    }

    /**
     * Gets the Dal instance and prepares an Executor for the specified entity class, using the given SqlSession.
     *
     * @param clazz      Entity class type.
     * @param sqlSession SqlSession for database interaction.
     * @param <T>        Type of the entity class.
     * @return Executor prepared for the specified entity class.
     */
    public static <T> Executor<T> with(Class<T> clazz, SqlSession sqlSession) {
        DataAccessLayer instance = Holder.instance;
        instance.initSession(sqlSession);
        EntityTable entityTable = null;
        if (clazz != null) {
            entityTable = instance.cachedTableInfo.computeIfAbsent(clazz, EntityTableMapper::extractTableInfo);
        }
        return instance.new Executor<>(entityTable);
    }

    /**
     * Executes a native SQL query and returns the results as a list of objects.
     *
     * @param sql        SQL query to execute.
     * @param param      Query parameters.
     * @param resultType Result type.
     * @param <T>        Generic type of the result.
     * @return List of query results.
     */
    public static <T> List<T> sql(String sql, Object param, Class<T> resultType) {
        return with(resultType).sqlQuery(sql, param, resultType);
    }

    public class Executor<E> implements BaseMapper<E> {
        private final EntityTable table;
        private final Class<?> resultType;

        Executor(EntityTable table) {
            this.table = table;
            this.resultType = table != null ? table.getEntityClass() : null;
        }

        @Override
        public List<E> query(Function<SQL, SQL> sqlBuild, Object criteria) {
            Map<String, Object> maps = new HashMap<>(2);
            maps.put("sqlBuild", sqlBuild);
            maps.put("entity", criteria);
            String sql = new BaseMapper.SelectBySqlProvider().buildSql(maps, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectList(msId, criteria);
        }

        @Override
        public List<E> selectAll(String criteria) {
            String sql = new BaseMapper.SelectAllSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectList(msId, criteria);
        }

        @Override
        public List<E> select(E criteria) {
            String sql = new BaseMapper.SelectByCriteriaSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectList(msId, criteria);
        }

        @Override
        public List<E> selectListByLambda(LambdaQueryWrapper<E> wrapper) {
            String sql = new BaseMapper.SelectByLambdaSqlProvider().buildSql(wrapper, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectList(msId, wrapper);
        }

        @Override
        public E selectByPrimaryKey(Serializable criteria) {
            String sql = new BaseMapper.SelectByIdSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectOne(msId, criteria);
        }

        @Override
        public E selectOne(E criteria) {
            String sql = new BaseMapper.SelectByCriteriaSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectOne(msId, criteria);
        }

        @Override
        public List<E> selectByColumn(String column, Serializable[] criteria) {
            Map<String, Object> maps = new HashMap<>(2);
            maps.put("column", column);
            maps.put("array", criteria);
            String sql = new BaseMapper.SelectInSqlProvider().buildSql(maps, this.table);
            String msId = execute(sql, table.getEntityClass(), resultType, SqlCommandType.SELECT);
            return sqlSession.selectList(msId, criteria);
        }

        @Override
        public Long countByExample(E criteria) {
            String sql = new BaseMapper.CountByCriteriaSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), Long.class, SqlCommandType.SELECT);
            return sqlSession.selectOne(msId, criteria);
        }

        @Override
        public Integer insert(E criteria) {
            String sql = new BaseMapper.InsertSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.INSERT);
            return sqlSession.insert(msId, criteria);
        }

        @Override
        public Integer updateById(E criteria) {
            String sql = new BaseMapper.UpdateSelectiveSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.UPDATE);
            return sqlSession.update(msId, criteria);
        }

        @Override
        public Integer update(E criteria) {
            String sql = new BaseMapper.UpdateSelectiveSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.UPDATE);
            return sqlSession.update(msId, criteria);
        }

        @Override
        public Integer delete(E criteria) {
            String sql = new BaseMapper.DeleteSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.DELETE);
            return sqlSession.delete(msId, criteria);
        }

        @Override
        public Integer deleteByPrimaryKey(Serializable criteria) {
            String sql = new BaseMapper.DeleteSqlProvider().buildSql(criteria, this.table);
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.DELETE);
            return sqlSession.delete(msId, criteria);
        }

        public List<E> sqlQuery(String sql, Object param, Class<?> resultType) {
            return sqlQuery(sql, param, param != null ? param.getClass() : Map.class, resultType);
        }

        public List<E> sqlQuery(String sql, Object param, Class<?> paramType, Class<?> resultType) {
            String msId = execute(sql, paramType, resultType, SqlCommandType.SELECT);
            return sqlSession.selectList(msId, param);
        }

        @Override
        public boolean exist(E criteria) {
            List<E> ret = select(criteria);
            return ret != null && !ret.isEmpty();
        }

        @Override
        public Integer batchInsert(List<E> entities) {
            // Use BatchInsertSqlProvider to build SQL statement
            String sql = new BaseMapper.BatchInsertSqlProvider().buildSql(entities, this.table);

            // Get MyBatis mapped SQL ID
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.INSERT);

            // Get SqlSessionFactory and open batch session
            SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);

            // Use try-with-resources to ensure SqlSession is closed
            try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {

                // Execute batch insert operation
                entities.forEach(entity -> sqlSession.insert(msId, entity));

                // Flush all statements in the batch
                sqlSession.flushStatements();

                // Commit transaction to ensure batch operation takes effect
                sqlSession.commit();

                // Return the number of successfully inserted records
                return entities.size();
            } catch (Exception e) {
                // Log exception information
                LogUtils.error("Error occurred during batch insert: ", e);
                return 0;
            }
        }
    }

    /**
     * Executes SQL and returns the generated MappedStatement ID.
     *
     * @param sql            SQL query.
     * @param parameterType  Parameter type.
     * @param resultType     Result type.
     * @param sqlCommandType SQL command type.
     * @return MappedStatement ID.
     */
    private String execute(String sql, Class<?> parameterType, Class<?> resultType, SqlCommandType sqlCommandType) {
        String msId = sqlCommandType.toString() + "." + parameterType.getName() + "." + sql.hashCode();
        if (configuration.hasStatement(msId, false)) {
            return msId;
        }
        SqlSource sqlSource = configuration
                .getDefaultScriptingLanguageInstance()
                .createSqlSource(configuration, sql, parameterType);
        // Cache MappedStatement
        newMappedStatement(msId, sqlSource, resultType, sqlCommandType);
        return msId;
    }

    /**
     * Creates and registers a new MappedStatement.
     *
     * @param msId           MappedStatement ID.
     * @param sqlSource      SQL source.
     * @param resultType     Result type.
     * @param sqlCommandType SQL command type.
     */
    private void newMappedStatement(String msId, SqlSource sqlSource, Class<?> resultType, SqlCommandType sqlCommandType) {
        MappedStatement ms = new MappedStatement.Builder(configuration, msId, sqlSource, sqlCommandType)
                .resultMaps(Collections.singletonList(
                        new ResultMap.Builder(configuration, "defaultResultMap", resultType, new ArrayList<>(0)).build()
                ))
                .build();
        configuration.addMappedStatement(ms);
    }
}