package io.demo.config;

import io.demo.common.schedule.ScheduleManager;
import io.demo.common.schedule.ScheduleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for scheduling tasks.
 * Configures the Quartz scheduler and scheduling management service, enabled when the quartz.enabled property is set to true in the configuration file.
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {

    /**
     * Configures the ScheduleManager Bean.
     * The ScheduleManager will only be loaded into the container when the quartz.enabled property is set to true.
     *
     * @return ScheduleManager instance
     */
    @Bean
    @ConditionalOnProperty(prefix = "quartz", value = "enabled", havingValue = "true")
    public ScheduleManager scheduleManager() {
        return new ScheduleManager();
    }

    /**
     * Configures the ScheduleService Bean.
     * The ScheduleService will only be loaded into the container when the quartz.enabled property is set to true.
     *
     * @return ScheduleService instance
     */
    @Bean
    @ConditionalOnProperty(prefix = "quartz", value = "enabled", havingValue = "true")
    public ScheduleService scheduleService() {
        return new ScheduleService();
    }
}