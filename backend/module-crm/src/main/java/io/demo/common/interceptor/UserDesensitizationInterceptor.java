package io.demo.common.interceptor;

import io.demo.modules.system.domain.User;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Interceptor for desensitizing user password fields.
 * This interceptor removes the password field from User objects in query results to prevent sensitive information leakage.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class UserDesensitizationInterceptor implements Interceptor {

    /**
     * Intercepts the query method to desensitize the password field in User objects.
     *
     * @param invocation The method invocation object
     * @return The desensitized result
     * @throws Throwable If an error occurs during method execution
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // Execute the original method
        Object returnValue = invocation.proceed();

        // If the return value is of type List, process each element
        if (returnValue instanceof List<?>) {
            List<Object> list = new ArrayList<>();
            boolean isDecrypted = false;

            for (Object val : (List<?>) returnValue) {
                if (val instanceof User) {
                    isDecrypted = true;
                    // Set the password field to null for desensitization
                    ((User) val).setPassword(null);
                }
                list.add(val);
            }

            // Return the modified list if any desensitization was performed, otherwise return the original result
            return isDecrypted ? list : returnValue;
        }

        // If the return value is a single User object, desensitize its password field
        if (returnValue instanceof User) {
            ((User) returnValue).setPassword(null);
        }

        return returnValue;
    }

    /**
     * Wraps the target object with the interceptor.
     *
     * @param target The target object to be wrapped
     * @return The wrapped target object
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * Sets the properties for the interceptor.
     *
     * @param properties The properties for the interceptor
     */
    @Override
    public void setProperties(Properties properties) {
        // This interceptor does not require any properties to be set
    }
}