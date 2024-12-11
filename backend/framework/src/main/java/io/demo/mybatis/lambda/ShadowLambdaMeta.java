package io.demo.mybatis.lambda;

import jodd.util.StringPool;

/**
 * Meta-information class created based on {@link SerializedLambda}.
 * <p>
 * This class provides functionality to get the name of the Lambda implementation method and the instantiated class.
 * It extends the {@link LambdaMeta} class, providing access to the metadata of Lambda expressions.
 * </p>
 */
public class ShadowLambdaMeta extends LambdaMeta {
    private final SerializedLambda lambda;

    /**
     * Constructor that accepts a {@link SerializedLambda} object.
     *
     * @param lambda {@link SerializedLambda} object containing serialized information of the Lambda expression.
     */
    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    /**
     * Gets the name of the Lambda implementation method and converts it to snake\_case format.
     *
     * @return The converted method name.
     */
    @Override
    public String getImplMethodName() {
        return toSnakeCase(lambda.getImplMethodName());
    }

    /**
     * Gets the {@link Class} object of the instantiated class.
     * Converts the string of the instantiated type from {@link SerializedLambda} to a {@link Class} object.
     *
     * @return The {@link Class} object of the instantiated class.
     */
    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        // Extract and convert the type name
        String instantiatedType = instantiatedMethodType
                .substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON))
                .replace(StringPool.SLASH, StringPool.DOT);
        // Load the instantiated class using the class loader
        return ClassUtils.toClassConfident(instantiatedType, lambda.getClass().getClassLoader());
    }
}