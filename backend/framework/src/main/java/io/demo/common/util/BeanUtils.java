package io.demo.common.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * BeanUtils provides utility methods for operating on Java Beans, including property copying, reflection-based property access, and setting property values.
 */
public class BeanUtils {

    /**
     * Copies properties from the source object to the target object.
     *
     * @param target Target object
     * @param source Source object
     * @param <T>    Target object type
     * @return Target object
     * @throws RuntimeException If the copying process fails, a runtime exception is thrown
     */
    public static <T> T copyBean(T target, Object source) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object: ", e);
        }
    }

    /**
     * Copies properties from the source object to the target object, with specified properties to ignore.
     *
     * @param target           Target object
     * @param source           Source object
     * @param ignoreProperties Properties to ignore
     * @param <T>              Target object type
     * @return Target object
     * @throws RuntimeException If the copying process fails, a runtime exception is thrown
     */
    public static <T> T copyBean(T target, Object source, String... ignoreProperties) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object: ", e);
        }
    }

    /**
     * Gets the value of a Java Bean property by field name.
     *
     * @param fieldName Field name
     * @param bean      Java Bean object
     * @return Value of the field, or null if retrieval fails
     */
    public static Object getFieldValueByName(String fieldName, Object bean) {
        try {
            if (StringUtils.isBlank(fieldName)) {
                return null;
            }
            String getter = "get" + StringUtils.capitalize(fieldName);
            Method method = bean.getClass().getMethod(getter);
            return method.invoke(bean);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the value of a Java Bean property by field name and type.
     *
     * @param bean      Java Bean object
     * @param fieldName Field name
     * @param value     Value to set
     * @param type      Field type
     */
    public static void setFieldValueByName(Object bean, String fieldName, Object value, Class<?> type) {
        try {
            if (StringUtils.isBlank(fieldName)) {
                return;
            }
            String setter = "set" + StringUtils.capitalize(fieldName);
            Method method = bean.getClass().getMethod(setter, type);
            method.invoke(bean, value);
        } catch (Exception ignore) {
            // Log or handle as needed
        }
    }

    /**
     * Gets the setter method of a Java Bean property by field name and type.
     *
     * @param bean      Java Bean object
     * @param fieldName Field name
     * @param type      Field type
     * @return Setter method, or null if retrieval fails
     */
    public static Method getMethod(Object bean, String fieldName, Class<?> type) {
        try {
            if (StringUtils.isBlank(fieldName)) {
                return null;
            }
            String setter = "set" + StringUtils.capitalize(fieldName);
            return bean.getClass().getMethod(setter, type);
        } catch (Exception e) {
            return null;
        }
    }
}