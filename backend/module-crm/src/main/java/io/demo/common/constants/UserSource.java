package io.demo.common.constants;

/**
 * Enum class for user source types, used to identify the source of users.
 * <p>
 * This enum class defines different user source types, including local, LDAP, CAS, OIDC, OAuth2, and QR code.
 * </p>
 */
public enum UserSource {

    /**
     * Local user source, indicating that the user registers and logs in through the local system.
     */
    LOCAL,

    /**
     * LDAP user source, indicating that the user is authenticated through the LDAP (Lightweight Directory Access Protocol) system.
     */
    LDAP,

    /**
     * CAS user source, indicating that the user is authenticated through CAS (Central Authentication Service).
     */
    CAS,

    /**
     * OIDC user source, indicating that the user is authenticated through OIDC (OpenID Connect).
     */
    OIDC,

    /**
     * OAuth2 user source, indicating that the user is authenticated through the OAuth2 authorization framework.
     */
    OAUTH2,

    /**
     * QR code user source, indicating that the user logs in by scanning a QR code.
     */
    QR_CODE;
}