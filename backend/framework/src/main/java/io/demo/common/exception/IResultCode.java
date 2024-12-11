package io.demo.common.exception;

import io.demo.common.util.Translator;

/**
 * API interface status code.
 * <p>
 * 1. If you want to return a status code with HTTP meaning, use MsHttpResultCode.
 * 2. Business status codes: each module defines its own status code enumeration class and manages it independently.
 * 3. Business error codes: defined as 6-digit numbers.
 * 4. Business error codes: the first three digits represent the module name, and the last three digits represent the error code. For example: 101001  101: System settings, 001: Resource pool validation failed.
 * 5. When an exception needs to be thrown, set the status code enumeration object for the exception.
 * <p>
 * <p>
 * @Author: jianxing
 */
public interface IResultCode {
    /**
     * Return status code.
     *
     * @return Status code
     */
    int getCode();

    /**
     * Return status code message.
     *
     * @return Status code message
     */
    String getMessage();

    /**
     * Return the internationalized status code message.
     * If no match is found, return the original text.
     *
     * @param message Status code message
     * @return Internationalized status code message
     */
    default String getTranslationMessage(String message) {
        return Translator.get(message, message);
    }
}