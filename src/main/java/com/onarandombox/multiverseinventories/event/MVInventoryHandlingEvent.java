package com.onarandombox.multiverseinventories.event;

import com.onarandombox.multiverseinventories.api.share.PersistingProfile;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

/**
 * Called when a player has changed from one world to another. Cancellable.
 */
public class MVInventoryHandlingEvent extends Event implements Cancellable {

    /**
     * The cause of this Handling event.
     */
    public static enum Cause {
        /**
         * Event caused by player changing worlds.
         */
        WORLD_CHANGE,
        /**
         * Event caused by player changing game modes.
         */
        GAME_MODE_CHANGE
    }

    private boolean cancelled;

    private final Player player;
    private final Cause cause;
    private final String fromWorld;
    private final String toWorld;
    private final GameMode fromGameMode;
    private final GameMode toGameMode;
    private final List<PersistingProfile> fromProfiles;
    private final List<PersistingProfile> toProfiles;

    public MVInventoryHandlingEvent(Player player, Cause cause, String fromWorld, String toWorld,
                                    GameMode fromGameMode, GameMode toGameMode) {
        this.fromProfiles = new ArrayList<PersistingProfile>();
        this.toProfiles = new ArrayList<PersistingProfile>();
        this.player = player;
        this.cause = cause;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.fromGameMode = fromGameMode;
        this.toGameMode = toGameMode;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of HANDLERS.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
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
     * @return The profiles for the world/groups the player is coming from.
     */
    public List<PersistingProfile> getFromProfiles() {
        return this.fromProfiles;
    }

    /**
     * @return The profiles for the world/groups the player is going to.
     */
    public List<PersistingProfile> getToProfiles() {
        return this.toProfiles;
    }

    /**
     * @return The world travelling from.
     */
    public String getFromWorld() {
        return this.fromWorld;
    }

    /**
     * @return The world travelling to.
     */
    public String getToWorld() {
        return this.toWorld;
    }

    /**
     * @return The GameMode player is changing from.
     */
    public GameMode getFromGameMode() {
        return fromGameMode;
    }

    /**
     * @return The GameMode player is changing to.
     */
    public GameMode getToGameMode() {
        return toGameMode;
    }

    /**
     * @return The player involved in this sharing transaction.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The cause of this event.
     */
    public Cause getCause() {
        return cause;
    }
}