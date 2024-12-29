package org.mvplugins.multiverse.inventories.locale;

import java.io.IOException;
import java.util.Locale;

/**
 * Thrown when an error occurs while a localization is loaded.
 */
public class LocalizationLoadingException extends IOException {
    private final Locale locale;

    public LocalizationLoadingException(Locale locale) {
        this.locale = locale;
    }

    public LocalizationLoadingException(String message, Locale locale) {
        super(message);
        this.locale = locale;
    }

    public LocalizationLoadingException(Throwable cause, Locale locale) {
        super(cause);
        this.locale = locale;
    }

    public LocalizationLoadingException(String message, Throwable cause, Locale locale) {
        super(message, cause);
        this.locale = locale;
    }

    /**
     * Retrieves the locale that was attempting to be loaded.
     *
     * @return The locale that was attempting to be loaded.
     */
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " (While trying to load localization for locale " + getLocale() + ")";
    }
}
