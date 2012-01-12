package com.onarandombox.multiverseinventories.util;

import com.onarandombox.MultiverseCore.utils.DebugLog;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Static plugin debug logger.
 */
public class MVIDebug {

    // BEGIN CHECKSTYLE-SUPPRESSION: Name
    private static DebugLog LOG = null;
    private static String NAME = "Multiverse-Profiles";
    private static String VERSION = "v.???";
    // END CHECKSTYLE-SUPPRESSION: Name

    private MVIDebug() { }

    /**
     * Prepares the log for use.
     *
     * @param plugin The plugin.
     */
    public static void init(JavaPlugin plugin) {
        PluginDescriptionFile pdf = plugin.getDescription();
        NAME = pdf.getName();
        VERSION = pdf.getVersion();
        plugin.getDataFolder().mkdirs();
        LOG = new DebugLog(NAME, plugin.getDataFolder() + File.separator + "debug.log");
    }

    /**
     * Adds the plugin name and optionally the version number to the log message.
     *
     * @param message     Log message
     * @param showVersion Whether to show version in log message
     * @return Modified message
     */
    public static String getString(String message, boolean showVersion) {
        String string = "[" + NAME;
        if (showVersion) string += " " + VERSION;
        string = string + "] " + message;
        return string;
    }

    /**
     * Returns the logger object.
     *
     * @return Logger object
     */
    public static DebugLog getLog() {
        return LOG;
    }

    /**
     * Returns the Name and Version as a combined string.
     *
     * @return "$Name v$Version"
     */
    public static String getNameVersion() {
        return NAME + " " + VERSION;
    }

    /**
     * Custom log method.
     *
     * @param level   Log level
     * @param message Log message
     */
    public static void log(Level level, String message) {
        MVIDebug.log(level, message, false);
    }

    /**
     * Custom log method.
     *
     * @param level   Log level
     * @param message Log message
     * @param showVersion True adds version into message
     */
    public static void log(Level level, String message, boolean showVersion) {
        if (LOG != null) {
            LOG.log(level, getString(message, showVersion));
        } else {
            System.out.println("Debug log not initialized!");
            System.out.println(level.toString() + getString(message, showVersion));
        }
    }

    /**
     * Fine level logging.
     *
     * @param message Log message
     */
    public static void fine(String message) {
        fine(message, false);
    }

    /**
     * Fine level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void fine(String message, boolean showVersion) {
        MVIDebug.log(Level.FINE, message, showVersion);
    }

    /**
     * Finer level logging.
     *
     * @param message Log message
     */
    public static void finer(String message) {
        fine(message, false);
    }

    /**
     * Finer level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void finer(String message, boolean showVersion) {
        MVIDebug.log(Level.FINER, message, showVersion);
    }

    /**
     * Finest level logging.
     *
     * @param message Log message
     */
    public static void finest(String message) {
        finest(message, false);
    }

    /**
     * Finest level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void finest(String message, boolean showVersion) {
        MVIDebug.log(Level.FINEST, message, showVersion);
    }

    /**
     * Info level logging.
     *
     * @param message Log message
     */
    public static void info(String message) {
        info(message, false);
    }

    /**
     * Info level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void info(String message, boolean showVersion) {
        MVIDebug.log(Level.INFO, message, showVersion);
    }

    /**
     * Config level logging.
     *
     * @param message Log message
     */
    public static void config(String message) {
        config(message, false);
    }

    /**
     * Config level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void config(String message, boolean showVersion) {
        MVIDebug.log(Level.CONFIG, message, showVersion);
    }

    /**
     * All level logging.
     *
     * @param message Log message
     */
    public static void all(String message) {
        all(message, false);
    }

    /**
     * All level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void all(String message, boolean showVersion) {
        MVIDebug.log(Level.ALL, message, showVersion);
    }

    /**
     * Off level logging.
     *
     * @param message Log message
     */
    public static void off(String message) {
        off(message, false);
    }

    /**
     * Off level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void off(String message, boolean showVersion) {
        MVIDebug.log(Level.OFF, message, showVersion);
    }

    /**
     * Warning level logging.
     *
     * @param message Log message
     */
    public static void warning(String message) {
        warning(message, false);
    }

    /**
     * Warning level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void warning(String message, boolean showVersion) {
        MVIDebug.log(Level.WARNING, message, showVersion);
    }

    /**
     * Severe level logging.
     *
     * @param message Log message
     */
    public static void severe(String message) {
        severe(message, false);
    }

    /**
     * Severe level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void severe(String message, boolean showVersion) {
        MVIDebug.log(Level.SEVERE, message, showVersion);
    }

}
