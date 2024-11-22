package org.mvplugins.multiverse.inventories.profile;

import org.mvplugins.multiverse.inventories.WorldGroup;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Manager class for manipulating the groups of this plugin that are contained in the groups configuration.
 */
public interface WorldGroupManager {

    /**
     * <p>Retrieves the world group associated with the given name.</p>
     *
     * These groups represent the groups that define a set of worlds and what they share.
     *
     * @param groupName Name of world group to retrieve. Casing is ignored.
     * @return The world group by the name given or null if one doesn't exist by that name.
     */
    WorldGroup getGroup(String groupName);

    /**
     * <p>Returns a list of all the world groups defined in Multiverse-Inventories's groups configuration.</p>
     *
     * This list is unmodifiable.
     *
     * @return An unmodifiable list of all world groups.
     */
    List<WorldGroup> getGroups();

    /**
     * Retrieves all of the world groups associated with the given world.
     *
     * @param worldName Name of the world to get groups for.
     * @return List of World Groups associated with the world or null if none.
     */
    List<WorldGroup> getGroupsForWorld(String worldName);

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
    void setGroups(List<WorldGroup> worldGroups);

    /**
     * Adds a World Group to the collection in memory, also writing it to the groups configuration.
     *
     * @param worldGroup World group to add. Casing is ignored.
     * @param persist    This parameter is unused due to deprecation of the method.
     * @deprecated
     */
    @Deprecated
    void addGroup(WorldGroup worldGroup, boolean persist);

    /**
     * <p>Adds or updates a world group in Multiverse-Inventories.</p>
     *
     * <p>This will update an existing group by persisting changes made to it in the groups configuration.
     * This should be called when any of the facets of a group such as worlds or shares have been modified.</p>
     *
     * <p>If the group does not exist it will be added to the groups configuration.</p>
     *
     * If worldGroup's name matches the name of a different WorldGroupProfileContainer object that is already
     * known, the previous object will be overwritten with worldGroup parameter.
     *
     * @param worldGroup the world group to add.
     */
    void updateGroup(WorldGroup worldGroup);

    /**
     * Removes a world group from the collection in memory AND from the groups configuration.
     *
     * @param worldGroup the world group to remove.
     * @return true if group was removed.
     */
    boolean removeGroup(WorldGroup worldGroup);

    /**
     * <p>Creates a new empty world group.</p>
     *
     * Please note if you do not add worlds to this group it will not persist very well.
     * This does not automatically persist the new group. It must bed added via {@link #updateGroup(WorldGroup)}.
     *
     * @param name A name for the new group.
     * @return The newly created world group.
     */
    WorldGroup newEmptyGroup(String name);

    /**
     * Creates a default world group including all of the loaded MV worlds sharing everything.
     */
    void createDefaultGroup();

    /**
     * @return The default world group which may be empty.
     */
    WorldGroup getDefaultGroup();

    /**
     * Checks all the world groups to see if there are any potential issues.
     *
     * @return A list of all the potential conflicts.
     */
    List<GroupingConflict> checkGroups();

    /**
     * Runs a check for conflicts between groups and displays them to console and sender if not null.
     *
     * @param sender The sender to relay information to. If null, info only displayed in console.
     */
    void checkForConflicts(CommandSender sender);
}

