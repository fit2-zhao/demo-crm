package io.demo.aspectj.support;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.lang.Nullable;

import io.demo.aspectj.annotation.EnableLogRecord;
import io.demo.aspectj.config.LogRecordProxyAutoConfiguration;

public class LogRecordConfigureSelector extends AdviceModeImportSelector<EnableLogRecord> {

    @Override
    @Nullable
    public String[] selectImports(AdviceMode adviceMode) {
        return switch (adviceMode) {
            case PROXY ->
                    new String[]{AutoProxyRegistrar.class.getName(), LogRecordProxyAutoConfiguration.class.getName()};
            case ASPECTJ -> new String[]{LogRecordProxyAutoConfiguration.class.getName()};
        };
    }
}