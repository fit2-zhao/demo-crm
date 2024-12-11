package io.demo.common.util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

/**
 * General Spring Bean factory for obtaining Beans from the Spring container and invoking methods.
 * Implements the ApplicationContextAware interface to obtain the Spring application context when needed.
 */
@Component
public class CommonBeanFactory implements ApplicationContextAware {

    // Holds the ApplicationContext instance
    private static ApplicationContext context;

    /**
     * Sets the ApplicationContext, the Spring container will automatically inject the context.
     *
     * @param ctx the current Spring application context
     * @throws BeansException if an error occurs
     */
    public void setApplicationContext(@NotNull ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    /**
     * Gets a Bean instance by its name.
     *
     * @param beanName the name of the Bean
     * @return the Bean instance, or null if not found
     */
    public static Object getBean(String beanName) {
        try {
            // Return null if context or beanName is empty
            if (context != null && StringUtils.isNotBlank(beanName)) {
                return context.getBean(beanName);
            }
        } catch (BeansException e) {
            // Catch Spring exception and return null
            return null;
        }
        return null;
    }

    /**
     * Gets a Bean instance by its type.
     *
     * @param className the type of the Bean
     * @param <T>       the type of the Bean
     * @return the Bean instance, or null if not found
     */
    public static <T> T getBean(Class<T> className) {
        try {
            // Return null if context or className is empty
            if (context != null && className != null) {
                return context.getBean(className);
            }
        } catch (BeansException e) {
            // Catch Spring exception and return null
            return null;
        }
        return null;
    }

    /**
     * Gets all Bean instances of the specified type.
     *
     * @param className the type of the Beans
     * @param <T>       the type of the Beans
     * @return a Map of all Bean instances of the specified type
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> className) {
        return context.getBeansOfType(className);
    }

    /**
     * Invokes a method on the specified Bean.
     *
     * @param beanName       the name of the Bean
     * @param methodFunction a function that accepts the Bean's class type and returns a Method object
     * @param args           the arguments for the method invocation
     * @return the result of the method execution, or null if an exception occurs
     */
    public static Object invoke(String beanName, Function<Class<?>, Method> methodFunction, Object... args) {
        try {
            Object bean = getBean(beanName);
            // Check if bean exists
            if (ObjectUtils.isNotEmpty(bean)) {
                Class<?> clazz = bean.getClass();
                // Use the provided methodFunction to get and execute the method
                Method method = methodFunction.apply(clazz);
                if (method != null) {
                    return method.invoke(bean, args);
                }
            }
        } catch (Exception e) {
            // Log the error
            LogUtils.error(e);
        }
        return null;
    }
}