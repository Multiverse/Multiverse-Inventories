package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.GroupingConflict;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Shares;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of GroupingConflict.
 */
final class DefaultGroupingConflict implements GroupingConflict {

    private WorldGroupProfile groupOne;
    private WorldGroupProfile groupTwo;
    private Shares conflictingShares;

    public DefaultGroupingConflict(WorldGroupProfile groupOne, WorldGroupProfile groupTwo, Shares conflictingShares) {
        this.groupOne = groupOne;
        this.groupTwo = groupTwo;
        this.conflictingShares = conflictingShares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroupProfile getFirstGroup() {
        return this.groupOne;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroupProfile getSecondGroup() {
        return this.groupTwo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getConflictingShares() {
        return this.conflictingShares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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

