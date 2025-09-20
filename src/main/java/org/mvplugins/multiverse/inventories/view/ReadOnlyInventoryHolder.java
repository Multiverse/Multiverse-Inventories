package org.mvplugins.multiverse.inventories.view; // New package

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A custom InventoryHolder that serves as a marker for read-only inventories.
 * Inventories created with this holder will have their interactions cancelled by the InventoryViewListener.
 *
 * @since 5.2
 */
@ApiStatus.AvailableSince("5.2")
public final class ReadOnlyInventoryHolder implements InventoryHolder {
    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Inventory getInventory() {
        // This method is required by the interface but is not directly used for the marker purpose.
        // Throwing UnsupportedOperationException clearly indicates it's not meant to be called.
        throw new UnsupportedOperationException("ReadOnlyInventoryHolder does not provide an Inventory directly.");
    }
}
