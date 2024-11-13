package io.demo.crm.listener;

import io.demo.crm.config.MinioProperties;
import io.demo.crm.common.file.storage.FileCenter;
import io.demo.crm.common.file.storage.FileRepository;
import io.demo.crm.common.file.storage.FileRequest;
import io.demo.crm.common.file.storage.MinioRepository;
import io.demo.crm.common.uid.impl.DefaultUidGenerator;
import io.demo.crm.common.util.LogUtils;
import io.demo.crm.common.util.rsa.RsaKey;
import io.demo.crm.common.util.rsa.RsaUtils;
import io.demo.crm.common.file.storage.DefaultRepositoryDir;
import io.demo.crm.common.file.storage.StorageType;
import io.demo.crm.services.system.service.ExtScheduleService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器，用于在应用启动时初始化必要的配置，如唯一 ID 生成器、MinIO 存储桶、RSA 配置等。
 * <p>
 * 该类实现了 {@link ApplicationRunner} 接口，应用启动时会执行 {@link #run(ApplicationArguments)} 方法。
 * </p>
 *
 * @see ApplicationRunner
 */
@Component
public class AppStartListener implements ApplicationRunner {

    @Resource
    private MinioClient minioClient;

    @Resource
    private DefaultUidGenerator uidGenerator;

    @Resource
    private ExtScheduleService extScheduleService;

    /**
     * 应用启动后执行的初始化方法。
     * <p>
     * 此方法会依次初始化唯一 ID 生成器、MinIO 配置和 RSA 配置。
     * </p>
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        LogUtils.info("===== 开始初始化配置 =====");

        // 初始化唯一ID生成器
        uidGenerator.init();

        // 初始化MinIO配置
        LogUtils.info("初始化MinIO配置");
        initializeMinIO();

        // 初始化RSA配置
        LogUtils.info("初始化RSA配置");
        initializeRsaConfiguration();

        LogUtils.info("初始化定时任务");
        extScheduleService.startEnableSchedules();

        LogUtils.info("===== 完成初始化配置 =====");
    }

    /**
     * 初始化 MinIO 存储配置。
     * <p>
     * 此方法检查 MinIO 存储桶是否存在。如果不存在，则创建一个新的存储桶。
     * </p>
     */
    private void initializeMinIO() {
//        String bucketName = minioProperties.getBucket();
        try {
            MinioRepository minioRepository = (MinioRepository) FileCenter.getRepository(StorageType.MINIO);
            minioRepository.init(minioClient);

         /*   // 检查存储桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                // 如果存储桶不存在，创建存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                LogUtils.info("MinIO存储桶创建成功.");
            } else {
                LogUtils.info("MinIO存储桶已存在.");
            }*/
        } catch (Exception e) {
            LogUtils.error("初始化MinIO存储桶时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 初始化 RSA 配置。
     * <p>
     * 此方法首先尝试加载现有的 RSA 密钥。如果不存在，则生成新的 RSA 密钥并保存到文件系统。
     * </p>
     */
    private void initializeRsaConfiguration() {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName("rsa.key");
        fileRequest.setFolder(DefaultRepositoryDir.getSystemRootDir());
        FileRepository fileRepository = FileCenter.getDefaultRepository();

        try {
            byte[] rsaFile = fileRepository.getFile(fileRequest);
            if (rsaFile != null) {
                // 如果RSA密钥文件存在，反序列化并设置密钥
                RsaKey rsaKey = SerializationUtils.deserialize(rsaFile);
                RsaUtils.setRsaKey(rsaKey);
                return;
            }
        } catch (Exception e) {
            LogUtils.error("获取RSA配置失败", e);
        }

        try {
            // 如果RSA密钥文件不存在，生成新的RSA密钥并保存
            RsaKey rsaKey = RsaUtils.getRsaKey();
            byte[] rsaKeyBytes = SerializationUtils.serialize(rsaKey);
            fileRepository.saveFile(rsaKeyBytes, fileRequest);
            RsaUtils.setRsaKey(rsaKey);
        } catch (Exception e) {
            LogUtils.error("初始化RSA配置失败", e);
        }
    }
}
