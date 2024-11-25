package io.demo.listener;

import io.demo.common.file.storage.*;
import io.demo.common.uid.impl.DefaultUidGenerator;
import io.demo.common.util.CommonBeanFactory;
import io.demo.common.util.HikariCPUtils;
import io.demo.common.util.LogUtils;
import io.demo.common.util.rsa.RsaKey;
import io.demo.common.util.rsa.RsaUtils;
import io.demo.modules.system.service.ExtScheduleService;
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

        HikariCPUtils.printHikariCPStatus();

        LogUtils.info("===== 完成初始化配置 =====");
    }

    /**
     * 初始化 MinIO 存储配置。
     * <p>
     * 此方法检查 MinIO 存储桶是否存在。如果不存在，则创建一个新的存储桶。
     * </p>
     */
    private void initializeMinIO() {
        try {
            FileRepository fileRepository = FileCenter.getDefaultRepository();
            if (fileRepository instanceof MinioRepository minioRepository) {
                MinioClient minioClient = CommonBeanFactory.getBean(MinioClient.class);
                minioRepository.init(minioClient);
            }
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
