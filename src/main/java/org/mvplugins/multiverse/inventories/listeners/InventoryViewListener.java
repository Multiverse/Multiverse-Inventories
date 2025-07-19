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
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.share.Sharables;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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

    // This holder stores context needed to save the inventory when it's closed.
    public static class ModifiableInventoryHolder implements InventoryHolder {
        private final OfflinePlayer targetPlayer;
        private final String worldName;
        private final ProfileType profileType;
        private final MultiverseInventories inventories;

        public ModifiableInventoryHolder(@NotNull OfflinePlayer targetPlayer,
                                         @NotNull String worldName,
                                         @NotNull ProfileType profileType,
                                         @NotNull MultiverseInventories inventories) {
            this.targetPlayer = targetPlayer;
            this.worldName = worldName;
            this.profileType = profileType;
            this.inventories = inventories;
        }

        public @NotNull OfflinePlayer getTargetPlayer() {
            return targetPlayer;
        }

        public @NotNull String getWorldName() {
            return worldName;
        }

        public @NotNull ProfileType getProfileType() {
            return profileType;
        }

        public @NotNull MultiverseInventories getInventories() {
            return inventories;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return null; // The actual inventory is passed via the event
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

            // Save the updated inventory, armor, and off-hand contents asynchronously
            CompletableFuture<Void> saveFuture = CompletableFuture.allOf(
                    SingleShareWriter.of(plugin, targetPlayer, worldName, profileType, Sharables.INVENTORY)
                            .write(newContents, true) // true to update if player is online
                            .thenRun(() -> plugin.getLogger().fine("Saved inventory for " + targetPlayer.getName() + " in " + worldName)),
                    SingleShareWriter.of(plugin, targetPlayer, worldName, profileType, Sharables.ARMOR)
                            .write(newArmor, true)
                            .thenRun(() -> plugin.getLogger().fine("Saved armor for " + targetPlayer.getName() + " in " + worldName)),
                    SingleShareWriter.of(plugin, targetPlayer, worldName, profileType, Sharables.OFF_HAND)
                            .write(newOffHand, true)
                            .thenRun(() -> plugin.getLogger().fine("Saved off-hand for " + targetPlayer.getName() + " in " + worldName))
            );

            saveFuture.thenRun(() -> {
                plugin.getLogger().info("Inventory for player " + targetPlayer.getName() + " in world " + worldName + " has been modified and saved.");
            }).exceptionally(throwable -> {
                plugin.getLogger().severe("Failed to save inventory for " + targetPlayer.getName() + " in world " + worldName + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });

            // If the target player is online, update their live inventory
            if (targetPlayer.isOnline()) {
                Player onlinePlayer = targetPlayer.getPlayer();
                if (onlinePlayer != null) {
                    // Check if the online player is in the world whose inventory was modified
                    // This is important to avoid overwriting their current inventory if they are in a different world
                    if (onlinePlayer.getWorld().getName().equalsIgnoreCase(worldName)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            onlinePlayer.getInventory().setContents(newContents);
                            onlinePlayer.getInventory().setArmorContents(newArmor);
                            onlinePlayer.getInventory().setItemInOffHand(newOffHand);
                            onlinePlayer.updateInventory(); // Ensure client sees changes
                            plugin.getLogger().info("Updated live inventory for online player " + onlinePlayer.getName() + " in world " + worldName);
                        });
                    } else {
                        plugin.getLogger().info("Player " + onlinePlayer.getName() + " is online but in a different world (" + onlinePlayer.getWorld().getName() + "), not updating live inventory.");
                    }
                }
            }
        }
    }
}