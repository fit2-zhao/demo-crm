package io.demo.common.util;

public class ServiceUtils {
    /**
     * Saves the resource name, used to concatenate the resource name when handling NOT_FOUND exceptions.
     */
    private static final ThreadLocal<String> resourceName = new ThreadLocal<>();

    public static String getResourceName() {
        return resourceName.get();
    }

    public static void clearResourceName() {
        resourceName.remove();
    }
}