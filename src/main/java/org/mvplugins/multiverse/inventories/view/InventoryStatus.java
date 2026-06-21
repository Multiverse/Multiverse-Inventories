package org.mvplugins.multiverse.inventories.view;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

/**
 * Represents the status of an inventory data load operation.
 * Provides a fixed set of states for clarity and type safety.
 *
 * @since 5.2
 */
//TODO: Make this enum implement LocalizableMessage
@ApiStatus.Experimental
@ApiStatus.AvailableSince("5.2")
public enum InventoryStatus {
    /**
     * Indicates that live inventory data from an online player was displayed.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    LIVE_INVENTORY(
            MVInvi18n.INVENTORY_LIVEINVENTORY,
            "&aDisplaying LIVE inventory for {player} in world {world}"),

    /**
     * Indicates that stored inventory data from Multiverse-Inventories profiles was displayed.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    STORED_INVENTORY(
            MVInvi18n.INVENTORY_STOREDINVENTORY,
            "&aDisplaying STORED inventory for {player} in world {world}"),

    /**
     * Indicates that no player data was found for the specified world/player.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    NO_DATA_FOUND(
            MVInvi18n.INVENTORY_NODATAFOUND,
            "&cNo player data found for {player} in world {world}. "
                    + "Try checking a different world or ensure the player has played in this world.");

    private final MVInvi18n messageKey;
    private final String nonLocalizedMessage;

    InventoryStatus(@NotNull MVInvi18n messageKey, @NotNull String nonLocalizedMessage) {
        this.messageKey = messageKey;
        this.nonLocalizedMessage = nonLocalizedMessage;
    }

    /**
     * Gets the full status message including player and world context.
     *
     * @param playerName The name of the target player.
     * @param worldName  The name of the target world.
     * @return The formatted status message.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public @NotNull String getFormattedMessage(@NotNull String playerName, @NotNull String worldName) {
        return messageKey.bundle(nonLocalizedMessage, getReplacements(playerName, worldName)).formatted();
    }

    private MessageReplacement[] getReplacements(@NotNull String playerName, @NotNull String worldName) {
        return new MessageReplacement[]{
                replace("{player}").with(playerName),
                replace("{world}").with(worldName)
        };
    }
}
