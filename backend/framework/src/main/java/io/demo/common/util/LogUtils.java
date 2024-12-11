package io.demo.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * LogUtils provides utility methods for logging, supporting different log levels.
 */
public class LogUtils {
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";

    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtils.class);

    /**
     * Outputs log information based on the log level.
     *
     * @param msg   The log message to output
     * @param level The log level, supports DEBUG, INFO, WARN, ERROR
     */
    public static void writeLog(Object msg, String level) {
        String message = getMsg(msg);

        switch (level) {
            case DEBUG:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(message);
                }
                break;
            case INFO:
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(message);
                }
                break;
            case WARN:
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(message);
                }
                break;
            case ERROR:
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(message);
                }
                break;
            default:
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(StringUtils.EMPTY);
                }
        }
    }

    /**
     * Outputs an INFO level log.
     *
     * @param msg The log message to output
     */
    public static void info(Object msg) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(getMsg(msg));
        }
    }

    /**
     * Outputs an INFO level log with formatted parameters.
     *
     * @param message The formatted log message
     * @param args    The parameters
     */
    public static void info(String message, Object... args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(message, args);
        }
    }

    /**
     * Outputs an INFO level log with one parameter.
     *
     * @param msg  The log message to output
     * @param arg1 The first parameter
     */
    public static void info(Object msg, Object arg1) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(getMsg(msg), arg1);
        }
    }

    /**
     * Outputs an INFO level log with multiple parameters.
     *
     * @param msg  The log message to output
     * @param args The parameter array
     */
    public static void info(Object msg, Object[] args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(getMsg(msg), args);
        }
    }

    /**
     * Outputs a DEBUG level log.
     *
     * @param msg The log message to output
     */
    public static void debug(Object msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getMsg(msg));
        }
    }

    /**
     * Outputs a DEBUG level log with one parameter.
     *
     * @param msg  The log message to output
     * @param arg1 The first parameter
     */
    public static void debug(Object msg, Object arg1) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getMsg(msg), arg1);
        }
    }

    /**
     * Outputs a DEBUG level log with multiple parameters.
     *
     * @param msg  The log message to output
     * @param args The parameter array
     */
    public static void debug(Object msg, Object[] args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getMsg(msg), args);
        }
    }

    /**
     * Outputs a WARN level log.
     *
     * @param msg The log message to output
     */
    public static void warn(Object msg) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(getMsg(msg));
        }
    }

    /**
     * Outputs a WARN level log with one parameter.
     *
     * @param msg  The log message to output
     * @param arg1 The first parameter
     */
    public static void warn(Object msg, Object arg1) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(getMsg(msg), arg1);
        }
    }

    /**
     * Outputs a WARN level log with multiple parameters.
     *
     * @param msg  The log message to output
     * @param args The parameter array
     */
    public static void warn(Object msg, Object[] args) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(getMsg(msg), args);
        }
    }

    /**
     * Outputs an ERROR level log.
     *
     * @param msg The log message to output
     */
    public static void error(Object msg) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(getMsg(msg));
        }
    }

    /**
     * Outputs an ERROR level log with an exception.
     *
     * @param e The exception information
     */
    public static void error(Throwable e) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(getMsg(e), e);
        }
    }

    /**
     * Outputs an ERROR level log with one parameter.
     *
     * @param msg  The log message to output
     * @param arg1 The first parameter
     */
    public static void error(Object msg, Object arg1) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(getMsg(msg), arg1);
        }
    }

    /**
     * Outputs an ERROR level log with multiple parameters.
     *
     * @param msg  The log message to output
     * @param args The parameter array
     */
    public static void error(Object msg, Object[] args) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(getMsg(msg), args);
        }
    }

    /**
     * Outputs an ERROR level log with an exception.
     *
     * @param msg The log message to output
     * @param ex  The exception
     */
    public static void error(Object msg, Throwable ex) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(getMsg(msg), ex);
        }
    }

    /**
     * Gets the log message.
     *
     * @param msg The log message to output
     * @param ex  The exception information
     * @return The formatted log message
     */
    private static String getMsg(Object msg, Throwable ex) {
        String message = (msg != null) ? msg.toString() : "null";
        String methodName = getLogMethod();
        String exceptionMessage = (ex != null) ? "[" + ex.getMessage() + "]" : "";
        return "Method[" + methodName + "]" + "[" + message + "]" + exceptionMessage;
    }

    /**
     * Gets the log message.
     *
     * @param msg The log message to output
     * @return The formatted log message
     */
    private static String getMsg(Object msg) {
        return getMsg(msg, null);
    }

    /**
     * Gets the calling class name.
     *
     * @return The calling class name
     */
    private static String getLogClass() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        return (stack.length > 3) ? stack[3].getClassName() : StringUtils.EMPTY;
    }

    /**
     * Gets the calling method name.
     *
     * @return The calling method name
     */
    private static String getLogMethod() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        return (stack.length > 4) ? stack[4].getMethodName() : StringUtils.EMPTY;
    }

    /**
     * Converts the exception stack trace to a string.
     *
     * @param e The exception
     * @return The string representation of the exception stack trace
     */
    public static String toString(Throwable e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
}