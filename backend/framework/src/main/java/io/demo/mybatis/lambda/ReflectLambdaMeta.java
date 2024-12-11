package io.demo.mybatis.lambda;

import jodd.util.StringPool;

import java.lang.invoke.SerializedLambda;

/**
 * Class providing metadata for Lambda expressions through reflection.
 * This class provides functionality to get the name of the Lambda implementation method and the instantiated class.
 */
public class ReflectLambdaMeta extends LambdaMeta {

    private final SerializedLambda lambda;
    private final ClassLoader classLoader;

    /**
     * Constructor to initialize an instance of ReflectLambdaMeta.
     *
     * @param lambda      The serialized Lambda expression
     * @param classLoader The class loader used to load classes
     */
    public ReflectLambdaMeta(SerializedLambda lambda, ClassLoader classLoader) {
        this.lambda = lambda;
        this.classLoader = classLoader;
    }

    /**
     * Gets the name of the Lambda implementation method and converts it to snake\_case format.
     *
     * @return The name of the Lambda implementation method (in snake\_case format)
     */
    @Override
    public String getImplMethodName() {
        return toSnakeCase(lambda.getImplMethodName());
    }

    /**
     * Gets the class instantiated by the Lambda expression.
     * Loads the corresponding class using the class loader.
     *
     * @return The class instantiated by the Lambda expression
     */
    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON)).replace(StringPool.SLASH, StringPool.DOT);
        return ClassUtils.toClassConfident(instantiatedType, this.classLoader);
    }

}