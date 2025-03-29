package org.mvplugins.multiverse.inventories.event;

import org.mvplugins.multiverse.inventories.handleshare.AffectedProfiles;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class GameModeChangeShareHandlingEvent extends ShareHandlingEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of HANDLERS.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final GameMode fromGameMode;
    private final GameMode toGameMode;

    public GameModeChangeShareHandlingEvent(Player player, AffectedProfiles affectedProfiles,
                                            GameMode fromGameMode, GameMode toGameMode) {
        super(player, affectedProfiles);
        this.fromGameMode = fromGameMode;
        this.toGameMode = toGameMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
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

}
