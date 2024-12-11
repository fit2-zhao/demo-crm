package io.demo.mybatis.lambda;

import io.demo.common.util.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Lambda parsing utility class.
 * <p>
 * This utility class provides functionality to extract method names from Lambda expressions, supporting various methods including proxy, reflection, and serialization.
 * </p>
 */
public final class LambdaUtils {

    /**
     * Extracts the implementation method name of a Lambda expression.
     * <p>
     * Depending on the environment, this method will attempt to parse the Lambda expression using proxy, reflection, or serialization.
     * </p>
     *
     * @param func The Lambda object to be parsed.
     * @return The implementation method name of the Lambda expression.
     */
    public static String extract(XFunction<?, ?> func) {
        // 1. In IDEA debug mode, the Lambda expression is a proxy object
        if (func instanceof Proxy) {
            ProxyLambdaMeta lambdaMeta = new ProxyLambdaMeta((Proxy) func);
            return lambdaMeta.getImplMethodName();
        }

        // 2. Read the metadata in the Lambda expression through reflection
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            ReflectLambdaMeta lambdaMeta = new ReflectLambdaMeta((java.lang.invoke.SerializedLambda) method.invoke(func), func.getClass().getClassLoader());
            return lambdaMeta.getImplMethodName();
        } catch (Throwable e) {
            // 3. When reflection fails, use serialization to read Lambda metadata
            LogUtils.error("Extract lambda meta error", e);
            return new ShadowLambdaMeta(io.demo.mybatis.lambda.SerializedLambda.extract(func)).getImplMethodName();
        }
    }
}