package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.share.Shares;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;

/**
 * Contains all the information related to a World Group as defined in the plugin's config.yml
 * including the worlds making up the group and the things those worlds share.
 */
public interface WorldGroup {

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
     * Adds a world to this world group.
     *
     * @param worldName The name of the world to add.
     */
    void addWorld(String worldName);

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
     * Adds the data of this class to the given ConfigurationSection.
     *
     * @param groupData The ConfigurationSection to add this World Group to.
     */
    void serialize(ConfigurationSection groupData);

    //ItemBlacklist getItemBlacklist(String worldName);
}
