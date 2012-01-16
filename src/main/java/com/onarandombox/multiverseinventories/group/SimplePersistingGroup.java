package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.share.Sharable;

/**
 * Simple implementation of PersistingGroup.
 */
public class SimplePersistingGroup implements PersistingGroup {

    private Sharable sharable;
    private WorldGroup group;

    public SimplePersistingGroup(Sharable sharable, WorldGroup group) {
        this.sharable = sharable;
        this.group = group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sharable getSharable() {
        return this.sharable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup getGroup() {
        return this.group;
    }
}
