package com.onarandombox.multiverseinventories.api;

import com.onarandombox.multiverseinventories.api.profile.GroupingConflict;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * Manager class for manipulating the groups of this plugin that are contained in memory.
 */
public interface GroupManager {

    /**
     * Retrieves the WorldGroupProfile associated with the given name.  Casing is ignored.
     *
     * @param groupName Name of WorldGroupProfile to retrieve.
     * @return The world group by the name given or null if one doesn't exist by that name.
     */
    WorldGroupProfile getGroup(String groupName);

    /**
     * @return A List of all world groups.
     */
    List<WorldGroupProfile> getGroups();

    /**
     * Retrieves all of the World Groups associated with the given world.  Casing is ignored.
     *
     * @param worldName Name of the world to get groups for.
     * @return List of World Groups associated with the world or null if none.
     */
    List<WorldGroupProfile> getGroupsForWorld(String worldName);

    /**
     * Sets up the World Groups in memory.
     *
     * @param worldGroups List of World Groups to store in memory.
     */
    void setGroups(List<WorldGroupProfile> worldGroups);

    /**
     * Adds a World Group to the collection in memory.
     *
     * @param worldGroup World group to add.
     * @param persist    True means this world group will be added to the Config file as well.
     */
    void addGroup(WorldGroupProfile worldGroup, boolean persist);

    /**
     * Removes a World Group from the collection in memory AND from the config.
     *
     * @param worldGroup World group to remove.
     */
    void removeGroup(WorldGroupProfile worldGroup);

    /**
     * Creates a new empty world group.  Please note if you do not add worlds to this group it will
     * not persist very well.  This does not automatically persist the new group.  It must bed added via
     * {@link #addGroup(com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile, boolean)}
     *
     * @param name A name for the new group.
     * @return The newly created WorldGroupProfile.
     */
    WorldGroupProfile newEmptyGroup(String name);

    /**
     * Creates a new world group filled with the data provided in dataMap.
     *
     * @param name    A name for the new group.
     * @param dataMap A map of the data that pertains to this world group.
     * @return The newly created WorldGroupProfile.
     * @throws DeserializationException If the dataMap is not formatted correctly.
     */
    WorldGroupProfile newGroupFromMap(String name, Map<String, Object> dataMap) throws DeserializationException;

    /**
     * Creates a default world group including all of the loaded MV worlds sharing everything.
     */
    void createDefaultGroup();

    /**
     * @return The default world group which may be empty.
     */
    WorldGroupProfile getDefaultGroup();

    /**
     * Checks all the world groups to see if there are any potential issues.
     *
     * @return A list of all the potential conflicts.
     */
    List<GroupingConflict> checkGroups();

    /**
     * Runs a check for conflicts between groups and displays them to console and sender if not null.
     *
     * @param sender The sender to relay information to.  If null, info only displayed in console.
     */
    void checkForConflicts(CommandSender sender);
}

