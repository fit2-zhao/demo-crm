package io.demo.common.security;

import io.demo.security.SessionConstants;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Custom filter for handling API key authentication in a web application.
 * Extends AnonymousFilter to support both API key authentication and regular session authentication.
 */
public class ApiKeyFilter extends AnonymousFilter {

    private static final String NO_PASSWORD = "no_pass"; // Default password for API key authentication

    /**
     * Called before processing the request. This method checks if the request uses an API key.
     * If there is no API key and the user is not authenticated, the request is allowed to proceed.
     * If an API key is provided, it attempts to authenticate the user using the key.
     *
     * @param request  Servlet request
     * @param response Servlet response
     * @param mappedValue Mapped values in the filter chain
     * @return true if the request should continue processing, false otherwise
     */
    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);

        // Allow the request to proceed if it is not an API key request and the user is not authenticated
        if (!ApiKeyHandler.isApiKeyCall(httpRequest) && !SecurityUtils.getSubject().isAuthenticated()) {
            return true;
        }

        // Handle API key authentication
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            String userId = ApiKeyHandler.getUser(httpRequest);
            if (StringUtils.isNotBlank(userId)) {
                // Authenticate using the user ID from the API key, with the default password
                SecurityUtils.getSubject().login(new UsernamePasswordToken(userId, NO_PASSWORD));
            }
        }

        // If still not authenticated, set the response header to invalid status
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader(SessionConstants.AUTHENTICATION_STATUS, "invalid");
        }

        return true;
    }

    /**
     * Called after processing the request. This method handles API key logout logic.
     * If it is an API key request and the user is authenticated, the current user is logged out.
     *
     * @param request  Servlet request
     * @param response Servlet response
     * @throws Exception if an error occurs
     */
    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);

        // If it is an API key request and the user is authenticated, log out the user
        if (ApiKeyHandler.isApiKeyCall(httpRequest) && SecurityUtils.getSubject().isAuthenticated()) {
            SecurityUtils.getSubject().logout();
        }
    }
}