package io.demo.crm.core;

import io.demo.crm.common.util.LogUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 数据访问层（DAL），提供与数据库交互的方法，使用 MyBatis 进行 SQL 查询的执行。
 * 支持多种 CRUD 操作。
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
     * 初始化 SqlSession，用于执行 DAL 操作。
     *
     * @param sqlSession SqlSession，用于数据库交互。
     */
    private void initSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        this.configuration = sqlSession.getConfiguration();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    /**
     * 单例持有者，确保 Dal 实例的唯一性。
     */
    private static class Holder {
        private static final DataAccessLayer instance = new DataAccessLayer();
    }

    /**
     * 获取 Dal 实例并为指定的实体类准备 Executor。
     *
     * @param clazz 实体类类型。
     * @param <T>   实体类的类型。
     * @return 为指定实体类准备的 Executor。
     */
    public static <T> Executor<T> with(Class<T> clazz) {
        return with(clazz, applicationContext.getBean(SqlSession.class));
    }

    /**
     * 获取 Dal 实例并为指定的实体类准备 Executor，同时使用给定的 SqlSession。
     *
     * @param clazz      实体类类型。
     * @param sqlSession SqlSession，用于数据库交互。
     * @param <T>        实体类的类型。
     * @return 为指定实体类准备的 Executor。
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
     * 执行原生 SQL 查询，并将结果作为对象列表返回。
     *
     * @param sql        要执行的 SQL 查询语句。
     * @param param      查询参数。
     * @param resultType 结果类型。
     * @param <T>        结果类型的泛型。
     * @return 查询结果的对象列表。
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
        public Integer batchInsert(List<E> es) {
            // 使用 BatchInsertSqlProvider 构建 SQL 语句
            String sql = new BaseMapper.BatchInsertSqlProvider().buildSql(es, this.table);

            // 获取 MyBatis 映射的 SQL ID
            String msId = execute(sql, table.getEntityClass(), int.class, SqlCommandType.INSERT);

            // 获取 SqlSessionFactory 并打开批处理会话
            SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);

            // 使用 try-with-resources 确保 SqlSession 关闭
            try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {

                // 执行批量插入操作
                for (E entity : es) {
                    sqlSession.insert(msId, entity);
                }

                // 刷新批处理中的所有语句
                sqlSession.flushStatements();

                // 提交事务，确保批量操作生效
                sqlSession.commit();

                // 返回成功插入的记录数
                return es.size();
            } catch (Exception e) {
                // 记录异常信息
                LogUtils.error("Error occurred during batch insert: ", e);
                return 0;
            }
        }
    }

    /**
     * 执行 SQL，并返回生成的 MappedStatement ID。
     *
     * @param sql            SQL 查询语句。
     * @param parameterType  参数类型。
     * @param resultType     结果类型。
     * @param sqlCommandType SQL 命令类型。
     * @return MappedStatement 的 ID。
     */
    private String execute(String sql, Class<?> parameterType, Class<?> resultType, SqlCommandType sqlCommandType) {
        String msId = sqlCommandType.toString() + "." + parameterType.getName() + "." + sql.hashCode();
        if (configuration.hasStatement(msId, false)) {
            return msId;
        }
        SqlSource sqlSource = configuration
                .getDefaultScriptingLanguageInstance()
                .createSqlSource(configuration, sql, parameterType);
        // 缓存 MappedStatement
        newMappedStatement(msId, sqlSource, resultType, sqlCommandType);
        return msId;
    }

    /**
     * 创建并注册新的 MappedStatement。
     *
     * @param msId           MappedStatement 的 ID。
     * @param sqlSource      SQL 源。
     * @param resultType     结果类型。
     * @param sqlCommandType SQL 命令类型。
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
