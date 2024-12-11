package io.demo.common.security;

import io.demo.common.util.CodingUtils;
import io.demo.common.util.CommonBeanFactory;
import io.demo.security.SessionConstants;
import io.demo.security.SessionUser;
import io.demo.security.SessionUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

/**
 * Custom filter for handling CSRF validation.
 * This filter ensures that only authenticated users can access non-public resources and validates the CSRF token and Referer in the request.
 */
public class CsrfFilter extends AnonymousFilter {

    /**
     * Performs CSRF validation before processing the request.
     * If the request is not authenticated or contains a valid CSRF token in the headers, the request is allowed to proceed.
     * Otherwise, an appropriate exception is thrown or an invalid authentication status is returned.
     *
     * @param request     Servlet request
     * @param response    Servlet response
     * @param mappedValue Mapped values in the filter chain
     * @return true if the request should continue processing, false otherwise
     */
    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);

        // Return invalid authentication status if the user is not authenticated
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader(SessionConstants.AUTHENTICATION_STATUS, SessionConstants.AUTHENTICATION_INVALID);
            return true;
        }

        // No CSRF validation is required for error pages
        if (httpServletRequest.getRequestURI().equals("/error")) {
            return true;
        }

        // No CSRF validation is required for API requests
        if (ApiKeyHandler.isApiKeyCall(httpServletRequest)) {
            return true;
        }

        // No CSRF validation is required for WebSocket requests
        String websocketKey = httpServletRequest.getHeader("Sec-WebSocket-Key");
        if (StringUtils.isNotBlank(websocketKey)) {
            return true;
        }

        // Get CSRF token and X-Auth-Token from request headers
        String csrfToken = httpServletRequest.getHeader(SessionConstants.CSRF_TOKEN);
        String xAuthToken = httpServletRequest.getHeader(SessionConstants.HEADER_TOKEN);

        // Validate CSRF token and X-Auth-Token
        validateToken(csrfToken, xAuthToken);

        // Validate Referer
        validateReferer(httpServletRequest);

        return true;
    }

    /**
     * Validates the Referer in the request against the configured domain names.
     * If no Referer domain is configured, validation is skipped.
     *
     * @param request HttpServletRequest request
     */
    private void validateReferer(HttpServletRequest request) {
        Environment env = CommonBeanFactory.getBean(Environment.class);
        assert env != null;
        String domains = env.getProperty("referer.urls");

        // Skip validation if referer.urls is not configured
        if (StringUtils.isBlank(domains)) {
            return;
        }

        String[] allowedDomains = StringUtils.split(domains, ",");
        String referer = request.getHeader(HttpHeaders.REFERER);

        // Validate if the Referer is in the allowed domain list
        if (allowedDomains != null && !ArrayUtils.contains(allowedDomains, referer)) {
            throw new RuntimeException("CSRF error: invalid referer");
        }
    }

    /**
     * Validates the CSRF token and X-Auth-Token in the request.
     * Throws an exception if the token is invalid.
     *
     * @param csrfToken  CSRF token
     * @param xAuthToken X-Auth-Token
     */
    private void validateToken(String csrfToken, String xAuthToken) {
        if (StringUtils.isBlank(csrfToken)) {
            throw new RuntimeException("CSRF token is empty");
        }

        // Decrypt CSRF token
        csrfToken = CodingUtils.aesDecrypt(csrfToken, SessionUser.secret, CodingUtils.generateIv());

        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(csrfToken), "|");
        if (signatureArray.length != 4) {
            throw new RuntimeException("Invalid CSRF token format");
        }

        // Validate if the user ID and session ID match
        if (!StringUtils.equals(SessionUtils.getUserId(), signatureArray[0])) {
            throw new RuntimeException("CSRF token does not match the current user");
        }

        // Validate if the sessionId or X-Auth-Token match
        if (!StringUtils.equals(SessionUtils.getSessionId(), signatureArray[2]) &&
                !StringUtils.equals(xAuthToken, signatureArray[2])) {
            throw new RuntimeException("CSRF token does not match the current session");
        }
    }
}