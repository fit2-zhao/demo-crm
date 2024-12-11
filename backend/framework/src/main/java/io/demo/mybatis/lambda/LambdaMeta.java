package io.demo.mybatis.lambda;

/**
 * Lambda information
 */
public abstract class LambdaMeta {

    /**
     * Gets the name of the method implemented by the lambda expression.
     *
     * @return The name of the method implemented by the lambda expression.
     */
    String getImplMethodName() {
        return "";
    }

    /**
     * Instantiates the class of this method.
     *
     * @return The name of the corresponding class.
     */
    Class<?> getInstantiatedClass() {
        return null;
    }

    /**
     * Converts a method name to snake case.
     *
     * @param methodName The method name to convert.
     * @return The method name in snake case.
     */
    String toSnakeCase(String methodName) {
        String fieldName = methodName.replaceAll("get|set", "");
        // Use regex to convert uppercase letters to lowercase and add underscores
        String result = fieldName.replaceAll("([a-z])([A-Z])", "$1_$2");
        return result.toLowerCase();
    }
}