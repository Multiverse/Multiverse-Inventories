package org.mvplugins.multiverse.inventories.locale;

import org.jvnet.hk2.annotations.Contract;

import java.util.List;
import java.util.Locale;

/**
 * <p>Multiverse 2 MessageProvider.</p>
 *
 * This interface describes a Multiverse-MessageProvider.
 */
@Contract
public interface MessageProvider {
    /**
     * The default locale.
     */
    Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * Returns a message (as {@link String}) for the specified key (as {@link Message}).
     *
     * @param key  The key
     * @param args Args for String.format()
     * @return The message
     */
    String getMessage(Message key, Object... args);

    /**
     * Returns a message (as {@link String}) in a specified {@link Locale} for the specified key (as {@link Message}).
     *
     * @param key    The Key
     * @param locale The {@link Locale}
     * @param args   Args for String.format()
     * @return The message
     */
    String getMessage(Message key, Locale locale, Object... args);

    /**
     * Returns a message (as {@link List}) of Strings for the specified key (as {@link Message}).
     *
     * @param key  The key
     * @param args Args for String.format()
     * @return The messages
     */
    List<String> getMessages(Message key, Object... args);

    /**
     * Returns a message (as {@link List}) of Strings in a specified {@link Locale} for the specified key (as {@link Message}).
     *
     * @param key    The key
     * @param locale The {@link Locale}
     * @param args   Args for String.format()
     * @return The messages
     */
    List<String> getMessages(Message key, Locale locale, Object... args);

    /**
     * Returns the Locale this MessageProvider is currently using.
     *
     * @return The locale this MessageProvider is currently using.
     */
    Locale getLocale();

    /**
     * Sets the locale for this MessageProvider.
     *
     * @param locale The new {@link Locale}.
     */
    void setLocale(Locale locale);
}

