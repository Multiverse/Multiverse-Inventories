package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.share.Shares;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of GroupingConflict.
 */
public class SimpleGroupingConflict implements GroupingConflict {

    private WorldGroup groupOne;
    private WorldGroup groupTwo;
    private Shares conflictingShares;

    public SimpleGroupingConflict(WorldGroup groupOne, WorldGroup groupTwo, Shares conflictingShares) {
        this.groupOne = groupOne;
        this.groupTwo = groupTwo;
        this.conflictingShares = conflictingShares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup getFirstGroup() {
        return this.groupOne;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup getSecondGroup() {
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
