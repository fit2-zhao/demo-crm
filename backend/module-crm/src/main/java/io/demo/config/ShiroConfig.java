package io.demo.config;

import io.demo.security.ShiroFilter;
import io.demo.common.security.ApiKeyFilter;
import io.demo.common.security.CsrfFilter;
import io.demo.common.security.realm.LocalRealm;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.aop.*;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.aop.SpringAnnotationResolver;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shiro configuration class for configuring Shiro's security manager, session manager, filters, etc.
 * <p>
 * This class is responsible for configuring Shiro-related Beans, including session management, cache management, security filter chain, etc.
 * It also defines the logic for authorization and authentication, as well as annotation support.
 * </p>
 *
 * @version 1.0
 */
@Configuration
public class ShiroConfig {

    /**
     * Configures the Shiro filter factory.
     * <p>
     * Sets the login page, unauthorized page, filter chain, etc. Also configures API Key and CSRF protection filters.
     * </p>
     *
     * @param sessionManager Default web security manager
     * @return Configured {@link ShiroFilterFactoryBean} instance
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager sessionManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setLoginUrl("/");
        shiroFilterFactoryBean.setSecurityManager(sessionManager);
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setSuccessUrl("/");

        // Add custom filters
        shiroFilterFactoryBean.getFilters().put("apikey", new ApiKeyFilter());
        shiroFilterFactoryBean.getFilters().put("csrf", new CsrfFilter());

        // Configure filter chain
        Map<String, String> filterChainDefinitionMap = shiroFilterFactoryBean.getFilterChainDefinitionMap();
        filterChainDefinitionMap.putAll(ShiroFilter.loadBaseFilterChain());
        filterChainDefinitionMap.putAll(ShiroFilter.ignoreCsrfFilter());
        filterChainDefinitionMap.put("/**", "apikey, csrf, authc");

        return shiroFilterFactoryBean;
    }

    /**
     * Configures Shiro's cache manager using in-memory cache management.
     *
     * @return Configured {@link MemoryConstrainedCacheManager} instance
     */
    @Bean
    public MemoryConstrainedCacheManager memoryConstrainedCacheManager() {
        return new MemoryConstrainedCacheManager();
    }

    /**
     * Configures Shiro's session manager.
     * <p>
     * Uses {@link ServletContainerSessionManager} to manage web sessions.
     * </p>
     *
     * @return Configured {@link SessionManager} instance
     */
    @Bean
    public SessionManager sessionManager() {
        return new ServletContainerSessionManager();
    }

    /**
     * Configures Shiro's security manager.
     * <p>
     * Sets the session manager, cache manager, and custom Realm in the security manager.
     * </p>
     *
     * @param sessionManager Session manager
     * @param cacheManager   Cache manager
     * @param localRealm     Custom Realm instance
     * @return Configured {@link DefaultWebSecurityManager} instance
     */
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(SessionManager sessionManager, CacheManager cacheManager, Realm localRealm) {
        DefaultWebSecurityManager defaultManager = new DefaultWebSecurityManager();
        defaultManager.setSessionManager(sessionManager);
        defaultManager.setCacheManager(cacheManager);
        defaultManager.setRealm(localRealm);
        return defaultManager;
    }

    /**
     * Configures Shiro's custom Realm for authentication and authorization logic.
     * <p>
     * The custom {@link LocalRealm} implements Shiro's Realm interface to handle user authentication and authorization.
     * </p>
     *
     * @return Configured {@link LocalRealm} instance
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public LocalRealm localRealm() {
        return new LocalRealm();
    }

    /**
     * Configures Shiro's lifecycle Bean post-processor to manage Shiro's lifecycle.
     *
     * @return Configured {@link LifecycleBeanPostProcessor} instance
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * Configures Shiro's default advisor auto proxy creator to support method-level annotation authorization.
     *
     * @return Configured {@link DefaultAdvisorAutoProxyCreator} instance
     */
    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
        daap.setProxyTargetClass(true);
        return daap;
    }

    /**
     * Configures Shiro's authorization annotation support.
     * <p>
     * Uses {@link AuthorizationAttributeSourceAdvisor} and {@link AopAllianceAnnotationsAuthorizingMethodInterceptor}
     * to configure Shiro's role and authentication annotations.
     * </p>
     *
     * @param sessionManager Default web security manager
     * @return Configured {@link AuthorizationAttributeSourceAdvisor} instance
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager sessionManager) {
        AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
        aasa.setSecurityManager(sessionManager);

        // Configure annotation interceptors
        AopAllianceAnnotationsAuthorizingMethodInterceptor advice = new AopAllianceAnnotationsAuthorizingMethodInterceptor();
        List<AuthorizingAnnotationMethodInterceptor> interceptors = new ArrayList<>(5);

        AnnotationResolver resolver = new SpringAnnotationResolver();
        interceptors.add(new RoleAnnotationMethodInterceptor(resolver));
        interceptors.add(new AuthenticatedAnnotationMethodInterceptor(resolver));
        interceptors.add(new UserAnnotationMethodInterceptor(resolver));
        interceptors.add(new GuestAnnotationMethodInterceptor(resolver));

        advice.setMethodInterceptors(interceptors);
        aasa.setAdvice(advice);

        return aasa;
    }
}