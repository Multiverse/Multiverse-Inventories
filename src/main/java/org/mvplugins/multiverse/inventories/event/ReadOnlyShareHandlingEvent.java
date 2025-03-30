package org.mvplugins.multiverse.inventories.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.handleshare.AffectedProfiles;

public final class ReadOnlyShareHandlingEvent extends ShareHandlingEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of HANDLERS.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ReadOnlyShareHandlingEvent(Player player, AffectedProfiles affectedProfiles) {
        super(player, affectedProfiles);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
