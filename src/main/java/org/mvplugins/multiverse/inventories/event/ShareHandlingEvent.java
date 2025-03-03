package org.mvplugins.multiverse.inventories.event;

import org.mvplugins.multiverse.inventories.handleshare.AffectedProfiles;
import org.mvplugins.multiverse.inventories.handleshare.PersistingProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.List;

/**
 * Called when a player has changed from one world to another. Cancellable.
 */
public abstract class ShareHandlingEvent extends Event implements Cancellable {

    private boolean cancelled;

    private final Player player;
    private final List<PersistingProfile> writeProfiles;
    private final List<PersistingProfile> readProfiles;

    ShareHandlingEvent(Player player, AffectedProfiles affectedProfiles) {
        this.player = player;
        this.writeProfiles = affectedProfiles.getWriteProfiles();
        this.readProfiles = affectedProfiles.getReadProfiles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * @return The profiles for the world/groups the player is coming from that data will be saved to.
     */
    public List<PersistingProfile> getWriteProfiles() {
        return this.writeProfiles;
    }

    /**
     * @return The profiles for the world/groups the player is going to that data will be loaded from.
     */
    public List<PersistingProfile> getReadProfiles() {
        return this.readProfiles;
    }

    /**
     * @return The player involved in this sharing transaction.
     */
    public Player getPlayer() {
        return this.player;
    }
}