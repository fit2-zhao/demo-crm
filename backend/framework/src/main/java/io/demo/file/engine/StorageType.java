package io.demo.file.engine;

/**
 * Storage type enumeration class, used to identify different file storage methods.
 * <p>
 * This enumeration class defines three storage methods: MINIO, GIT, and LOCAL.
 * </p>
 */
public enum StorageType {

    /**
     * MINIO storage, typically used for object storage.
     */
    MINIO,

    /**
     * LOCAL storage, typically used for local file storage.
     */
    LOCAL;

    /**
     * Gets the corresponding enum value based on the string.
     * <p>
     * If the provided string does not match any enum value, returns {@code null}.
     * </p>
     *
     * @param storageType The storage type represented as a string
     * @return The corresponding enum value, or {@code null} if no matching enum is found
     */
    public static StorageType fromString(String storageType) {
        if (storageType != null) {
            for (StorageType type : StorageType.values()) {
                if (type.name().equalsIgnoreCase(storageType)) {
                    return type;
                }
            }
        }
        return LOCAL;
    }

}