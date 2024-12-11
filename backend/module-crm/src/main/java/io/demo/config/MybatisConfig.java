package io.demo.config;

import com.fit2cloud.quartz.anno.QuartzDataSource;
import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import io.demo.common.interceptor.UserDesensitizationInterceptor;
import io.demo.mybatis.interceptor.MybatisInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration class for setting up MyBatis and data source related settings.
 * <p>
 * This class is responsible for configuring MyBatis pagination interceptor, user information desensitization interceptor,
 * and database source configuration, including the main data source and Quartz related data source configuration.
 * </p>
 *
 * @version 1.0
 */
@Configuration
@MapperScan(basePackages = {"io.demo.modules.*.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class MybatisConfig {

    /**
     * Configures the MyBatis pagination interceptor.
     * <p>
     * This method creates and returns a {@link PageInterceptor} instance to enable MyBatis pagination functionality.
     * </p>
     *
     * @return Configured pagination interceptor instance
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("pageSizeZero", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    /**
     * Configures the custom MyBatis interceptor.
     * <p>
     * This method creates and returns a {@link MybatisInterceptor} instance, merging multiple interceptor configurations into a list.
     * </p>
     *
     * @param interceptorConfigs List of configured interceptors
     * @return Configured custom interceptor instance
     */
    @Bean
    public MybatisInterceptor dbInterceptor(List<MybatisInterceptorConfig>[] interceptorConfigs) {
        List<MybatisInterceptorConfig> mybatisInterceptorConfigs = new ArrayList<>();
        for (List<MybatisInterceptorConfig> configList : interceptorConfigs) {
            mybatisInterceptorConfigs.addAll(configList);
        }
        // Unified configuration
        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.setInterceptorConfigList(mybatisInterceptorConfigs);
        return interceptor;
    }

    /**
     * Configures the user information desensitization interceptor.
     * <p>
     * This method creates and returns a {@link UserDesensitizationInterceptor} instance for desensitizing user information.
     * </p>
     *
     * @return Configured user desensitization interceptor instance
     */
    @Bean
    public UserDesensitizationInterceptor userDesensitizationInterceptor() {
        return new UserDesensitizationInterceptor();
    }

    /**
     * Configures the main data source.
     * <p>
     * This method creates a main data source based on the properties in the configuration file, using {@link HikariDataSource} as the data source type.
     * </p>
     *
     * @param properties Basic configuration of the data source
     * @return Configured main data source instance
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(@Qualifier("dataSourceProperties") DataSourceProperties properties) {
        return DataSourceBuilder.create(properties.getClassLoader()).type(HikariDataSource.class)
                .driverClassName(properties.determineDriverClassName())
                .url(properties.determineUrl())
                .username(properties.determineUsername())
                .password(properties.determinePassword())
                .build();
    }

    /**
     * Configures the Quartz data source.
     * <p>
     * This method creates a Quartz data source based on the properties in the configuration file, using {@link HikariDataSource} as the data source type.
     * </p>
     *
     * @param properties Basic configuration of the Quartz data source
     * @return Configured Quartz data source instance
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.quartz.hikari")
    @QuartzDataSource
    public DataSource quartzDataSource(@Qualifier("quartzDataSourceProperties") DataSourceProperties properties) {
        return DataSourceBuilder.create(properties.getClassLoader()).type(HikariDataSource.class)
                .driverClassName(properties.determineDriverClassName())
                .url(properties.determineUrl())
                .username(properties.determineUsername())
                .password(properties.determinePassword())
                .build();
    }

    /**
     * Configures the basic properties of the main data source.
     * <p>
     * This method creates and returns a {@link DataSourceProperties} instance for configuring data source properties in Spring Boot.
     * </p>
     *
     * @return Configured data source properties instance
     */
    @Bean("dataSourceProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Configures the basic properties of the Quartz data source.
     * <p>
     * This method creates and returns a {@link DataSourceProperties} instance for configuring Quartz data source properties.
     * </p>
     *
     * @return Configured Quartz data source properties instance
     */
    @Bean("quartzDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.quartz")
    public DataSourceProperties quartzDataSourceProperties() {
        return new DataSourceProperties();
    }
}