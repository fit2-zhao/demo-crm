package io.demo.crm.common.file.storage;

/**
 * 存储类型枚举类，用于标识不同的文件存储方式。
 * <p>
 * 此枚举类定义了三种存储方式：MINIO、GIT 和 LOCAL。
 * </p>
 */
public enum StorageType {

    /**
     * MINIO 存储，通常用于对象存储。
     */
    MINIO,

    /**
     * GIT 存储，通常用于通过版本控制系统存储文件。
     */
    GIT,

    /**
     * LOCAL 存储，通常用于本地文件存储。
     */
    LOCAL
}
