package com.onarandombox.multiverseinventories.api;

import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.multiverseinventories.config.MVIConfig;
import com.onarandombox.multiverseinventories.data.MVIData;
import com.onarandombox.multiverseinventories.group.GroupManager;
import com.onarandombox.multiverseinventories.locale.Messaging;
import com.onarandombox.multiverseinventories.migration.ImportManager;
import com.onarandombox.multiverseinventories.profile.ProfileManager;
import org.bukkit.command.CommandSender;

import java.io.File;

public interface Inventories extends MVPlugin, Messaging {

    /**
     * @return A class used for managing importing data from other similar plugins.
     */
    public ImportManager getImportManager();

    /**
     * @return The pastebin version string.
     */
    public String getVersionInfo();

    /**
     * @return the MVIConfig object which contains settings for this plugin.
     */
    public MVIConfig getSettings();

    /**
     * Nulls the config object and reloads a new one, also resetting the world groups in memory.
     */
    public void reloadConfig();

    /**
     * @return the MVIData object which contains data for this plugin.
     */
    public MVIData getData();

    /**
     * Runs a check for conflicts between groups and displays them to console and sender if not null.
     *
     * @param sender The sender to relay information to.  If null, info only displayed in console.
     */
    public void checkForGroupConflicts(CommandSender sender);

    /**
     * @return The required protocol version of core.
     */
    public int getRequiredProtocol();

    /**
     * @return The World Group manager for this plugin.
     */
    public GroupManager getGroupManager();

    /**
     * @return The Profile manager for this plugin.
     */
    public ProfileManager getProfileManager();

    /**
     * Gets the server's root-folder as {@link File}.
     *
     * @return The server's root-folder
     */
    public File getServerFolder();

    /**
     * Sets this server's root-folder.
     *
     * @param newServerFolder The new server-root
     */
    public void setServerFolder(File newServerFolder);
}
