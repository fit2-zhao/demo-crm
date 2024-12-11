package io.demo.mybatis.lambda;

import io.demo.common.exception.GenericException;
import org.apache.ibatis.io.Resources;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for operating on classes, providing methods to check if a class is a proxy class and to load classes using different class loaders.
 */
public final class ClassUtils {

    private static ClassLoader systemClassLoader;

    static {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
            // AccessControlException in Google App Engine
        }
    }

    /**
     * List of proxy class names.
     */
    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList(
            "net.sf.cglib.proxy.Factory", // cglib
            "org.springframework.cglib.proxy.Factory", // cglib
            "javassist.util.proxy.ProxyObject", // javassist
            "org.apache.ibatis.javassist.util.proxy.ProxyObject" // javassist
    );

    // Private constructor to prevent instantiation
    private ClassUtils() {
    }

    /**
     * Determines if the given type is a boolean type (including primitive boolean and Boolean wrapper).
     *
     * @param type The class type to check.
     * @return {@code true} if the type is boolean or its wrapper type, otherwise {@code false}.
     */
    public static boolean isBoolean(Class<?> type) {
        return type == boolean.class || Boolean.class == type;
    }

    /**
     * Determines if the given class is a proxy class.
     *
     * @param clazz The class to check.
     * @return {@code true} if the class is a proxy class, otherwise {@code false}.
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Loads a class using the specified class name and class loader. This method ensures the class is loaded from a valid class loader.
     *
     * @param name        The fully qualified name of the class.
     * @param classLoader The class loader to use.
     * @return The class object corresponding to the class name.
     * @throws GenericException If the class cannot be found or loaded.
     */
    public static Class<?> toClassConfident(String name, ClassLoader classLoader) {
        try {
            return loadClass(name, getClassLoaders(classLoader));
        } catch (ClassNotFoundException e) {
            throw new GenericException("Class not found! Only call this method when you are sure the class exists.", e);
        }
    }

    /**
     * Attempts to load a class using multiple class loaders.
     *
     * @param className    The fully qualified name of the class.
     * @param classLoaders An array of class loaders.
     * @return The class object corresponding to the class name.
     * @throws ClassNotFoundException If the class cannot be found.
     */
    private static Class<?> loadClass(String className, ClassLoader[] classLoaders) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                try {
                    return Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException e) {
                    // Ignore exception and try the next class loader
                }
            }
        }
        throw new ClassNotFoundException("Class not found: " + className);
    }

    /**
     * Gets an array of class loaders to use for loading classes.
     *
     * @param classLoader The primary class loader.
     * @return An array of class loaders.
     */
    private static ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                Resources.getDefaultClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassUtils.class.getClassLoader(),
                systemClassLoader
        };
    }
}