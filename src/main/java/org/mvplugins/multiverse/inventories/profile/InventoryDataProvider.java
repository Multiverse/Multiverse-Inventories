package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareReader;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareWriter;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharables;

import java.util.concurrent.CompletableFuture; // Needed for async operations
import java.util.concurrent.CompletionException; // Needed for async error handling

/**
 * Provides methods for asynchronously loading player inventory data.
 * This class encapsulates the business logic for fetching inventory, armor, and off-hand contents.
 *
 * @since 5.2
 */
@ApiStatus.AvailableSince("5.2")
@Service
public final class InventoryDataProvider {

    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final MultiverseInventories inventories;

    @Inject
    public InventoryDataProvider(
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull MultiverseInventories inventories
    ) {
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.inventories = inventories;
    }

    /**
     * Represents the loaded inventory data.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public static class PlayerInventoryData {
        public final ItemStack[] contents;
        public final ItemStack[] armor;
        public final ItemStack offHand;
        public final String statusMessage; // To indicate if it's live or stored data
        public final ProfileType profileTypeUsed; // To pass back which profile type was used for stored data

        /**
         *
         * @param contents
         * @param armor
         * @param offHand
         * @param statusMessage
         * @param profileTypeUsed
         *
         * @since 5.2
         */
        @ApiStatus.AvailableSince("5.2")
        public PlayerInventoryData(ItemStack[] contents, ItemStack[] armor, ItemStack offHand, String statusMessage, ProfileType profileTypeUsed) {
            this.contents = contents;
            this.armor = armor;
            this.offHand = offHand;
            this.statusMessage = statusMessage;
            this.profileTypeUsed = profileTypeUsed;
        }
    }

    /**
     * Asynchronously loads a player's inventory data.
     * If the player is online AND in the specified world, it attempts to get their live inventory.
     * Otherwise (offline or online in a different world), it loads from Multiverse-Inventories' stored profiles.
     *
     * @param targetPlayer The OfflinePlayer whose inventory data to load.
     * @param worldName The name of the world to load the inventory from (either live or stored).
     * @return A CompletableFuture that will complete with PlayerInventoryData or an exception.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public CompletableFuture<PlayerInventoryData> loadPlayerInventoryData(
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName
    ) {
        // If the player is online, prioritize getting their live inventory
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            // Ensure onlineTarget is not null and their current world matches the requested worldName
            if (onlineTarget != null && onlineTarget.getWorld().getName().equalsIgnoreCase(worldName)) {
                // Get the actual ProfileType for the online player
                ProfileType profileType = ProfileTypes.forPlayer(onlineTarget);
                // Return immediately with live data
                return CompletableFuture.completedFuture(new PlayerInventoryData(
                        onlineTarget.getInventory().getContents(),
                        onlineTarget.getInventory().getArmorContents(),
                        onlineTarget.getInventory().getItemInOffHand(),
                        "Displaying LIVE inventory for " + targetPlayer.getName() + " in world " + worldName + ".",
                        profileType
                ));
            }
            // If online but in a different world, or getPlayer() returned null, fall through to stored data logic
            if (onlineTarget != null) {
                inventories.getLogger().fine("Player " + targetPlayer.getName() + " is online but in world " + onlineTarget.getWorld().getName() + ". Loading stored data for " + worldName + ".");
            } else {
                inventories.getLogger().warning("Player " + targetPlayer.getName() + " is online but getPlayer() returned null. Falling back to stored data.");
            }
        }
            // If the player is offline or online in a different world, or live data failed, load from Multiverse-Inventories' stored profiles
            return CompletableFuture.supplyAsync(() -> {
                ProfileContainer container = profileContainerStoreProvider.getStore(ContainerType.WORLD)
                        .getContainer(worldName);
                if (container == null) {
                    throw new IllegalStateException("Could not load profile container for world: " + worldName);
                }

                PlayerProfile tempProfile = container.getPlayerProfileNow(ProfileTypes.SURVIVAL, targetPlayer);
                ProfileType profileTypeToUse = ProfileTypes.SURVIVAL;

                if (tempProfile == null) {
                    for (ProfileType type : ProfileTypes.getTypes()) {
                        if (type.equals(ProfileTypes.SURVIVAL)) continue;
                        tempProfile = container.getPlayerProfileNow(type, targetPlayer);
                        if (tempProfile != null) {
                            profileTypeToUse = type;
                            break;
                        }
                    }
                }

                if (tempProfile == null) {
                    throw new IllegalStateException("No player data found for " + targetPlayer.getName() + " in world " + worldName + ". Try checking a different world or ensure the player has played in this world.");
                }

                try {
                    ItemStack[] contents = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.INVENTORY).read().join();
                    ItemStack[] armor = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.ARMOR).read().join();
                    ItemStack offHand = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.OFF_HAND).read().join();

                    return new PlayerInventoryData(contents, armor, offHand, "Displaying STORED inventory for " + targetPlayer.getName() + " in world " + worldName + ".", profileTypeToUse);
                } catch (CompletionException e) {
                    // Unwrap CompletionException to get the actual cause
                    throw new IllegalStateException("Error loading inventory data: " + e.getCause().getMessage(), e.getCause());
                }
            });
        }

    /**
     * Asynchronously saves a player's inventory data to their Multiverse-Inventories profile.
     * If the player is online and in the target world, their live inventory is also updated.
     *
     * @param targetPlayer The OfflinePlayer whose inventory data to save.
     * @param worldName The name of the world the inventory belongs to.
     * @param profileType The ProfileType (e.g., SURVIVAL) associated with this inventory data.
     * @param newContents The new main inventory contents (slots 0-35).
     * @param newArmor The new armor contents (helmet, chestplate, leggings, boots).
     * @param newOffHand The new off-hand item.
     * @return A CompletableFuture that completes when saving is done, or an exception occurs.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public CompletableFuture<Void> savePlayerInventoryData(
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName,
            @NotNull ProfileType profileType,
            @NotNull ItemStack[] newContents,
            @NotNull ItemStack[] newArmor,
            @Nullable ItemStack newOffHand
    ) {
        // Save the updated inventory, armor, and off-hand contents asynchronously
        CompletableFuture<Void> saveFuture = CompletableFuture.allOf(
                SingleShareWriter.of(inventories, targetPlayer, worldName, profileType, Sharables.INVENTORY)
                        .write(newContents, true) // true to update if player is online
                        .thenRun(() -> inventories.getLogger().fine("Saved inventory for " + targetPlayer.getName() + " in " + worldName)),
                SingleShareWriter.of(inventories, targetPlayer, worldName, profileType, Sharables.ARMOR)
                        .write(newArmor, true)
                        .thenRun(() -> inventories.getLogger().fine("Saved armor for " + targetPlayer.getName() + " in " + worldName)),
                SingleShareWriter.of(inventories, targetPlayer, worldName, profileType, Sharables.OFF_HAND)
                        .write(newOffHand, true)
                        .thenRun(() -> inventories.getLogger().fine("Saved off-hand for " + targetPlayer.getName() + " in " + worldName))
        );

        return saveFuture.thenRun(() -> {
            inventories.getLogger().info("Inventory for player " + targetPlayer.getName() + " in world " + worldName + " has been modified and saved.");

            // If the target player is online, update their live inventory
            if (targetPlayer.isOnline()) {
                Player onlinePlayer = targetPlayer.getPlayer();
                if (onlinePlayer != null) {
                    // Check if the online player is in the world whose inventory was modified
                    // This is important to avoid overwriting their current inventory if they are in a different world
                    if (onlinePlayer.getWorld().getName().equalsIgnoreCase(worldName)) {
                        // Run Bukkit API calls on the main thread
                        Bukkit.getScheduler().runTask(inventories, () -> {
                            onlinePlayer.getInventory().setContents(newContents);
                            onlinePlayer.getInventory().setArmorContents(newArmor);
                            onlinePlayer.getInventory().setItemInOffHand(newOffHand);
                            onlinePlayer.updateInventory(); // Ensure client sees changes
                            inventories.getLogger().info("Updated live inventory for online player " + onlinePlayer.getName() + " in world " + worldName);
                        });
                    } else {
                        inventories.getLogger().info("Player " + onlinePlayer.getName() + " is online but in a different world (" + onlinePlayer.getWorld().getName() + "), not updating live inventory.");
                    }
                }
            }
        }).exceptionally(throwable -> {
            inventories.getLogger().severe("Failed to save inventory for " + targetPlayer.getName() + " in world " + worldName + ": " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }
}
