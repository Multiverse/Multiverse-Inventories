package com.onarandombox.multiverseinventories.api;

import com.onarandombox.multiverseinventories.profile.GroupingConflict;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * Manager class for manipulating the groups of this plugin that are contained in the groups configuration.
 */
public interface GroupManager {

    /**
     * Retrieves the world group associated with the given name.
     * <p/>
     * These groups represent the groups that define a set of worlds and what they share.
     *
     * @param groupName Name of world group to retrieve. Casing is ignored.
     * @return The world group by the name given or null if one doesn't exist by that name.
     */
    WorldGroupProfile getGroup(String groupName);

    /**
     * Returns a list of all the world groups defined in Multiverse-Inventories's groups configuration.
     * <p/>
     * This list is unmodifiable.
     *
     * @return An unmodifiable list of all world groups.
     */
    List<WorldGroupProfile> getGroups();

    /**
     * Retrieves all of the world groups associated with the given world.
     *
     * @param worldName Name of the world to get groups for.
     * @return List of World Groups associated with the world or null if none.
     */
    List<WorldGroupProfile> getGroupsForWorld(String worldName);

    /**
     * Check if the given world has any configured groups.
     * 
     * @param worldName Name of the world to check.
     * @return true if this world has one or more groups.
     */
    boolean hasGroup(String worldName);

    /**
     * Sets up the World Groups in memory.
     *
     * @param worldGroups List of World Groups to store in memory.
     * @deprecated This feature is now completely unused.
     */
    @Deprecated
    void setGroups(List<WorldGroupProfile> worldGroups);

    /**
     * Adds a World Group to the collection in memory, also writing it to the groups configuration.
     *
     * @param worldGroup World group to add.  Casing is ignored.
     * @param persist    This parameter is unused due to deprecation of the method.
     * @deprecated
     */
    @Deprecated
    void addGroup(WorldGroupProfile worldGroup, boolean persist);

    /**
     * Adds or updates a world group in Multiverse-Inventories.
     * <p/>
     * This will update an existing group by persisting changes made to it in the groups configuration.
     * This should be called when any of the facets of a group such as worlds or shares have been modified.
     * <p/>
     * If the group does not exist it will be added to the groups configuration.
     * <p/>
     * If worldGroup's name matches the name of a different WorldGroupProfile object that is already
     * known, the previous object will be overwritten with worldGroup parameter.
     *
     * @param worldGroup the world group to add.
     */
    void updateGroup(WorldGroupProfile worldGroup);

    /**
     * Removes a world group from the collection in memory AND from the groups configuration.
     *
     * @param worldGroup the world group to remove.
     * @return true if group was removed.
     */
    boolean removeGroup(WorldGroupProfile worldGroup);

    /**
     * Creates a new empty world group.
     * <p/>
     * Please note if you do not add worlds to this group it will not persist very well.
     * This does not automatically persist the new group.  It must bed added via
     * {@link #updateGroup(com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile)}
     *
     * @param name A name for the new group.
     * @return The newly created world group.
     */
    WorldGroupProfile newEmptyGroup(String name);

    /**
     * Creates a new world group filled with the data provided in dataMap.
     *
     * @param name    A name for the new group.
     * @param dataMap A map of the data that pertains to this world group.
     * @return The newly created WorldGroupProfile.
     * @throws DeserializationException If the dataMap is not formatted correctly.
     * @deprecated This method is no longer appropriate as part of the api.  It has no realistic outside function.
     */
    @Deprecated
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

