package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.group.PersistingGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;

import java.util.List;

/**
 * An interface for the handling of sharing.
 */
public interface ShareHandler {

    /**
     * @param sharable What from this group needs to be saved.
     * @param group A group the player came from that will need inventories saved to.
     */
    void addFromGroup(Sharable sharable, WorldGroup group);

    /**
     * @param sharable What from this group needs to be loaded.
     * @param group A group the player is going to that will need effects loaded from.
     */
    void addToGroup(Sharable sharable, WorldGroup group);

    /**
     * @return The groups the player is coming from.
     */
    List<PersistingGroup> getFromGroups();

    /**
     * @return The groups the player is going to.
     */
    List<PersistingGroup> getToGroups();
}
