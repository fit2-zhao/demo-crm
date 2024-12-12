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
 * 通用的Spring Bean工厂，用于从Spring容器中获取Bean并调用方法。
 * 实现了ApplicationContextAware接口，以便在需要时获取Spring的应用上下文。
 */
@Component
public class CommonBeanFactory implements ApplicationContextAware {

    // 保存ApplicationContext实例
    private static ApplicationContext context;

    /**
     * 设置ApplicationContext，Spring容器会自动注入上下文。
     *
     * @param ctx 当前的Spring应用上下文
     * @throws BeansException 如果出现错误
     */
    public void setApplicationContext(@NotNull ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    /**
     * 根据Bean名称获取Bean实例
     *
     * @param beanName Bean的名称
     * @return 返回Bean实例，若未找到则返回null
     */
    public static Object getBean(String beanName) {
        try {
            // 如果上下文或Bean名称为空，则返回null
            if (context != null && StringUtils.isNotBlank(beanName)) {
                return context.getBean(beanName);
            }
        } catch (BeansException e) {
            // 捕获Spring的异常并返回null
            return null;
        }
        return null;
    }

    /**
     * 根据Bean类型获取Bean实例
     *
     * @param className Bean的类型
     * @param <T>       返回的Bean类型
     * @return 返回Bean实例，若未找到则返回null
     */
    public static <T> T getBean(Class<T> className) {
        try {
            // 如果上下文或类型为空，则返回null
            if (context != null && className != null) {
                return context.getBean(className);
            }
        } catch (BeansException e) {
            // 捕获Spring的异常并返回null
            return null;
        }
        return null;
    }

    /**
     * 获取指定类型的所有Bean实例
     *
     * @param className Bean的类型
     * @param <T>       返回的Bean类型
     * @return 返回所有类型为className的Bean实例的Map
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> className) {
        return context.getBeansOfType(className);
    }

    /**
     * 调用指定Bean的方法
     *
     * @param beanName       Bean的名称
     * @param methodFunction 方法选择器函数，接受Bean的类类型并返回一个Method对象
     * @param args           方法调用的参数
     * @return 返回方法的执行结果，若发生异常则返回null
     */
    public static Object invoke(String beanName, Function<Class<?>, Method> methodFunction, Object... args) {
        try {
            Object bean = getBean(beanName);
            // 检查bean是否存在
            if (ObjectUtils.isNotEmpty(bean)) {
                Class<?> clazz = bean.getClass();
                // 使用提供的methodFunction来获取方法并执行
                Method method = methodFunction.apply(clazz);
                if (method != null) {
                    return method.invoke(bean, args);
                }
            }
        } catch (Exception e) {
            // 记录错误日志
            LogUtils.error(e);
        }
        return null;
    }
}
