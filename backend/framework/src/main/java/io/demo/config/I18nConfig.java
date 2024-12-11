package io.demo.config;

import io.demo.common.util.Translator;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration class for setting up internationalization and validation related beans.
 * <p>
 * This configuration class includes:
 * 1. Bean configuration for the internationalization translator (Translator).
 * 2. Validator using JSR-303 specification, with an internationalized message source.
 * </p>
 */
@Configuration
public class I18nConfig {

    /**
     * Creates a Translator bean to provide internationalization translation functionality.
     * <p>
     * This bean is created only if no other Translator bean is present.
     * </p>
     *
     * @return Translator object
     */
    @Bean
    @ConditionalOnMissingBean
    public Translator translator() {
        return new Translator();
    }

    /**
     * Configures the internationalized message source for JSR-303 validation.
     * <p>
     * Uses Hibernate Validator as the validation provider and sets the specified MessageSource as the message source.
     * </p>
     *
     * @param messageSource The message source for providing internationalized error messages
     * @return Configured LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setProviderClass(HibernateValidator.class);
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        return localValidatorFactoryBean;
    }

    /**
     * Creates a Validator bean for performing JSR-303 validation.
     * <p>
     * This bean uses LocalValidatorFactoryBean as the validation factory to provide a validator instance.
     * </p>
     *
     * @param localValidatorFactoryBean The validation factory
     * @return Validator instance
     */
    @Bean
    public Validator validator(LocalValidatorFactoryBean localValidatorFactoryBean) {
        return localValidatorFactoryBean.getValidator();
    }
}