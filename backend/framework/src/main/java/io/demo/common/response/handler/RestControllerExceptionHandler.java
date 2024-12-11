package io.demo.common.response.handler;

import io.demo.common.exception.IResultCode;
import io.demo.common.exception.GenericException;
import io.demo.common.util.Translator;
import io.demo.common.response.result.MsHttpResultCode;
import io.demo.common.util.ServiceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.lang.ShiroException;
import org.eclipse.jetty.io.EofException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that handles various exceptions and returns a unified error response format.
 */
@RestControllerAdvice
public class RestControllerExceptionHandler {

    /**
     * Handles data validation exceptions and returns specific field validation information.
     *
     * @param ex MethodArgumentNotValidException exception
     * @return ResultHolder containing the encapsulated error information, HTTP status code 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultHolder handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResultHolder.error(MsHttpResultCode.VALIDATE_FAILED.getCode(),
                MsHttpResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    /**
     * Handles exceptions where the request method is not supported, returning HTTP status code 405.
     *
     * @param response  HttpServletResponse response
     * @param exception exception information
     * @return ResultHolder containing the error information
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultHolder handleHttpRequestMethodNotSupportedException(HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        return ResultHolder.error(HttpStatus.METHOD_NOT_ALLOWED.value(), exception.getMessage());
    }

    /**
     * Handles MSException exceptions, setting the HTTP status code and business status code based on errorCode.
     *
     * @param e MSException exception
     * @return ResponseEntity containing the response entity with error information
     */
    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ResultHolder> handlerMSException(GenericException e) {
        IResultCode errorCode = e.getErrorCode();
        if (errorCode == null) {
            // If errorCode is not set, return internal server error
            return ResponseEntity.internalServerError()
                    .body(ResultHolder.error(MsHttpResultCode.FAILED.getCode(), e.getMessage()));
        }

        int code = errorCode.getCode();
        String message = errorCode.getMessage();
        message = Translator.get(message, message);

        if (errorCode instanceof MsHttpResultCode) {
            // If it is of type MsHttpResultCode, use the last three digits of its status code as the HTTP status code
            if (errorCode.equals(MsHttpResultCode.NOT_FOUND)) {
                message = getNotFoundMessage(message);
            }
            return ResponseEntity.status(code % 1000)
                    .body(ResultHolder.error(code, message, e.getMessage()));
        } else {
            // Other types of errors, return status code 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultHolder.error(code, Translator.get(message, message), e.getMessage()));
        }
    }

    /**
     * Handles NOT_FOUND exceptions, concatenating the resource name to provide more detailed error information.
     *
     * @param message error message template
     * @return String concatenated error message
     */
    private static String getNotFoundMessage(String message) {
        String resourceName = ServiceUtils.getResourceName();
        if (StringUtils.isNotBlank(resourceName)) {
            message = String.format(message, Translator.get(resourceName, resourceName));
        } else {
            message = String.format(message, Translator.get("resource.name"));
        }
        ServiceUtils.clearResourceName();
        return message;
    }

    /**
     * Handles all types of exceptions, returning HTTP status code 500 and formatting the exception stack trace.
     *
     * @param e Exception exception
     * @return ResponseEntity containing the response entity with error information
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResultHolder> handleException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(ResultHolder.error(MsHttpResultCode.FAILED.getCode(),
                        e.getMessage(), getStackTraceAsString(e)));
    }

    /**
     * Handles EOF exceptions, determining the request path and returning an appropriate response.
     *
     * @param request HttpServletRequest request
     * @param e       exception information
     * @return ResponseEntity containing the response entity with error information
     */
    @ExceptionHandler({EofException.class})
    public ResponseEntity<Object> handleEofException(HttpServletRequest request, Exception e) {
        String requestURI = request.getRequestURI();
        if (StringUtils.startsWith(requestURI, "/assets")
                || StringUtils.startsWith(requestURI, "/fonts")
                || StringUtils.startsWith(requestURI, "/images")
                || StringUtils.startsWith(requestURI, "/templates")) {
            return ResponseEntity.internalServerError().body(null);
        }
        return ResponseEntity.internalServerError()
                .body(ResultHolder.error(MsHttpResultCode.FAILED.getCode(),
                        e.getMessage(), getStackTraceAsString(e)));
    }

    /**
     * Handles Shiro exceptions, returning HTTP status code 401.
     *
     * @param request   HttpServletRequest request
     * @param response  HttpServletResponse response
     * @param exception exception information
     * @return ResultHolder containing the error information
     */
    @ExceptionHandler(ShiroException.class)
    public ResultHolder exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResultHolder.error(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
    }

    /**
     * Handles Shiro unauthorized exceptions, returning HTTP status code 403.
     *
     * @param request   HttpServletRequest request
     * @param response  HttpServletResponse response
     * @param exception exception information
     * @return ResultHolder containing the error information
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResultHolder unauthorizedExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return ResultHolder.error(HttpStatus.FORBIDDEN.value(), exception.getMessage());
    }

    /**
     * Formats the exception stack trace.
     *
     * @param e Exception exception
     * @return String string representation of the exception stack trace
     */
    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}