package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.profile.container.GroupProfileContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class to hold information about any conflicts between world groups.
 */
public final class GroupingConflict {

    private GroupProfileContainer groupOne;
    private GroupProfileContainer groupTwo;
    private Shares conflictingShares;

    public GroupingConflict(GroupProfileContainer groupOne, GroupProfileContainer groupTwo, Shares conflictingShares) {
        this.groupOne = groupOne;
        this.groupTwo = groupTwo;
        this.conflictingShares = conflictingShares;
    }

    /**
     * @return The first group in the conflict.
     */
    public GroupProfileContainer getFirstGroup() {
        return this.groupOne;
    }

    /**
     * @return The second group in the conflict.
     */
    public GroupProfileContainer getSecondGroup() {
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

