package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the status of an inventory data load operation.
 * Provides a fixed set of states for clarity and type safety.
 */
public enum InventoryStatus {
    /**
     * Indicates that live inventory data from an online player was displayed.
     */
    LIVE_INVENTORY(ChatColor.GREEN + "Displaying LIVE inventory"),

    /**
     * Indicates that stored inventory data from Multiverse-Inventories profiles was displayed.
     */
    STORED_INVENTORY(ChatColor.GREEN + "Displaying STORED inventory"),

    /**
     * Indicates that no player data was found for the specified world/player.
     */
    NO_DATA_FOUND(ChatColor.RED + "No player data found");

    private final String message;

    InventoryStatus(@NotNull String message) {
        this.message = message;
    }

    /**
     * Gets the full status message including player and world context.
     *
     * @param playerName The name of the target player.
     * @param worldName  The name of the target world.
     * @return The formatted status message.
     */
    public @NotNull String getFormattedMessage(@NotNull String playerName, @NotNull String worldName) {
        if (this == NO_DATA_FOUND) {
            return this.message + " for " + playerName + " in world" + worldName + ". Try checking a different world or ensure the player has played in this world.";
        }
        return this.message + " for " + playerName + " in world " + worldName;
    }
}
