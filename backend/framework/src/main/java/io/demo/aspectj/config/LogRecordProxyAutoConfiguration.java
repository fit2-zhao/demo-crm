package io.demo.aspectj.config;

import io.demo.aspectj.annotation.EnableLogRecord;
import io.demo.aspectj.support.aop.LogRecordAopAdvisor;
import io.demo.aspectj.support.aop.LogRecordInterceptor;
import io.demo.aspectj.support.aop.LogRecordOperationSource;
import io.demo.common.util.LogUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
public class LogRecordProxyAutoConfiguration implements ImportAware {
    private AnnotationAttributes enableLogRecord;

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationSource logRecordOperationSource() {
        return new LogRecordOperationSource();
    }

    @DependsOn("logRecordInterceptor")
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordAopAdvisor logRecordAdvisor(LogRecordInterceptor logRecordInterceptor) {
        LogRecordAopAdvisor advisor =
                new LogRecordAopAdvisor();
        advisor.setLogRecordOperationSource(logRecordOperationSource());
        advisor.setAdvice(logRecordInterceptor);
        advisor.setOrder(enableLogRecord.getNumber("order"));
        return advisor;
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordInterceptor logRecordInterceptor() {
        LogRecordInterceptor interceptor = new LogRecordInterceptor();
        interceptor.setLogRecordOperationSource(logRecordOperationSource());
        interceptor.setJoinTransaction(enableLogRecord.getBoolean("joinTransaction"));
        return interceptor;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLogRecord = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (this.enableLogRecord == null) {
            LogUtils.info("EnableLogRecord is not present on importing class");
        }
    }
}
