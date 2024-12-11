package io.demo.security;

import io.demo.common.util.CommonBeanFactory;
import io.demo.common.util.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Objects;

import static io.demo.security.SessionConstants.ATTR_USER;


/**
 * Session utility class, providing common methods for operating user sessions.
 * <p>
 * Includes functions such as getting current user information, getting session ID, kicking out users, etc.
 * </p>
 */
public class SessionUtils {

    /**
     * Gets the current user's ID.
     *
     * @return The current user's ID, or null if no user information is obtained
     */
    public static String getUserId() {
        SessionUser user = getUser();
        return user == null ? null : user.getId();
    }

    /**
     * Gets the current user information.
     *
     * @return The current user object, or null if no user information is obtained
     */
    public static SessionUser getUser() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            return (SessionUser) session.getAttribute(ATTR_USER);
        } catch (Exception e) {
            LogUtils.warn("Failed to get online user in the background: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the current session ID.
     *
     * @return The current session ID
     */
    public static String getSessionId() {
        return (String) SecurityUtils.getSubject().getSession().getId();
    }

    /**
     * Kicks out the user with the specified username (removes from Redis session).
     *
     * @param username The username
     */
    public static void kickOutUser(String username) {
        // Get Redis session repository
        RedisIndexedSessionRepository sessionRepository = CommonBeanFactory.getBean(RedisIndexedSessionRepository.class);
        if (sessionRepository == null) {
            return;
        }

        // Find sessions by username
        Map<String, ?> users = sessionRepository.findByPrincipalName(username);
        if (MapUtils.isNotEmpty(users)) {
            // Delete all sessions associated with the username
            users.keySet().forEach(k -> {
                sessionRepository.deleteById(k);
                sessionRepository.getSessionRedisOperations().delete("spring:session:sessions:" + k);
            });
        }
    }

    /**
     * Saves the current user information to the session.
     *
     * @param sessionUser The current user object
     */
    public static void putUser(SessionUser sessionUser) {
        // Save user information to session
        SecurityUtils.getSubject().getSession().setAttribute(ATTR_USER, sessionUser);
        // Save user ID to session
        SecurityUtils.getSubject().getSession().setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, sessionUser.getId());
    }

    /**
     * Gets the specified field from the HTTP request header.
     *
     * @param headerName The name of the request header field
     * @return The value of the request header field, or null if the field is not found or an exception occurs
     */
    public static String getHttpHeader(String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return null;
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            return request.getHeader(headerName);
        } catch (Exception e) {
            return null;
        }
    }
}