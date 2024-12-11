package io.demo.common.interceptor;

import io.demo.config.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * System Interceptor Configuration Class
 * <p>
 * This class is used to configure MyBatis interceptors, especially for field compression and other functionalities.
 * </p>
 */
@Configuration
public class SystemInterceptor {

    /**
     * Configures the list of system interceptors.
     *
     * @return Returns the list of MyBatis interceptor configurations, currently supporting field compression and other functionalities.
     */
    @Bean
    public List<MybatisInterceptorConfig> systemCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        // TODO: Implement blob field compression functionality
        // Add custom interceptor configurations, such as compression and decompression functionalities
        // configList.add(new MybatisInterceptorConfig(TestResourcePoolBlob.class, "configuration", CompressUtils.class, "zip", "unzip"));
        return configList;
    }
}