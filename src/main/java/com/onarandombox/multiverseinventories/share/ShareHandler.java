package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.group.PersistingGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import org.bukkit.World;
import org.bukkit.entity.Player;

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

    /**
     * @return The world travelling from.
     */
    World getFromWorld();

    /**
     * @return The world travelling to.
     */
    World getToWorld();

    /**
     * @return The player involved in this sharing transaction.
     */
    Player getPlayer();

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     *
     * @param shares The shares involved in the transaction.
     */
    void handleShares(Shares shares);
}
