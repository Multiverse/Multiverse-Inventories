package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Shares;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.Set;

/**
 * Contains all the information related to a World Group as defined in the plugin's config.yml
 * including the worlds making up the group and the things those worlds share.  Also extends
 * Profile container which allows World Groups to hold player profiles.
 */
public interface WorldGroupProfile extends ProfileContainer {

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
     * @param worldName    The name of the world to add.
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
     * Removes a world from this world group and updates the group in the Config.
     *
     * @param worldName The name of the world to remove.
     */
    void removeWorld(String worldName);

    /**
     * Removes a world from this world group and optionally updates it in the Config.
     *
     * @param worldName    The name of the world to remove.
     * @param updateConfig True to update this group in the config.
     */
    void removeWorld(String worldName, boolean updateConfig);

    /**
     * Convenience method to remove a {@link org.bukkit.World} from this World Group.
     *
     * @param world The world to remove.
     */
    void removeWorld(World world);

    /**
     * Retrieves all of the worlds in this World Group.
     *
     * @return The worlds of this World Group.
     */
    Set<String> getWorlds();

    /**
     * Checks if this group is sharing sharable.  This will check both shares and negative shares of the group.
     * This is the preferred method for checking if a group shares something as shares may contain ALL shares while
     * ones indicated in negative shares means those aren't actually shared.
     *
     * @param sharable Sharable to check if sharing.
     * @return true is the sharable is shared for this group.
     */
    boolean isSharing(Sharable sharable);

    /**
     * Retrieves the shares for this World Group.
     *
     * @return The shares for this World Group.
     */
    Shares getShares();

    /**
     * Retrieves the negated shares for this World Group.  Elements in this Shares set may be found in
     * {@link #getShares()}.  Their presence here means that the copies in {@link #getShares()} will be ignored.
     *
     * @return The negated shares for this World Group.
     */
    Shares getNegativeShares();

    /**
     * @return A map containing all the world group data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @param worldName Name of world to check for.
     * @return True if specified world is part of this group.
     */
    boolean containsWorld(String worldName);

    /**
     * @return The name of the world that will be used as the spawn for this group.
     *         Or null if no world was specified as the group spawn world.
     */
    String getSpawnWorld();

    /**
     * @param worldName The name of the world to set this groups spawn to.
     */
    void setSpawnWorld(String worldName);

    /**
     * @return The priority for the respawn event that this spawn will act on.
     */
    EventPriority getSpawnPriority();

    /**
     * @param priority The priority that will be used for respawning the player at
     *                 this group's spawn location if there is one set.
     */
    void setSpawnPriority(EventPriority priority);

    //Set<ProfileType> getProfileTypes();

    // ItemBlacklist getItemBlacklist(String worldName);
}

