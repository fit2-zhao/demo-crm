package io.demo.config;

import io.demo.mybatis.BaseMapper;
import io.demo.mybatis.DataAccessLayer;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;

import java.util.Objects;

/**
 * Data access configuration class.
 * Provides dynamic injection support for MyBatis {@link BaseMapper}.
 */
@Configuration
public class DataAccessConfig {

    /**
     * Injected MyBatis {@link SqlSession} for database operations.
     */
    @Resource
    private SqlSession sqlSession;

    /**
     * Provides dynamic instances of generic {@link BaseMapper}.
     * Uses Spring's prototype scope to dynamically resolve the generic type and instantiate each injection.
     *
     * @param injectionPoint Information about the current injection point, used to resolve the target generic type
     * @param <E>            Generic parameter representing the entity class type
     * @return {@link BaseMapper} instance for the corresponding entity class
     * @throws IllegalArgumentException If the field type of the injection point cannot be resolved
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <E> BaseMapper<E> simpleBaseMapper(InjectionPoint injectionPoint) {
        // Resolve the generic type of the injection point field
        ResolvableType resolved = ResolvableType.forField(
                Objects.requireNonNull(injectionPoint.getField(), "Field information of InjectionPoint cannot be null")
        );

        // Get and validate the generic parameter type
        @SuppressWarnings("unchecked")
        Class<E> parameterClass = (Class<E>) Objects.requireNonNull(
                resolved.getGeneric(0).resolve(),
                "Cannot resolve generic parameter type, please ensure a clear generic declaration is used"
        );

        // Return the corresponding BaseMapper instance
        return DataAccessLayer.with(parameterClass, sqlSession);
    }
}