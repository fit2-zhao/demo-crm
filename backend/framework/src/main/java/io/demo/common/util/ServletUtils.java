package io.demo.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Client utility class
 */
public class ServletUtils {
    /**
     * @param request The request
     * @return The user agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "";
    }

    /**
     * Get the request
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return getUserAgent(request);
    }

    public static String getRequestHost(HttpServletRequest request) {
        String port = ":" + request.getServerPort();
        if (request.getServerPort() == 80 || request.getServerPort() == 443) {
            port = "";
        }
        return request.getScheme() + "://" + request.getServerName() + port;
    }

    public static String getUrl() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        return getRequestHost(request);
    }
}