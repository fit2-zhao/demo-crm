package io.demo.file.engine;

import io.demo.common.util.CommonBeanFactory;
import io.demo.common.util.LogUtils;

import static io.demo.file.engine.StorageType.LOCAL;
import static io.demo.file.engine.StorageType.MINIO;

/**
 * The FileCenter class provides static methods to obtain the corresponding file repository based on the storage type.
 * <p>
 * This class encapsulates the logic for obtaining file repositories of different storage types (e.g., MINIO, LOCAL) and allows returning the corresponding {@link FileRepository} implementation based on the storage type.
 * </p>
 */
public class FileCenter {
    // Default storage type
    private static StorageType defStorageType = null;

    // Private constructor to prevent instantiation
    private FileCenter() {
    }

    /**
     * Returns the corresponding {@link FileRepository} implementation based on the given storage type.
     *
     * @param storageType The storage type enum value indicating the required storage implementation (e.g., MINIO, LOCAL).
     * @return The corresponding {@link FileRepository} implementation. If the storage type is unknown, returns the default repository.
     */
    public static FileRepository getRepository(StorageType storageType) {
        return switch (storageType) {
            case MINIO -> CommonBeanFactory.getBean(MinioRepository.class);
            case LOCAL -> CommonBeanFactory.getBean(LocalRepository.class);
            default -> getDefaultRepository();
        };
    }

    /**
     * Returns the default {@link FileRepository} implementation.
     * <p>
     * The current default implementation is {@link MinioRepository}. This method can be modified to support other default repositories as needed.
     * </p>
     *
     * @return The default {@link FileRepository} implementation.
     */
    public static FileRepository getDefaultRepository() {
        if (defStorageType == null) {
            MinioProperties properties = CommonBeanFactory.getBean(MinioProperties.class);
            defStorageType = properties != null && properties.isEnabled() ? MINIO : LOCAL;
        }
        LogUtils.info("Default storage type is set to: " + defStorageType);
        return getRepository(defStorageType);
    }
}