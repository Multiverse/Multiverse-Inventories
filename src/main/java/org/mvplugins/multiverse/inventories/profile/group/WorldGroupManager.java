package org.mvplugins.multiverse.inventories.profile.group;

import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.vavr.control.Try;

import java.util.List;

/**
 * Manager class for manipulating the groups of this plugin that are contained in the groups configuration.
 */
@Contract
public sealed interface WorldGroupManager permits AbstractWorldGroupManager {

    /**
     * <p>Loads the groups from storage.</p>
     */
    Try<Void> load();

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
    boolean hasConfiguredGroup(String worldName);

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
     * Checks for conflicts between groups and returns a result object containing the conflicts.
     *
     * @return A result object containing the conflicts found (if any).
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    GroupingConflictResult checkForConflicts();

    /**
     * Checks all the world groups to see if there are any potential issues.
     *
     * @return A list of all the potential conflicts.
     *
     * @deprecated Use {@link #checkForConflicts()} instead.
     */
    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    List<GroupingConflict> checkGroups();

    /**
     * Runs a check for conflicts between groups and displays them to issuer or console.
     *
     * @param issuer The issuer to relay information to. If null, info only displayed in console.
     *
     * @deprecated Use {@link #checkForConflicts()} instead.
     */
    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    void checkForConflicts(MVCommandIssuer issuer);

    /**
     * Recalculates the applicable shares for all groups removing disabled optional shares.
     * <br />
     * You should not need to call this method unless there is an edge case of the shares not recalculating automatically.
     */
    void recalculateApplicableShares();

    /**
     * Recalculates the applicable worlds for all groups. This will be automatically called when a world is added or removed,
     * and when the group is saved.
     * <br />
     * You should not need to call this method unless there is an edge case of the worlds not recalculating automatically.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    void recalculateApplicableWorlds();
}
