package com.onarandombox.multiverseinventories.api;

import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Shares;

import java.util.List;

/**
 * Interface for interacting with the config of this plugin.
 */
public interface InventoriesConfig {

    /**
     * Sets globalDebug level.
     *
     * @param globalDebug The new value. 0 = off.
     */
    void setGlobalDebug(int globalDebug);

    /**
     * Gets globalDebug level.
     *
     * @return globalDebug.
     */
    int getGlobalDebug();

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
    List<WorldGroupProfile> getWorldGroups();

    /**
     * Updates the data of the specified world group in the config.  This will create a
     * ConfigurationSection in the config if one does not exist.
     *
     * @param worldGroup Group to update.
     */
    void updateWorldGroup(WorldGroupProfile worldGroup);

    /**
     * Removes the specified world group in the config.
     *
     * @param worldGroup Group to remove.
     */
    void removeWorldGroup(WorldGroupProfile worldGroup);

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
     * @return true if worlds with no group should be considered part of the default group.
     */
    boolean isDefaultingUngroupedWorlds();

    /**
     * @param useDefaultGroup Set this to true to use the default group for ungrouped worlds.
     */
    void setDefaultingUngroupedWorlds(boolean useDefaultGroup);

    /**
     * Tells whether Multiverse-Inventories should save on player logout and load on player login.
     *
     * @return True if should save and load on player log out and in.
     */
    boolean usingLoggingSaveLoad();

    /**
     * Sets whether Multiverse-Inventories should save on player logout and load on player login.
     *
     * @param useLoggingSaveLoad true if should save and load on player log out and in.
     */
    void setUsingLoggingSaveLoad(boolean useLoggingSaveLoad);

    /**
     * Saves the configuration file to disk.
     */
    void save();

    /**
     * @return A list of optional {@link com.onarandombox.multiverseinventories.api.share.Sharable}s to be treated as
     * regular {@link com.onarandombox.multiverseinventories.api.share.Sharable}s throughout the code.
     * A {@link com.onarandombox.multiverseinventories.api.share.Sharable} marked as optional is ignored if it is not
     * contained in this list.
     */
    Shares getOptionalShares();

    /**
     * @return True if we should check for bypass permissions.
     */
    boolean isUsingBypass();

    /**
     * @param useBypass Whether or not to check for bypass permissions.
     */
    void setUsingBypass(boolean useBypass);

    /**
     * @return True if using separate data for game modes.
     */
    boolean isUsingGameModeProfiles();

    /**
     * @param useGameModeProfile whether to use separate data for game modes.
     */
    void setUsingGameModeProfiles(boolean useGameModeProfile);
}

