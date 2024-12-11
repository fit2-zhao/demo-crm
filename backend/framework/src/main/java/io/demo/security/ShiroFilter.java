package io.demo.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing filter chains in the application.
 * Contains methods for loading the base filter chain and ignoring the CSRF filter chain.
 */
public class ShiroFilter {

    /**
     * Loads the base filter chain for the application.
     * This filter chain is a map that associates URL patterns with filter rules.
     *
     * @return A map containing the filter chain definitions, where the key is the URL pattern and the value is the associated filter rule.
     */
    public static Map<String, String> loadBaseFilterChain() {
        Map<String, String> filterChainDefinitionMap = new HashMap<>();

        // Publicly accessible URLs
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/signout", "anon");
        filterChainDefinitionMap.put("/is-login", "anon");
        filterChainDefinitionMap.put("/get-key", "anon");

        // Static resource paths
        filterChainDefinitionMap.put("/*.html", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/assets/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/display/info", "anon");
        filterChainDefinitionMap.put("/file/preview/**", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/base-display/**", "anon");

        // Swagger API documentation related paths
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/api-docs/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");

        // 403 error page path
        filterChainDefinitionMap.put("/403", "anon");
        filterChainDefinitionMap.put("/demo/**", "anon");

        // Anonymous paths
        filterChainDefinitionMap.put("/anonymous/**", "anon");

        return filterChainDefinitionMap;
    }

    /**
     * Returns the filter chain definitions that ignore CSRF protection.
     *
     * @return A map containing the filter chain definitions for URL paths that should bypass CSRF checks.
     */
    public static Map<String, String> ignoreCsrfFilter() {
        Map<String, String> filterChainDefinitionMap = new HashMap<>();

        // Paths that skip CSRF verification
        filterChainDefinitionMap.put("/", "apikey, authc"); // Root path skips CSRF check
        filterChainDefinitionMap.put("/language", "apikey, authc"); // /language path skips CSRF check
        filterChainDefinitionMap.put("/mock", "apikey, authc"); // /mock path skips CSRF check

        return filterChainDefinitionMap;
    }
}