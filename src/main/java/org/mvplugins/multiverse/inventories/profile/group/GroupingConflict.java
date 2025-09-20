package org.mvplugins.multiverse.inventories.profile.group;

import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class to hold information about any conflicts between world groups.
 */
public final class GroupingConflict {

    private final WorldGroup groupOne;
    private final WorldGroup groupTwo;
    private final Shares conflictingShares;

    /**
     * @deprecated This shouldn't have been public. Please never be instantiated directly.
     *             Instead, you should get the result from {@link WorldGroupManager#checkForConflicts()}.
     */
    @Deprecated
    public GroupingConflict(WorldGroup groupOne, WorldGroup groupTwo, Shares conflictingShares) {
        this.groupOne = groupOne;
        this.groupTwo = groupTwo;
        this.conflictingShares = conflictingShares;
    }

    /**
     * @return The first group in the conflict.
     */
    public WorldGroup getFirstGroup() {
        return this.groupOne;
    }

    /**
     * @return The second group in the conflict.
     */
    public WorldGroup getSecondGroup() {
        return this.groupTwo;
    }

    /**
     * @return The shares that are causing a conflict.
     */
    public Shares getConflictingShares() {
        return this.conflictingShares;
    }

    /**
     * @return The worlds the two groups share.
     */
    public List<String> getConflictingWorlds() {
        List<String> worlds = new ArrayList<String>();
        for (String world : this.getFirstGroup().getApplicableWorlds()) {
            if (this.getSecondGroup().getApplicableWorlds().contains(world)) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    /**
     * @return The worlds the two groups share as a single string.
     */
    public String getWorldsString() {
        return String.join(", ", this.getConflictingWorlds());
    }
}
