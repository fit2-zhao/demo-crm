package io.demo.mybatis.lambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Proxy;

/**
 * This class handles metadata for Lambda expressions executed in IDEA's Evaluate.
 */
public class ProxyLambdaMeta extends LambdaMeta {
    private final Class<?> clazz;
    private final String name;

    public ProxyLambdaMeta(Proxy func) {
        MethodHandle dmh = MethodHandleProxies.wrapperInstanceTarget(func);
        Executable executable = MethodHandles.reflectAs(Executable.class, dmh);
        clazz = executable.getDeclaringClass();
        name = executable.getName();
    }

    @Override
    public String getImplMethodName() {
        return toSnakeCase(name);
    }

    @Override
    public Class<?> getInstantiatedClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return clazz.getSimpleName() + "::" + name;
    }
}