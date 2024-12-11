package io.demo.common.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * GenericException is a custom runtime exception that includes an error code and detailed information.
 */
public class GenericException extends RuntimeException {

    /**
     * Error code
     */
    protected IResultCode errorCode;

    /**
     * Constructor that accepts an error message.
     *
     * @param message Error message
     */
    public GenericException(String message) {
        super(message);
    }

    /**
     * Constructor that accepts an exception object.
     *
     * @param t Exception object
     */
    public GenericException(Throwable t) {
        super(t);
    }

    /**
     * Constructor that accepts an error code, with no detailed information by default.
     *
     * @param errorCode Error code
     */
    public GenericException(IResultCode errorCode) {
        super(StringUtils.EMPTY);
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode cannot be null");
        }
        this.errorCode = errorCode;
    }

    /**
     * Constructor that accepts an error code and a custom error message.
     *
     * @param errorCode Error code
     * @param message   Error message
     */
    public GenericException(IResultCode errorCode, String message) {
        super(message);
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode cannot be null");
        }
        this.errorCode = errorCode;
    }

    /**
     * Constructor that accepts an error code and an exception object.
     *
     * @param errorCode Error code
     * @param t         Exception object
     */
    public GenericException(IResultCode errorCode, Throwable t) {
        super(t);
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode cannot be null");
        }
        this.errorCode = errorCode;
    }

    /**
     * Constructor that accepts a custom error message and an exception object.
     *
     * @param message Error message
     * @param t       Exception object
     */
    public GenericException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * Get the error code.
     *
     * @return Error code
     */
    public IResultCode getErrorCode() {
        return errorCode;
    }

    /**
     * Override the toString method to provide more useful error information.
     *
     * @return Error code and error message
     */
    @Override
    public String toString() {
        return "GenericException{errorCode=" + errorCode + ", message=" + getMessage() + "}";
    }
}