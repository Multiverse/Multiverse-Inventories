package com.onarandombox.multiverseinventories.group;

import java.util.List;

/**
 * Manager class for manipulating the groups of this plugin that are contained in memory.
 */
public interface WorldGroupManager {

    /**
     * Retrieves the WorldGroup associated with the given name.  Casing is ignored.
     *
     * @param groupName Name of WorldGroup to retrieve.
     * @return The world group by the name given or null if one doesn't exist by that name.
     */
    WorldGroup getGroup(String groupName);

    /**
     * Retrieves all of the World Groups associated with the given world.  Casing is ignored.
     *
     * @param worldName Name of the world to get groups for.
     * @return List of World Groups associated with the world or null if none.
     */
    List<WorldGroup> getWorldGroups(String worldName);

    /**
     * Sets up the World Groups in memory.
     *
     * @param worldGroups List of World Groups to store in memory.
     */
    void setWorldGroups(List<WorldGroup> worldGroups);

    /**
     * Adds a World Group to the collection in memory.
     *
     * @param worldGroup World group to add.
     */
    void addWorldGroup(WorldGroup worldGroup);

    /**
     * Removes a World Group from the collection in memory AND from the config.
     *
     * @param worldGroup World group to remove.
     */
    void removeWorldGroup(WorldGroup worldGroup);
}
