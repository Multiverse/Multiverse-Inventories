package org.mvplugins.multiverse.inventories.event;

import org.mvplugins.multiverse.inventories.handleshare.AffectedProfiles;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class WorldChangeShareHandlingEvent extends ShareHandlingEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of HANDLERS.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final String fromWorld;
    private final String toWorld;

    public WorldChangeShareHandlingEvent(
            Player player,
            AffectedProfiles affectedProfiles,
            String fromWorld,
            String toWorld) {
        super(player, affectedProfiles);
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
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
}
