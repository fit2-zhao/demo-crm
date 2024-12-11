package io.demo.common.util;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Translation utility class for obtaining localized messages from the message source.
 * <p>
 * This class provides functionality to retrieve translations from the message source based on message keys.
 * </p>
 */
public class Translator {

    private static MessageSource messageSource;

    /**
     * Injects the MessageSource for internationalized message handling.
     *
     * @param messageSource Spring's MessageSource instance
     */
    @Resource
    public void setMessageSource(MessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    /**
     * Retrieves the translated message based on the given message key.
     *
     * @param key Message key
     * @return Translated message, or "Not Support Key: " + key if the message is not found
     */
    public static String get(String key) {
        return messageSource.getMessage(key, null, "Not Support Key: " + key, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieves the translated message based on the given message key, or returns the specified default message if the message is not found.
     *
     * @param key            Message key
     * @param defaultMessage Default message
     * @return Translated message, or the default message if not found
     */
    public static String get(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieves the translated message based on the given message key and specified locale.
     *
     * @param key    Message key
     * @param locale Specified locale
     * @return Translated message
     */
    public static String get(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Retrieves the translated message based on the given message key and parameters.
     * Supports the insertion of formatted parameters.
     *
     * @param key  Message key
     * @param args Formatting parameters
     * @return Translated message
     */
    public static String getWithArgs(String key, Object... args) {
        return messageSource.getMessage(key, args, "Not Support Key: " + key, LocaleContextHolder.getLocale());
    }
}