package io.demo.common.response.handler;

import io.demo.common.response.result.MsHttpResultCode;
import lombok.Data;

/**
 * The ResultHolder class is used to encapsulate the response results of an interface, including status code, message, detailed information, and returned data.
 */
@Data
public class ResultHolder {

    /**
     * Status code indicating whether the request was successful, default value is 200 (success).
     */
    private int code = MsHttpResultCode.SUCCESS.getCode();

    /**
     * Description message returned to the frontend, usually an error message or success message.
     */
    private String message;

    /**
     * Detailed description information, such as storing exception logs when an exception occurs.
     */
    private Object messageDetail;

    /**
     * Returned data, which can be any type of object.
     */
    private Object data = "";

    /**
     * Default constructor, initializes default values.
     */
    public ResultHolder() {
    }

    /**
     * Constructor, initializes the returned data.
     *
     * @param data Returned data
     */
    public ResultHolder(Object data) {
        this.data = data;
    }

    /**
     * Constructor, initializes the status code and message.
     *
     * @param code Status code
     * @param msg Message
     */
    public ResultHolder(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    /**
     * Constructor, initializes the status code, message, and data.
     *
     * @param code Status code
     * @param msg Message
     * @param data Returned data
     */
    public ResultHolder(int code, String msg, Object data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    /**
     * Constructor, initializes the status code, message, detailed information, and data.
     *
     * @param code Status code
     * @param msg Message
     * @param messageDetail Detailed information
     * @param data Returned data
     */
    public ResultHolder(int code, String msg, Object messageDetail, Object data) {
        this.code = code;
        this.message = msg;
        this.messageDetail = messageDetail;
        this.data = data;
    }

    /**
     * Success response, returns a ResultHolder with data.
     *
     * @param obj Returned data
     * @return ResultHolder Encapsulated success response
     */
    public static ResultHolder success(Object obj) {
        return new ResultHolder(obj);
    }

    /**
     * Error response, returns a ResultHolder with status code and message.
     *
     * @param code Status code
     * @param message Error message
     * @return ResultHolder Encapsulated error response
     */
    public static ResultHolder error(int code, String message) {
        return new ResultHolder(code, message, null, null);
    }

    /**
     * Error response, returns a ResultHolder with message and detailed information.
     *
     * @param message Error message
     * @param messageDetail Detailed error information
     * @return ResultHolder Encapsulated error response
     */
    public static ResultHolder error(String message, String messageDetail) {
        return new ResultHolder(-1, message, messageDetail, null);
    }

    /**
     * Error response, returns a ResultHolder with status code, message, and detailed information.
     *
     * @param code Status code
     * @param message Error message
     * @param messageDetail Detailed error information
     * @return ResultHolder Encapsulated error response
     */
    public static ResultHolder error(int code, String message, Object messageDetail) {
        return new ResultHolder(code, message, messageDetail, null);
    }

    /**
     * Special case response, for example, the interface can normally return HTTP status code 200, but needs to provide error information to the frontend.
     *
     * @param code Custom status code
     * @param message Message returned to the frontend
     * @return ResultHolder Encapsulated response
     */
    public static ResultHolder successCodeErrorInfo(int code, String message) {
        return new ResultHolder(code, message, null, null);
    }
}