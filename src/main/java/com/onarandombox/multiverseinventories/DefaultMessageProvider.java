package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.locale.LazyLocaleMessageProvider;
import com.onarandombox.multiverseinventories.locale.LocalizationLoadingException;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.NoSuchLocalizationException;
import com.onarandombox.multiverseinventories.util.Font;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Implementation of MessageProvider.
 */
class DefaultMessageProvider implements LazyLocaleMessageProvider {

    /**
     * Name of localization folder.
     */
    public static final String LOCALIZATION_FOLDER_NAME = "localization";

    private final HashMap<Locale, HashMap<Message, List<String>>> messages;
    private final JavaPlugin plugin;

    private Locale locale = DEFAULT_LOCALE;

    public DefaultMessageProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        messages = new HashMap<Locale, HashMap<Message, List<String>>>();

        try {
            loadLocale(locale);
        } catch (NoSuchLocalizationException e) {
            // let's take the defaults from the enum!
        }
    }

    /**
     * Tries to load the locale.
     *
     * @param locale Locale to try to load.
     * @throws com.onarandombox.multiverseinventories.locale.LocalizationLoadingException if the Locale could not be loaded.
     */
    public void maybeLoadLocale(Locale locale) throws LocalizationLoadingException {
        if (!isLocaleLoaded(locale)) {
            try {
                loadLocale(locale);
            } catch (NoSuchLocalizationException e) {
                throw e;
            }
        }
        if (!isLocaleLoaded(locale))
            throw new LocalizationLoadingException("Couldn't load the localization: "
                    + locale.toString(), locale);
    }

    /**
     * Formats a list of strings by passing each through {@link #format(String, Object...)}.
     *
     * @param strings List of strings to format.
     * @param args    Arguments to pass in via %n.
     * @return List of formatted strings.
     */
    public List<String> format(List<String> strings, Object... args) {
        List<String> formattedStrings = new ArrayList<String>();
        for (String string : strings) {
            formattedStrings.add(format(string, args));
        }
        return formattedStrings;
    }

    /**
     * Formats a string by replacing ampersand with the Section symbol and %n with the corresponding args
     * object where n = argument index + 1.
     *
     * @param string String to format.
     * @param args   Arguments to pass in via %n.
     * @return The formatted string.
     */
    public String format(String string, Object... args) {
        // Replaces & with the Section character
        string = string.replaceAll("(&([a-fA-FkK0-9]))", Font.SECTION_SYMBOL + "$2");
        // If there are arguments, %n notations in the message will be
        // replaced
        if (args != null) {
            for (int j = 0; j < args.length; j++) {
                string = string.replace("%" + (j + 1), args[j].toString());
            }
        }
        return string;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadLocale(Locale l) throws NoSuchLocalizationException {
        messages.remove(l);

        InputStream resstream = null;
        InputStream filestream = null;

        try {
            filestream = new FileInputStream(new File(plugin.getDataFolder(), l.getLanguage() + ".yml"));
        } catch (FileNotFoundException e) {
        }

        try {
            resstream = plugin.getResource(new StringBuilder(LOCALIZATION_FOLDER_NAME).append("/")
                    .append(l.getLanguage()).append(".yml").toString());
        } catch (Exception e) {
        }

        if ((resstream == null) && (filestream == null))
            throw new NoSuchLocalizationException(l);

        messages.put(l, new HashMap<Message, List<String>>(Message.values().length));

        FileConfiguration resconfig = (resstream == null) ? null : YamlConfiguration.loadConfiguration(resstream);
        FileConfiguration fileconfig = (filestream == null) ? null : YamlConfiguration.loadConfiguration(filestream);
        for (Message m : Message.values()) {
            List<String> values = m.getDefault();

            if (resconfig != null) {
                if (resconfig.isList(m.toString())) {
                    values = resconfig.getStringList(m.toString());
                } else {
                    values.add(resconfig.getString(m.toString(), values.get(0)));
                }
            }
            if (fileconfig != null) {
                if (fileconfig.isList(m.toString())) {
                    values = fileconfig.getStringList(m.toString());
                } else {
                    values.add(fileconfig.getString(m.toString(), values.get(0)));
                }
            }

            messages.get(l).put(m, values);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Locale> getLoadedLocales() {
        return messages.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocaleLoaded(Locale l) {
        return messages.containsKey(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(Message key, Object... args) {
        if (!isLocaleLoaded(locale)) {
            return format(key.getDefault().get(0), args);
        } else
            return format(messages.get(locale).get(key).get(0), args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(Message key, Locale locale, Object... args) {
        try {
            maybeLoadLocale(locale);
        } catch (LocalizationLoadingException e) {
            e.printStackTrace();
            return getMessage(key, args);
        }
        return format(messages.get(locale).get(key).get(0), args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMessages(Message key, Object... args) {
        List<String> result;
        if (!isLocaleLoaded(locale)) {
            result = format(key.getDefault(), args);
        } else {
            result = format(this.messages.get(locale).get(key), args);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMessages(Message key, Locale locale, Object... args) {
        try {
            maybeLoadLocale(locale);
        } catch (LocalizationLoadingException e) {
            e.printStackTrace();
            return format(getMessages(key), args);
        }
        return format(messages.get(locale).get(key), args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(Locale locale) {
        if (locale == null)
            throw new IllegalArgumentException("Can't set locale to null!");
        try {
            maybeLoadLocale(locale);
        } catch (LocalizationLoadingException e) {
            if (!locale.equals(DEFAULT_LOCALE))
                throw new IllegalArgumentException("Error while trying to load localization for the given Locale!", e);
        }

        this.locale = locale;
    }
}

