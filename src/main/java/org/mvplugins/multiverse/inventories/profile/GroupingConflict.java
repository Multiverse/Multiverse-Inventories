package org.mvplugins.multiverse.inventories.profile;

import org.mvplugins.multiverse.inventories.WorldGroup;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class to hold information about any conflicts between world groups.
 */
public final class GroupingConflict {

    private WorldGroup groupOne;
    private WorldGroup groupTwo;
    private Shares conflictingShares;

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
        for (String world : this.getFirstGroup().getWorlds()) {
            if (this.getSecondGroup().getWorlds().contains(world)) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    /**
     * @return The worlds the two groups share as a single string.
     */
    public String getWorldsString() {
        StringBuilder builder = new StringBuilder();
        for (String world : this.getConflictingWorlds()) {
            if (!builder.toString().isEmpty()) {
                builder.append(", ");
            }
            builder.append(world);
        }
        return builder.toString();
    }
}

