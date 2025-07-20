package org.mvplugins.multiverse.inventories.listeners;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareWriter;
import org.mvplugins.multiverse.inventories.listeners.MVInvListener;
import org.mvplugins.multiverse.inventories.profile.InventoryDataProvider;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.view.ModifiableInventoryHolder;
import org.mvplugins.multiverse.inventories.view.ReadOnlyInventoryHolder;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
final class InventoryViewListener implements MVInvListener {

    private final MultiverseInventories inventories;
    private final InventoryDataProvider inventoryDataProvider;

    @Inject
    InventoryViewListener(
            @NotNull MultiverseInventories inventories,
            @NotNull InventoryDataProvider inventoryDataProvider
    ) {
        this.inventories = inventories;
        this.inventoryDataProvider = inventoryDataProvider;
    }
    // This listener will cancel any clicks or drags in inventories that have the ReadOnlyInventoryHolder marker.
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory being clicked has the custom holder
        if (event.getInventory().getHolder() instanceof ReadOnlyInventoryHolder) {
            event.setCancelled(true);
        }
        // If the clicked inventory is read-only, cancel the event (e.g., shift-click into it)
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

    // Event handler for InventoryCloseEvent to save changes
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        // Check if the closed inventory has the custom ModifiableInventoryHolder class
        if (event.getInventory().getHolder() instanceof ModifiableInventoryHolder holder) {
            final OfflinePlayer targetPlayer = holder.getTargetPlayer();
            final String worldName = holder.getWorldName();
            final ProfileType profileType = holder.getProfileType();
            final MultiverseInventories plugin = holder.getInventories();
            final Inventory closedInventory = event.getInventory();

            // Extract contents: 0-35 for inventory, 36-39 for armor, 40 for off-hand
            ItemStack[] newContents = Arrays.copyOfRange(closedInventory.getContents(), 0, 36);
            ItemStack[] newArmor = new ItemStack[4];
            // Map GUI armor slots back to Multiverse-Inventories' expected order [helmet, chestplate, leggings, boots]
            newArmor[0] = closedInventory.getItem(39); // Helmet
            newArmor[1] = closedInventory.getItem(38); // Chestplate
            newArmor[2] = closedInventory.getItem(37); // Leggings
            newArmor[3] = closedInventory.getItem(36); // Boots
            ItemStack newOffHand = closedInventory.getItem(40);

            // Delegate saving to InventoryDataProvider
            inventoryDataProvider.savePlayerInventoryData(
                    targetPlayer,
                    worldName,
                    profileType,
                    newContents,
                    newArmor,
                    newOffHand
            ).exceptionally(throwable -> {
                // Error logging is now handled within InventoryDataProvider, but we can add a general one here too
                inventories.getLogger().severe("Error during inventory save process for " + targetPlayer.getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
            }
        }
    }