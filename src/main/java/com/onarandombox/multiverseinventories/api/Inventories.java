package com.onarandombox.multiverseinventories.api;

import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypeManager;
import com.onarandombox.multiverseinventories.api.profile.WorldProfileManager;
import com.onarandombox.multiverseinventories.locale.Messaging;
import com.onarandombox.multiverseinventories.migration.ImportManager;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Interface for Multiverse-Inventories main class which contains many methods useful
 * for outside plugins wanting to interact with Multiverse-Inventories.
 */
public interface Inventories extends MVPlugin, Messaging, Plugin {

    /**
     * @return A class used for managing importing data from other similar plugins.
     */
    ImportManager getImportManager();

    /**
     * @return The pastebin version string.
     */
    String getVersionInfo();

    /**
     * @return the Config object which contains settings for this plugin.
     */
    InventoriesConfig getMVIConfig();

    /**
     * Nulls the config object and reloads a new one, also resetting the world groups in memory.
     */
    void reloadConfig();

    /**
     * @return the PlayerData object which contains data for this plugin.
     */
    PlayerData getData();

    /**
     * @return The required protocol version of core.
     */
    int getRequiredProtocol();

    /**
     * @return The World Group manager for this plugin.
     */
    GroupManager getGroupManager();

    /**
     * @return The World/Group Profile manager for this plugin.
     * This is where you find access to individual player data.
     */
    WorldProfileManager getWorldManager();

    /**
     * Gets the server's root-folder as {@link File}.
     *
     * @return The server's root-folder
     */
    File getServerFolder();

    /**
     * Sets this server's root-folder.
     *
     * @param newServerFolder The new server-root
     */
    void setServerFolder(File newServerFolder);

    /**
     * @return The ProfileType manager which will manage loading all profile types and retrieving the different types
     * from memory.
     */
    ProfileTypeManager getProfileTypeManager();
}
