package io.demo.common.uid;

import io.demo.common.util.CommonBeanFactory;
import io.demo.common.uid.impl.DefaultUidGenerator;

/**
 * IDGenerator is used to generate unique IDs.
 * Provides functionality to generate numeric and string IDs.
 */
public class IDGenerator {

    // Default UID generator instance
    private static final DefaultUidGenerator DEFAULT_UID_GENERATOR;

    static {
        // Get DefaultUidGenerator instance from CommonBeanFactory
        DEFAULT_UID_GENERATOR = CommonBeanFactory.getBean(DefaultUidGenerator.class);
    }

    /**
     * Generates a unique numeric ID.
     *
     * @return Unique numeric ID
     */
    public static Long nextNum() {
        return DEFAULT_UID_GENERATOR.getUID();
    }

    /**
     * Generates a unique string ID.
     *
     * @return Unique string ID
     */
    public static String nextStr() {
        return String.valueOf(DEFAULT_UID_GENERATOR.getUID());
    }
}