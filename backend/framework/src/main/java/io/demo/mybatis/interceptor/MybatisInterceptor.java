package io.demo.mybatis.interceptor;

import io.demo.common.util.BeanUtils;
import io.demo.config.MybatisInterceptorConfig;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis interceptor for encrypting or decrypting database operation parameters.
 * <p>
 * This interceptor supports encrypting parameters for update and query operations,
 * and decrypting results based on the configuration.
 * </p>
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class MybatisInterceptor implements Interceptor {

    @Setter
    @Getter
    private List<MybatisInterceptorConfig> interceptorConfigList;

    private final ConcurrentHashMap<String, Class> classMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Map<String, MybatisInterceptorConfig>>> interceptorConfigMap = new ConcurrentHashMap<>();

    /**
     * Intercepts the target method and performs encryption or decryption processing.
     *
     * @param invocation The current invocation information
     * @return The processed result object
     * @throws Throwable If an exception occurs during processing
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        Object parameter = invocation.getArgs()[1];

        // Process parameters for update operations
        if (parameter != null && methodName.equals("update")) {
            invocation.getArgs()[1] = process(parameter);
        }

        // Execute the target method
        Object returnValue = invocation.proceed();
        Object result = returnValue;

        // Decrypt the query result
        if (returnValue instanceof ArrayList<?>) {
            List<Object> list = new ArrayList<>();
            boolean isDecrypted = false;
            for (Object val : (ArrayList<?>) returnValue) {
                Object a = undo(val);
                if (a != val) {
                    isDecrypted = true;
                    list.add(a);
                } else {
                    break;
                }
            }
            if (isDecrypted) {
                result = list;
            }
        } else {
            result = undo(returnValue);
        }
        return result;
    }

    /**
     * Gets the configuration related to the specified object.
     *
     * @param p The target object
     * @return The configuration information of the object
     */
    private Map<String, Map<String, MybatisInterceptorConfig>> getConfig(Object p) {
        Map<String, Map<String, MybatisInterceptorConfig>> result = new HashMap<>();
        if (p == null) {
            return null;
        }

        String pClassName = p.getClass().getName();
        if (interceptorConfigMap.get(pClassName) != null) {
            return interceptorConfigMap.get(pClassName);
        }

        Map<String, List<MybatisInterceptorConfig>> m = new HashMap<>();
        for (MybatisInterceptorConfig interceptorConfig : interceptorConfigList) {
            String className = interceptorConfig.getModelName();
            String attrName = interceptorConfig.getAttrName();
            if (StringUtils.isNotBlank(className)) {
                Class<?> c = classMap.get(className);
                if (c == null) {
                    try {
                        c = Class.forName(className);
                        classMap.put(className, c);
                    } catch (ClassNotFoundException e) {
                        continue;
                    }
                }
                if (c.isInstance(p)) {
                    if (result.get(attrName) == null) {
                        result.put(attrName, new HashMap<>());
                    }
                    if (StringUtils.isNotBlank(interceptorConfig.getInterceptorMethod())) {
                        result.get(attrName).put(Methods.encrypt.name(), interceptorConfig);
                    }
                    if (StringUtils.isNotBlank(interceptorConfig.getInterceptorMethod())) {
                        result.get(attrName).put(Methods.decrypt.name(), interceptorConfig);
                    }
                }
            }
        }
        interceptorConfigMap.put(pClassName, result);
        return result;
    }

    /**
     * Processes encryption.
     *
     * @param obj The target object
     * @return The encrypted object
     * @throws Throwable If an exception occurs during encryption
     */
    private Object process(Object obj) throws Throwable {
        if (obj instanceof Map paramMap) {
            for (Object key : paramMap.keySet()) {
                if (paramMap.get(key) != null) {
                    paramMap.put(key, process(paramMap.get(key)));
                }
            }
            return paramMap;
        }

        Map<String, Map<String, MybatisInterceptorConfig>> localInterceptorConfigMap = getConfig(obj);
        if (MapUtils.isEmpty(localInterceptorConfigMap)) {
            return obj;
        }
        Object newObject = obj.getClass().getDeclaredConstructor().newInstance();
        BeanUtils.copyBean(newObject, obj);
        for (String attrName : localInterceptorConfigMap.keySet()) {
            if (MapUtils.isEmpty(localInterceptorConfigMap.get(attrName))) {
                continue;
            }
            MybatisInterceptorConfig interceptorConfig = localInterceptorConfigMap.get(attrName).get(Methods.encrypt.name());
            if (interceptorConfig == null || StringUtils.isBlank(interceptorConfig.getInterceptorClass())
                    || StringUtils.isBlank(interceptorConfig.getInterceptorMethod())) {
                continue;
            }
            Object fieldValue = BeanUtils.getFieldValueByName(interceptorConfig.getAttrName(), newObject);
            if (fieldValue != null) {
                Class<?> processClazz = Class.forName(interceptorConfig.getInterceptorClass());
                Method method = processClazz.getMethod(interceptorConfig.getInterceptorMethod(), Object.class);
                Object processedValue = method.invoke(null, fieldValue);
                if (processedValue instanceof byte[]) {
                    BeanUtils.setFieldValueByName(newObject, interceptorConfig.getAttrName(), processedValue, byte[].class);
                } else {
                    BeanUtils.setFieldValueByName(newObject, interceptorConfig.getAttrName(), processedValue, fieldValue.getClass());
                }
            }
        }

        return newObject;
    }

    /**
     * Processes decryption.
     *
     * @param obj The target object
     * @return The decrypted object
     * @throws Throwable If an exception occurs during decryption
     */
    private Object undo(Object obj) throws Throwable {
        Map<String, Map<String, MybatisInterceptorConfig>> localDecryptConfigMap = getConfig(obj);
        if (MapUtils.isEmpty(localDecryptConfigMap)) {
            return obj;
        }
        Object result = obj.getClass().getDeclaredConstructor().newInstance();
        BeanUtils.copyBean(result, obj);

        for (String attrName : localDecryptConfigMap.keySet()) {
            if (MapUtils.isEmpty(localDecryptConfigMap.get(attrName))) {
                continue;
            }
            MybatisInterceptorConfig interceptorConfig = localDecryptConfigMap.get(attrName).get(Methods.decrypt.name());
            if (interceptorConfig == null || StringUtils.isBlank(interceptorConfig.getUndoClass())
                    || StringUtils.isBlank(interceptorConfig.getUndoMethod())) {
                continue;
            }
            Object fieldValue = BeanUtils.getFieldValueByName(interceptorConfig.getAttrName(), result);
            if (fieldValue != null) {
                Class<?> processClazz = Class.forName(interceptorConfig.getUndoClass());
                Object undoValue;
                if (fieldValue instanceof List) {
                    Method method = processClazz.getMethod(interceptorConfig.getUndoMethod(), List.class, String.class);
                    method.invoke(null, fieldValue, interceptorConfig.getAttrNameForList());
                } else {
                    Method method = processClazz.getMethod(interceptorConfig.getUndoMethod(), Object.class);
                    undoValue = method.invoke(null, fieldValue);
                    if (undoValue instanceof byte[]) {
                        BeanUtils.setFieldValueByName(result, interceptorConfig.getAttrName(), undoValue, byte[].class);
                    } else {
                        BeanUtils.setFieldValueByName(result, interceptorConfig.getAttrName(), undoValue, fieldValue.getClass());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates a plugin object.
     *
     * @param target The target object
     * @return The wrapped object
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * Sets properties.
     *
     * @param properties Configuration properties
     */
    @Override
    public void setProperties(Properties properties) {
        // TODO: Handle configuration if needed
    }

    private enum Methods {
        encrypt, decrypt
    }
}