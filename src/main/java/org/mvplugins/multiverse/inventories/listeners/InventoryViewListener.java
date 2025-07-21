package org.mvplugins.multiverse.inventories.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.InventoryDataProvider;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.view.InventoryGUIHelper;
import org.mvplugins.multiverse.inventories.view.ModifiableInventoryHolder;
import org.mvplugins.multiverse.inventories.view.ReadOnlyInventoryHolder;

import java.util.Arrays;

@Service
final class InventoryViewListener implements MVInvListener {

    private final MultiverseInventories inventories;
    private final InventoryDataProvider inventoryDataProvider;
    private final InventoryGUIHelper inventoryGUIHelper;

    @Inject
    InventoryViewListener(
            @NotNull MultiverseInventories inventories,
            @NotNull InventoryDataProvider inventoryDataProvider,
            @NotNull InventoryGUIHelper inventoryGUIHelper
    ) {
        this.inventories = inventories;
        this.inventoryDataProvider = inventoryDataProvider;
        this.inventoryGUIHelper = inventoryGUIHelper;
    }

    // This listener will cancel any clicks or drags in inventories that have the ReadOnlyInventoryHolder marker.
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    void onInventoryClick(InventoryClickEvent event) {
        // If it's a read-only inventory, cancel all clicks.
        if (event.getInventory().getHolder() instanceof ReadOnlyInventoryHolder ||
                (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof ReadOnlyInventoryHolder)) {
            event.setCancelled(true);
            return;
        }

        // If it's a modifiable inventory, apply specific restrictions for armor/off-hand slots.
        if (event.getInventory().getHolder() instanceof ModifiableInventoryHolder) {
            int clickedSlot = event.getRawSlot();
            ItemStack cursorItem = event.getCursor(); // Item held by the cursor
            ItemStack currentItem = event.getCurrentItem(); // Item in the clicked slot
            Player player = (Player) event.getWhoClicked(); // The player who clicked

            // Define the special slots
            boolean isSpecialSlot =  (clickedSlot >= 36 && clickedSlot <= 40); // Armor (36-39) and Off-hand (40)

            // Determine if the slot is one of the padding slots (41-44)
            boolean isPaddingSlot =  (clickedSlot >= 41 && clickedSlot <= 44);

            if (isPaddingSlot) {
                // Clicks on padding slots are always cancelled and do nothing else.
                event.setCancelled(true);
                return;
            }

            // --- Logic for special slots (armor/off-hand) ---
            if (isSpecialSlot) {
                boolean currentItemIsFiller = currentItem != null && inventoryGUIHelper.isFillerItem(currentItem);

                // Scenario 1: Player tries to pick up a filler item (cursor is empty)
                if (currentItemIsFiller && (cursorItem == null || cursorItem.getType() == Material.AIR)) {
                    event.setCancelled(true);
                    return; // Prevent pickup, filler stays in place
                }

                // Scenario 2: Player tries to place an invalid item into a special slot
                if (cursorItem != null && cursorItem.getType() != Material.AIR && !inventoryGUIHelper.isValidItemForSlot(cursorItem, clickedSlot)) {
                    event.setCancelled(true);
                    return; // Prevent invalid placement
                }

                // Scenario 3: Player places a valid item into a special slot that currently holds a filler or a valid item.
                // Manually handle the swap to ensure fillers don't leave the GUI.
                if (cursorItem != null && cursorItem.getType() != Material.AIR && inventoryGUIHelper.isValidItemForSlot(cursorItem, clickedSlot)) {
                    event.setCancelled(true); // Take full control of the event

                    // Place the new item from the cursor into the clicked slot
                    event.getInventory().setItem(clickedSlot, cursorItem);

                    // If the player tries to replace an item in the filler slot, check if the item is valid.
                    if (currentItem != null && currentItem.getType() != Material.AIR && !inventoryGUIHelper.isFillerItem(currentItem)) {
                        // If the original item was a real, non-filler item, put it on the player's cursor.
                        player.setItemOnCursor(currentItem);
                    } else {
                        // If the original item was null, air, or a filler, clear the player's cursor.
                        player.setItemOnCursor(null);
                    }

                    player.updateInventory(); // Update client to reflect changes
                    return; // Event handled
                }

                // Scenario 4: Player is shift-clicking a valid item from a special slot.
                // Or picking up a valid item from a special slot.
                // We need to ensure filler reappears if the slot becomes empty.
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                        event.getAction() == InventoryAction.PICKUP_ALL ||
                        event.getAction() == InventoryAction.PICKUP_HALF ||
                        event.getAction() == InventoryAction.PICKUP_ONE ||
                        event.getAction() == InventoryAction.PICKUP_SOME) {
                    Bukkit.getScheduler().runTaskLater(inventories, () -> {
                        // After Bukkit processes the click, if the slot is now empty, put the filler back.
                        if (event.getInventory().getItem(clickedSlot) == null || event.getInventory().getItem(clickedSlot).getType() == Material.AIR) {
                            event.getInventory().setItem(clickedSlot, inventoryGUIHelper.createFillerItemForSlot(clickedSlot, true));
                        }
                    }, 1L);
                }
            }
        }
    }

    // Also cancel drag events to prevent items from being dragged into/out of the inventory
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    void onInventoryDrag(InventoryDragEvent event) {
        // If it is a read-only inventory, cancel all drags
        if (event.getInventory().getHolder() instanceof ReadOnlyInventoryHolder) {
            event.setCancelled(true);
        }

        // If it's a modifiable inventory, apply specific restrictions for armor/off-hand slots.
        if (event.getInventory().getHolder() instanceof ModifiableInventoryHolder) {
            ItemStack draggedItem = event.getCursor(); // The item being dragged (after the drag operation)

            // If nothing is being dragged, or dragging air, no restriction needed.
            if (draggedItem == null || draggedItem.getType() == Material.AIR) {
                return;
            }
            for (int slot : event.getRawSlots()) {
                // Define the special slots
                boolean isSpecialSlot = (slot >= 36 && slot <= 40);

                // Determine if the slot is one of the padding slots (41-44)
                boolean isPaddingSlot = (slot >= 41 && slot <= 44);

                if (isPaddingSlot) {
                    // Clicks on padding slots are always cancelled and do nothing else.
                    event.setCancelled(true);
                    return;
                }

                if (isSpecialSlot) {
                    // Check if the dragged item is valid for this special slot
                    if (!inventoryGUIHelper.isValidItemForSlot(draggedItem, slot)) { // Use helper
                        event.setCancelled(true);
                        return; // Cancel the entire drag event
                    }
                }
            }

            // After a drag, check if any special slots became empty and replace with filler
            Bukkit.getScheduler().runTaskLater(inventories, () -> {
                for (int slot : event.getRawSlots()) {
                    if ((slot >= 36 && slot <= 40) && (event.getInventory().getItem(slot) == null || event.getInventory().getItem(slot).getType() == Material.AIR)) {
                        event.getInventory().setItem(slot, inventoryGUIHelper.createFillerItemForSlot(slot, true)); // Use helper
                    }
                }
            }, 1L); // Run one tick later
        }
    }

    // Event handler for InventoryCloseEvent to save changes
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    void onInventoryClose(InventoryCloseEvent event) {
        // Check if the closed inventory has the custom ModifiableInventoryHolder class
        if (event.getInventory().getHolder() instanceof ModifiableInventoryHolder holder) {
            final OfflinePlayer targetPlayer = holder.getTargetPlayer();
            final String worldName = holder.getWorldName();
            final ProfileType profileType = holder.getProfileType();
            final Inventory closedInventory = event.getInventory();

            // Extract contents: 0-35 for inventory, 36-39 for armor, 40 for off-hand
            ItemStack[] newContents = Arrays.copyOfRange(closedInventory.getContents(), 0, 36);
            ItemStack[] newArmor = new ItemStack[4];
            // Map GUI armor slots back to Multiverse-Inventories' expected order [helmet, chestplate, leggings, boots]
            newArmor[3] = closedInventory.getItem(36); // Helmet
            newArmor[2] = closedInventory.getItem(37); // Chestplate
            newArmor[1] = closedInventory.getItem(38); // Leggings
            newArmor[0] = closedInventory.getItem(39); // Boots
            ItemStack newOffHand = closedInventory.getItem(40);

            // Before saving, ensure any filler items are removed from the actual data
            for (int i = 0; i < newArmor.length; i++) {
                if (newArmor[i] != null && inventoryGUIHelper.isFillerItem(newArmor[i])) {
                    newArmor[i] = null; // Replace filler with null for saving
                }
            }
            if (newOffHand != null && inventoryGUIHelper.isFillerItem(newOffHand)) {
                newOffHand = null; // Replace filler with null for saving
            }

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
                Logging.severe("Error during inventory save process for " + targetPlayer.getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
        }
    }
}