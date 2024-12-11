package io.demo.common.security;

import io.demo.common.util.CodingUtils;
import io.demo.common.util.CommonBeanFactory;
import io.demo.modules.system.domain.UserKey;
import io.demo.modules.system.service.UserKeyService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Utility class for handling API key authentication, including functions for retrieving users,
 * verifying if a request contains an API key, and validating signatures.
 */
public class ApiKeyHandler {

    public static final String API_ACCESS_KEY = "accessKey"; // API key field
    public static final String API_SIGNATURE = "signature";  // API signature field

    /**
     * Retrieves the user ID based on the API key and signature in the request.
     *
     * @param request HTTP request
     * @return User ID, or null if the request is invalid
     */
    public static String getUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return getUser(request.getHeader(API_ACCESS_KEY), request.getHeader(API_SIGNATURE));
    }

    /**
     * Determines if the request contains a valid API key and signature.
     *
     * @param request HTTP request
     * @return true if the request contains a valid API key and signature, false otherwise
     */
    public static Boolean isApiKeyCall(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return !StringUtils.isBlank(request.getHeader(API_ACCESS_KEY)) && !StringUtils.isBlank(request.getHeader(API_SIGNATURE));
    }

    /**
     * Validates the user based on the provided accessKey and signature, and returns the user ID.
     * Throws an appropriate exception if validation fails.
     *
     * @param accessKey API key
     * @param signature API signature
     * @return User ID
     * @throws RuntimeException if validation fails, an appropriate exception is thrown
     */
    public static String getUser(String accessKey, String signature) {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(signature)) {
            return null;
        }

        // Retrieve user key information
        UserKey userKey = Objects.requireNonNull(CommonBeanFactory.getBean(UserKeyService.class)).getUserKey(accessKey);
        if (userKey == null) {
            throw new RuntimeException("invalid accessKey");
        }

        // Check if the accessKey is enabled
        if (BooleanUtils.isFalse(userKey.getEnable())) {
            throw new RuntimeException("accessKey is disabled");
        }

        // Check if the accessKey is expired
        if (BooleanUtils.isFalse(userKey.getForever())) {
            if (userKey.getExpireTime() == null || userKey.getExpireTime() < System.currentTimeMillis()) {
                throw new RuntimeException("accessKey is expired");
            }
        }

        // Decrypt and validate the signature
        String signatureDecrypt;
        try {
            signatureDecrypt = CodingUtils.aesDecrypt(signature, userKey.getSecretKey(), accessKey.getBytes());
        } catch (Throwable t) {
            throw new RuntimeException("invalid signature", t);
        }

        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(signatureDecrypt), "|");
        if (signatureArray.length < 2) {
            throw new RuntimeException("invalid signature");
        }

        // Validate if the accessKey in the signature matches
        if (!StringUtils.equals(accessKey, signatureArray[0])) {
            throw new RuntimeException("invalid signature");
        }

        long signatureTime;
        try {
            signatureTime = Long.parseLong(signatureArray[signatureArray.length - 1]);
        } catch (Exception e) {
            throw new RuntimeException("invalid signature time", e);
        }

        // Validate if the signature is expired (30 minutes)
        if (Math.abs(System.currentTimeMillis() - signatureTime) > 1800000) {
            throw new RuntimeException("expired signature");
        }

        // Return the user creator ID
        return userKey.getCreateUser();
    }
}