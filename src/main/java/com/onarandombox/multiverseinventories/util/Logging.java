package com.onarandombox.multiverseinventories.util;

import com.onarandombox.MultiverseCore.utils.DebugLog;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static plugin logger.
 */
public class Logging {

    // BEGIN CHECKSTYLE-SUPPRESSION: Name
    private static Logger LOG = Logger.getLogger("Minecraft");
    private static String NAME = "Multiverse-Inventories";
    private static String VERSION = "v.???";
    private static int DEBUG_LEVEL = 0;
    private static DebugLog debugLog = null;
    // END CHECKSTYLE-SUPPRESSION: Name

    private Logging() {
        throw new AssertionError();
    }

    /**
     * Prepares the log for use.
     *
     * @param plugin The plugin.
     */
    public static void init(JavaPlugin plugin) {
        PluginDescriptionFile pdf = plugin.getDescription();
        NAME = pdf.getName();
        VERSION = pdf.getVersion();
    }

    /**
     * Safely closes the debug log file handle.
     */
    public static void close() {
        if (debugLog != null) {
            debugLog.close();
        }
    }

    /**
     * Prepares the log for use.
     *
     * @param plugin The plugin.
     */
    public static void initDebug(JavaPlugin plugin) {
        plugin.getDataFolder().mkdirs();
        debugLog = new DebugLog(NAME, plugin.getDataFolder() + File.separator + "debug.log");
    }

    /**
     * @param debugLevel 0 = off, 1-3 = debug level
     */
    public static void setDebugMode(int debugLevel) {
        Logging.DEBUG_LEVEL = debugLevel;
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
     * Adds the plugin's debug name to the message.
     *
     * @param message Log message
     * @return Modified message
     */
    public static String getDebugString(String message) {
        return "[" + NAME + "-Debug] " + message;
    }

    /**
     * Returns the logger object.
     *
     * @return Logger object
     */
    public static Logger getLog() {
        return LOG;
    }

    /**
     * Custom log method.
     *
     * @param level       Log level
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void log(Level level, String message, boolean showVersion) {
        if (level == Level.FINE && Logging.DEBUG_LEVEL >= 1) {
            debug(Level.INFO, message);
        } else if (level == Level.FINER && Logging.DEBUG_LEVEL >= 2) {
            debug(Level.INFO, message);
        } else if (level == Level.FINEST && Logging.DEBUG_LEVEL >= 3) {
            debug(Level.INFO, message);
        } else if (level != Level.FINE && level != Level.FINER && level != Level.FINEST) {
            LOG.log(level, getString(message, showVersion));
            if (debugLog != null) {
                debugLog.log(level, getString(message, showVersion));
            }
        }
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
     * Debug level logging.
     *
     * @param message Log message
     */
    private static void debug(Level level, String message) {
        LOG.log(level, getDebugString(message));
        if (debugLog != null) {
            debugLog.log(level, getDebugString(message));
        }
    }

    /**
     * Info level logging.
     *
     * @param message Log message
     */
    public static void fine(String message) {
        fine(message, false);
    }

    /**
     * Info level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void fine(String message, boolean showVersion) {
        Logging.log(Level.FINE, message, showVersion);
    }

    /**
     * Info level logging.
     *
     * @param message Log message
     */
    public static void finer(String message) {
        finer(message, false);
    }

    /**
     * Info level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void finer(String message, boolean showVersion) {
        Logging.log(Level.FINER, message, showVersion);
    }

    /**
     * Info level logging.
     *
     * @param message Log message
     */
    public static void finest(String message) {
        finest(message, false);
    }

    /**
     * Info level logging.
     *
     * @param message     Log message
     * @param showVersion True adds version into message
     */
    public static void finest(String message, boolean showVersion) {
        Logging.log(Level.FINEST, message, showVersion);
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
        Logging.log(Level.INFO, message, showVersion);
    }

    /**
     * Info level logging.  Only displays if debug mode is enabled.
     *
     * @param message Log message
     */
    /*public static void debug(String message) {
        if (MultiverseInventories.getConfig().isDebugging()) {
            LOG.info(getString(message, true));
        }
    }*/

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
        Logging.log(Level.WARNING, message, showVersion);
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
        Logging.log(Level.SEVERE, message, showVersion);
    }
}

