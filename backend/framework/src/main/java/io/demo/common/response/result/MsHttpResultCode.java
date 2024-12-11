package io.demo.common.response.result;

import io.demo.common.exception.IResultCode;

/**
 * <p>Enumeration representing HTTP status codes, mainly used to automatically set the HTTP response status code to the last three digits of the corresponding status code when an exception is thrown.</p>
 * <p>Each status code in the enumeration represents the response status of an HTTP request, commonly used in REST APIs.</p>
 *
 * <p>The status codes use the 100 series, with the first three digits representing the business domain and the last three digits representing the specific HTTP status code:</p>
 * <ul>
 *     <li>Success: 100200</li>
 *     <li>Failure: 100500</li>
 *     <li>Validation Failed: 100400</li>
 *     <li>Unauthorized: 100401</li>
 *     <li>Forbidden: 100403</li>
 *     <li>Not Found: 100404</li>
 * </ul>
 *
 * <p>Implements the {@link IResultCode} interface for standardized exception handling.</p>
 *
 * @see IResultCode
 * @see MsHttpResultCode#SUCCESS
 * @see MsHttpResultCode#FAILED
 * @see MsHttpResultCode#VALIDATE_FAILED
 * @see MsHttpResultCode#UNAUTHORIZED
 * @see MsHttpResultCode#FORBIDDEN
 * @see MsHttpResultCode#NOT_FOUND
 */
public enum MsHttpResultCode implements IResultCode {

    /**
     * Request succeeded
     */
    SUCCESS(100200, "http_result_success"),

    /**
     * Request failed, unknown exception
     */
    FAILED(100500, "http_result_unknown_exception"),

    /**
     * Validation failed
     */
    VALIDATE_FAILED(100400, "http_result_validate"),

    /**
     * Unauthorized, login required
     */
    UNAUTHORIZED(100401, "http_result_unauthorized"),

    /**
     * Forbidden access
     */
    FORBIDDEN(100403, "http_result_forbidden"),

    /**
     * Resource not found
     */
    NOT_FOUND(100404, "http_result_not_found");

    private final int code;
    private final String message;

    /**
     * Enum constructor
     *
     * @param code    HTTP status code
     * @param message Message key corresponding to the status code
     */
    MsHttpResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the HTTP status code
     *
     * @return Numeric value of the HTTP status code
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * Get the message of the status code
     *
     * @return Message corresponding to the status code, processed by translation
     */
    @Override
    public String getMessage() {
        return getTranslationMessage(this.message);
    }
}