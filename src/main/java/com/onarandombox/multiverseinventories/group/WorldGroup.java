package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Shares;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Map;

/**
 * Contains all the information related to a World Group as defined in the plugin's config.yml
 * including the worlds making up the group and the things those worlds share.  Also extends
 * Profile container which allows World Groups to hold player profiles.
 */
public interface WorldGroup extends ProfileContainer {

    /**
     * Get the name of this World Group.
     *
     * @return Name of this World Group.
     */
    String getName();

    /**
     * Sets the name of this World Group.
     *
     * @param name The new name for this World Group.
     */
    void setName(String name);

    /**
     * Adds a world to this world group and updates it in the Config.
     *
     * @param worldName The name of the world to add.
     */
    void addWorld(String worldName);

    /**
     * Adds a world to this world group and optionally updates it in the Config.
     *
     * @param worldName The name of the world to add.
     * @param updateConfig True to update this group in the config.
     */
    void addWorld(String worldName, boolean updateConfig);

    /**
     * Convenience method to add a {@link org.bukkit.World} to this World Group.
     *
     * @param world The world to add.
     */
    void addWorld(World world);

    /**
     * Retrieves all of the worlds in this World Group.
     *
     * @return The worlds of this World Group.
     */
    HashSet<String> getWorlds();

    /**
     * Sets the shares for this World Group.
     *
     * @param shares The new Shares for this World Group.
     */
    void setShares(Shares shares);

    /**
     * Retrieves the shares for this World Group.
     *
     * @return The shares for this World Group.
     */
    Shares getShares();

    /**
     * @return A map containing all the world group data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @param worldName Name of world to check for.
     * @return True if specified world is part of this group.
     */
    boolean containsWorld(String worldName);

    // ItemBlacklist getItemBlacklist(String worldName);
}

