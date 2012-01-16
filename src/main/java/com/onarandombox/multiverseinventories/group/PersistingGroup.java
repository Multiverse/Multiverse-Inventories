package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.share.Sharable;

/**
 * Simple interface for groups that are going to be saved/loaded.
 */
public interface PersistingGroup {

    /**
     * @return The sharable that will be saved/loaded for.
     */
    Sharable getSharable();

    /**
     * @return The group that will be saved/loaded for.
     */
    WorldGroup getGroup();
}
