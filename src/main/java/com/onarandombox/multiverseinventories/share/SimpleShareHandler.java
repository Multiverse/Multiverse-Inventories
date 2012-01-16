package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.group.PersistingGroup;
import com.onarandombox.multiverseinventories.group.SimplePersistingGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of ShareHandler.
 */
public class SimpleShareHandler implements ShareHandler {

    private List<PersistingGroup> fromGroups;
    private List<PersistingGroup> toGroups;

    public SimpleShareHandler() {
        this.fromGroups = new ArrayList<PersistingGroup>();
        this.toGroups = new ArrayList<PersistingGroup>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFromGroup(Sharable sharable, WorldGroup group) {
        this.getFromGroups().add(new SimplePersistingGroup(sharable, group));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToGroup(Sharable sharable, WorldGroup group) {
        this.getToGroups().add(new SimplePersistingGroup(sharable, group));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersistingGroup> getFromGroups() {
        return this.fromGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersistingGroup> getToGroups() {
        return this.toGroups;
    }
}
