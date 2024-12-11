package io.demo.common.constants;

/**
 * Enum interface for parameter validation annotation EnumValue.
 * If the enum defines a value similar to `value`, it can implement this interface to be used with the EnumValue annotation.
 * If the enum value only needs to be obtained through `name()`, this interface does not need to be implemented.
 *
 * @author: jianxing
 */
public interface ValueEnum<T> {
    /**
     * Get the enum value.
     *
     * @return Enum value
     */
    T getValue();
}