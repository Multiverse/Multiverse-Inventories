package com.onarandombox.multiverseinventories.config;

import com.onarandombox.multiverseinventories.group.WorldGroup;

import java.util.List;

/**
 * Interface for interacting with the config of this plugin.
 */
public interface MIConfig {

    /**
     * Retrieves the underlying FileConfiguration object for direct modification.
     *
     * @return The underlying FileConfiguration object.
     */
    CommentedConfiguration getConfig();

    /**
     * Checks to see if debug mode is set in the config.
     *
     * @return True if debug mode is enabled.
     */
    boolean isDebugging();

    /**
     * Retrieves the locale string from the config.
     *
     * @return The locale string.
     */
    String getLocale();

    /**
     * Retrieves the list of groups set up in the config.
     *
     * @return List of world groups or null if there are none.
     */
    List<WorldGroup> getWorldGroups();

    /**
     * Updates the data of the specified world group in the config.  This will create a
     * ConfigurationSection in the config if one does not exist.
     *
     * @param worldGroup Group to update.
     */
    void updateWorldGroup(WorldGroup worldGroup);

    /**
     * Tells whether this is the first time the plugin has run as set by a config flag.
     *
     * @return True if first_run is set to true in config.
     */
    boolean isFirstRun();

    /**
     * Sets the first_run flag in the config so that the plugin no longer thinks it is the first run.
     *
     * @param firstRun What to set the flag to in the config.
     */
    void setFirstRun(boolean firstRun);

    /**
     * Convenience method for saving the config to disk.
     */
    void save();
}
