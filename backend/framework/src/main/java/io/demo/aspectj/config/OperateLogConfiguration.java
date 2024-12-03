package io.demo.aspectj.config;

import io.demo.aspectj.annotation.EnableLogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;

/**
 * 操作日志配置类
 */
@EnableLogRecord(tenant = "io.demo",mode = AdviceMode.ASPECTJ)
@Configuration
@Slf4j
public class OperateLogConfiguration {
}
