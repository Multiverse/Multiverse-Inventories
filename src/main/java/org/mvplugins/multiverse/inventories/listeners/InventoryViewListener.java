package org.mvplugins.multiverse.inventories.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;

@Service
public final class InventoryViewListener implements MVInvListener {

    private final MultiverseInventories inventories;

    @Inject
    InventoryViewListener(
            @NotNull MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    // This class acts as a marker. When an inventory is created with this holder,
    // the event listener can identify it as a read-only inventory.
    public static class ReadOnlyInventoryHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            // This method is required by the interface but isn't strictly used for our marker purpose.
            // The actual inventory is obtained from the InventoryClickEvent itself.
            return null;
        }
    }
    // This listener will cancel any clicks or drags in inventories that have the ReadOnlyInventoryHolder marker.
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory being clicked has the custom holder
        if (event.getInventory().getHolder() instanceof ReadOnlyInventoryHolder) {
            event.setCancelled(true);
        }
        // This covers cases where a player might click in their own inventory
        // but the action is intended to move an item into the read-only inventory (e.g., shift-click).
        else if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof ReadOnlyInventoryHolder) {
            event.setCancelled(true);
        }
    }

    // Also cancel drag events to prevent items from being dragged into/out of the inventory
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ReadOnlyInventoryHolder) {
            event.setCancelled(true);
        }
    }
}

