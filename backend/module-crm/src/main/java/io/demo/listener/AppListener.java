package io.demo.listener;

import io.demo.common.uid.impl.DefaultUidGenerator;
import io.demo.common.util.CommonBeanFactory;
import io.demo.common.util.HikariCPUtils;
import io.demo.common.util.LogUtils;
import io.demo.common.util.rsa.RsaKey;
import io.demo.common.util.rsa.RsaUtils;
import io.demo.file.engine.*;
import io.demo.modules.system.service.ExtScheduleService;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class AppListener implements ApplicationRunner {
    @Resource
    private DefaultUidGenerator uidGenerator;

    @Resource
    private ExtScheduleService extScheduleService;

    /**
     * Initialization method executed after the application starts.
     * <p>
     * This method initializes the unique ID generator, MinIO configuration, and RSA configuration in sequence.
     * </p>
     *
     * @param args Startup arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        LogUtils.info("===== Starting configuration initialization =====");

        // Initialize unique ID generator
        uidGenerator.init();

        // Initialize MinIO configuration
        LogUtils.info("Initializing MinIO configuration");
        initializeMinIO();

        // Initialize RSA configuration
        LogUtils.info("Initializing RSA configuration");
        initializeRsaConfiguration();

        LogUtils.info("Initializing scheduled tasks");
        extScheduleService.startEnableSchedules();

        HikariCPUtils.printHikariCPStatus();

        LogUtils.info("===== Configuration initialization completed =====");
    }

    /**
     * Initialize MinIO storage configuration.
     * <p>
     * This method checks if the MinIO bucket exists. If it does not exist, a new bucket is created.
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
            LogUtils.error("Error occurred while initializing MinIO bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize RSA configuration.
     * <p>
     * This method first attempts to load the existing RSA key. If it does not exist, a new RSA key is generated and saved to the file system.
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
                // If the RSA key file exists, deserialize and set the key
                RsaKey rsaKey = SerializationUtils.deserialize(rsaFile);
                RsaUtils.setRsaKey(rsaKey);
                return;
            }
        } catch (Exception e) {
            LogUtils.error("Failed to get RSA configuration", e);
        }

        try {
            // If the RSA key file does not exist, generate a new RSA key and save it
            RsaKey rsaKey = RsaUtils.getRsaKey();
            byte[] rsaKeyBytes = SerializationUtils.serialize(rsaKey);
            fileRepository.saveFile(rsaKeyBytes, fileRequest);
            RsaUtils.setRsaKey(rsaKey);
        } catch (Exception e) {
            LogUtils.error("Failed to initialize RSA configuration", e);
        }
    }
}