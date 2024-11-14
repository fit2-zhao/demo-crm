package io.demo.crm.common.file.storage;

import io.demo.crm.common.util.CommonBeanFactory;
import io.demo.crm.common.util.LogUtils;
import io.demo.crm.config.MinioProperties;

import static io.demo.crm.common.file.storage.StorageType.*;

/**
 * FileCenter 类提供了根据存储类型获取对应文件仓库的静态方法。
 * <p>
 * 该类封装了不同存储类型（如 MINIO、LOCAL）的文件仓库获取逻辑，并允许根据存储类型返回相应的 {@link FileRepository} 实现。
 * </p>
 */
public class FileCenter {
    // 默认存储类型
    private static StorageType defStorageType = null;

    // 私有构造函数，防止实例化
    private FileCenter() {
    }

    /**
     * 根据给定的存储类型返回对应的 {@link FileRepository} 实现。
     *
     * @param storageType 存储类型枚举值，指示所需的存储实现（如 MINIO、LOCAL）。
     * @return 返回对应的 {@link FileRepository} 实现，如果存储类型未知，则返回默认的仓库。
     */
    public static FileRepository getRepository(StorageType storageType) {
        return switch (storageType) {
            case MINIO -> CommonBeanFactory.getBean(MinioRepository.class);
            case LOCAL -> CommonBeanFactory.getBean(LocalRepository.class);
            default -> getDefaultRepository();
        };
    }

    /**
     * 返回默认的 {@link FileRepository} 实现。
     * <p>
     * 当前默认实现为 {@link MinioRepository}，可以根据实际需求修改此方法以支持其他默认仓库。
     * </p>
     *
     * @return 默认的 {@link FileRepository} 实现。
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
