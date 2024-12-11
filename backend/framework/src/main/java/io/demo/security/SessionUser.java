package io.demo.security;

import io.demo.common.util.CodingUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Represents user information in a session, extending from {@link UserDTO}, and includes a token and session ID to prevent CSRF attacks.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionUser extends UserDTO implements Serializable {

    /**
     * Encryption key used to generate the CSRF Token. It is recommended to read it from configuration or environment variables.
     */
    public static final String secret = "9a9rdqPlTqhpZzkq";

    @Serial
    private static final long serialVersionUID = -7149638440406959033L;

    /**
     * CSRF Token used to prevent Cross-Site Request Forgery attacks.
     */
    private String csrfToken;

    /**
     * Session ID, representing the unique identifier of the current session.
     */
    private String sessionId;

    /**
     * Creates a SessionUser object from UserDTO and generates a CSRF Token and session ID.
     *
     * @param user     User data object
     * @param sessionId Session ID
     * @return {@link SessionUser} object
     */
    public static SessionUser fromUser(UserDTO user, String sessionId) {
        // Create a SessionUser instance
        SessionUser sessionUser = new SessionUser();
        // Copy properties from UserDTO to SessionUser
        BeanUtils.copyProperties(user, sessionUser);

        // Build information used to generate the CSRF Token
        List<String> infos = Arrays.asList(
                user.getId(),
                RandomStringUtils.randomAlphabetic(6),  // Random string to increase diversity
                sessionId,
                String.valueOf(System.currentTimeMillis()) // Current timestamp
        );

        try {
            // Use AES encryption to generate the CSRF Token
            sessionUser.csrfToken = CodingUtils.aesEncrypt(StringUtils.join(infos, "|"), secret, CodingUtils.generateIv());
        } catch (Exception e) {
            // Exception handling: log or return a default value if encryption fails
            sessionUser.csrfToken = StringUtils.EMPTY;
        }

        // Set sessionId
        sessionUser.sessionId = sessionId;
        return sessionUser;
    }
}