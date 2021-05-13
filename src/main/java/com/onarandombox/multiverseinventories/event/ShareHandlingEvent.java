package com.onarandombox.multiverseinventories.event;

import com.onarandombox.multiverseinventories.ShareHandler;
import com.onarandombox.multiverseinventories.share.PersistingProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Called when a player has changed from one world to another. Cancellable.
 */
public abstract class ShareHandlingEvent extends Event implements Cancellable {

    private boolean cancelled;

    private final Player player;
    private final PersistingProfile alwaysWriteProfile;
    private final List<PersistingProfile> writeProfiles;
    private final List<PersistingProfile> readProfiles;

    ShareHandlingEvent(Player player, ShareHandler.AffectedProfiles affectedProfiles) {
        this.player = player;
        this.alwaysWriteProfile = affectedProfiles.getAlwaysWriteProfile();
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
     * Returns the profile that will always be saved to. By default, this is a profile for the world the player was in.
     *
     * @return The profile that will always be saved to when this event occurs.
     */
    public PersistingProfile getAlwaysWriteProfile() {
        return alwaysWriteProfile;
    }

    /**
     * @return The profiles for the world/groups the player is coming from that data will be saved to in addition to
     * the profile returned by {@link #getAlwaysWriteProfile()}.
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