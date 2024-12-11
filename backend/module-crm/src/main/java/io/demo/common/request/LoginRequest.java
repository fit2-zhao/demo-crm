package io.demo.common.request;

import io.demo.common.util.rsa.RsaKey;
import io.demo.common.util.rsa.RsaUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>DTO class for login requests.</p>
 * <p>Includes username, password, and authentication information. The username and password are used after RSA decryption.</p>
 */
@Getter
@Setter
public class LoginRequest {

    /**
     * Username, cannot be null, maximum length is 256.
     */
    @NotBlank(message = "{user_name_is_null}")
    @Size(max = 256, message = "{user_name_length_too_long}")
    private String username;

    /**
     * Password, cannot be null, maximum length is 256.
     */
    @NotBlank(message = "{password_is_null}")
    @Size(max = 256, message = "{password_length_too_long}")
    private String password;

    /**
     * Authentication information, optional field.
     */
    private String authenticate;

    /**
     * Gets the decrypted username.
     * <p>If decryption fails, the original username will be returned.</p>
     *
     * @return The decrypted username
     */
    public String getUsername() {
        try {
            RsaKey rsaKey = RsaUtils.getRsaKey();
            return RsaUtils.privateDecrypt(username, rsaKey.getPrivateKey());
        } catch (Exception e) {
            // Decryption failed, return the original username
            return username;
        }
    }

    /**
     * Gets the decrypted password.
     * <p>If decryption fails, the original password will be returned.</p>
     *
     * @return The decrypted password
     */
    public String getPassword() {
        try {
            RsaKey rsaKey = RsaUtils.getRsaKey();
            return RsaUtils.privateDecrypt(password, rsaKey.getPrivateKey());
        } catch (Exception e) {
            // Decryption failed, return the original password
            return password;
        }
    }
}