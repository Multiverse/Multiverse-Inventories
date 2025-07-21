package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.minecraft.util.Logging;
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
    InventoryDataProvider(
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
        public final InventoryStatus status; // To indicate if it's live or stored data
        public final ProfileType profileTypeUsed; // To pass back which profile type was used for stored data

        // Non-inventory data
        public final double health;
        public final int level;
        public final float exp;
        public final int foodLevel;
        public final float saturation;

        /**
         *
         * @param contents The player's main inventory contents (slots 0-35).
         * @param armor The player's armor contents (boots, leggings, chestplate, helmet).
         * @param offHand The player's off-hand item.
         * @param status The status of the inventory data load (e.g., LIVE, STORED, NO_DATA_FOUND).
         * @param profileTypeUsed The profile type that was used to retrieve the data (e.g., SURVIVAL).
         * @param health The player's current health.
         * @param level The player's current experience level.
         * @param exp The player's current experience progress towards the next level (0.0-1.0).
         * @param foodLevel The player's current food level (0-20).
         * @param saturation The player's current saturation level.
         *
         * @since 5.2
         */
        @ApiStatus.AvailableSince("5.2")
        public PlayerInventoryData(ItemStack[] contents, ItemStack[] armor, ItemStack offHand, InventoryStatus status,
                                   ProfileType profileTypeUsed, double health, int level, float exp, int foodLevel,
                                   float saturation) {
            this.contents = contents;
            this.armor = armor;
            this.offHand = offHand;
            this.status = status;
            this.profileTypeUsed = profileTypeUsed;

            // Non-inventory data
            this.health = health;
            this.level = level;
            this.exp = exp;
            this.foodLevel = foodLevel;
            this.saturation = saturation;


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
        if (!targetPlayer.isOnline()) {
            return loadInventoryDataFromProfileStorage(targetPlayer, worldName);
        }

        Player onlineTarget = targetPlayer.getPlayer();
        // Ensure onlineTarget is not null and their current world matches the requested worldName
        if (onlineTarget != null && onlineTarget.getWorld().getName().equalsIgnoreCase(worldName)) {
            return loadInventoryDataFromPlayer(onlineTarget, worldName);
        }
        // If online but in a different world, or getPlayer() returned null, fall through to stored data logic
        if (onlineTarget != null) {
            Logging.fine("Player " + targetPlayer.getName() + " is online but in world " + onlineTarget.getWorld().getName() + ". Loading stored data for " + worldName + ".");
        } else {
            Logging.warning("Player " + targetPlayer.getName() + " is online but getPlayer() returned null. Falling back to stored data.");
        }
        // If the player is offline or online in a different world, or live data failed, load from Multiverse-Inventories' stored profiles
        return loadInventoryDataFromProfileStorage(targetPlayer, worldName);
    }

    private CompletableFuture<PlayerInventoryData> loadInventoryDataFromPlayer(
            @NotNull Player onlineTarget,
            @NotNull String worldName
    ) {
        // Get the actual ProfileType for the online player
        ProfileType profileType = ProfileTypes.forPlayer(onlineTarget);
        // Return immediately with live data
        return CompletableFuture.completedFuture(new PlayerInventoryData(
                onlineTarget.getInventory().getContents(),
                onlineTarget.getInventory().getArmorContents(),
                onlineTarget.getInventory().getItemInOffHand(),
                InventoryStatus.LIVE_INVENTORY,
                profileType,
                onlineTarget.getHealth(),
                onlineTarget.getLevel(),
                onlineTarget.getExp(),
                onlineTarget.getFoodLevel(),
                onlineTarget.getSaturation()
        ));
    }

    private CompletableFuture<PlayerInventoryData> loadInventoryDataFromProfileStorage(
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName
    ) {
        return CompletableFuture.supplyAsync(() -> {
            ProfileContainer container = profileContainerStoreProvider.getStore(ContainerType.WORLD)
                    .getContainer(worldName);
            if (container == null) {
                throw new IllegalStateException("Could not load profile container for world: " + worldName);
            }

            PlayerProfile tempProfile = loadMVInvPlayerProfile(container, targetPlayer);
            if (tempProfile == null) {
                throw new IllegalStateException(InventoryStatus.NO_DATA_FOUND.getFormattedMessage(targetPlayer.getName(), worldName));
            }
            ProfileType profileTypeToUse = tempProfile.getProfileType();
            try {
                ItemStack[] contents = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.INVENTORY).read().join();
                ItemStack[] armor = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.ARMOR).read().join();
                ItemStack offHand = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.OFF_HAND).read().join();

                // Non-inventory data
                double storedHealth = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.HEALTH)
                        .read().join();
                int storedLevel = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.LEVEL)
                        .read().join();
                float storedExp = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.EXPERIENCE)
                        .read().join();
                int storedFoodLevel = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.FOOD_LEVEL)
                        .read().join();
                float storedSaturationLevel = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.SATURATION)
                        .read().join();

                return new PlayerInventoryData(
                        contents,
                        armor,
                        offHand,
                        InventoryStatus.STORED_INVENTORY,
                        profileTypeToUse,
                        storedHealth,
                        storedLevel,
                        storedExp,
                        storedFoodLevel,
                        storedSaturationLevel
                );
            } catch (CompletionException e) {
                // Unwrap CompletionException to get the actual cause
                throw new IllegalStateException("Error loading inventory data: " + e.getCause().getMessage(), e.getCause());
            }
        });
    }

    @Nullable
    private PlayerProfile loadMVInvPlayerProfile(ProfileContainer container, OfflinePlayer targetPlayer) {
        PlayerProfile survivalProfile = container.getPlayerProfileNow(ProfileTypes.SURVIVAL, targetPlayer);
        if (survivalProfile != null) {
            return survivalProfile;
        }
        for (ProfileType type : ProfileTypes.getTypes()) {
            if (type.equals(ProfileTypes.SURVIVAL)) continue;
            PlayerProfile profile = container.getPlayerProfileNow(type, targetPlayer);
            if (profile != null) {
                return profile;
            }
        }
        return null;
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
        CompletableFuture<Void> saveFuture = writeInventoryDataToProfile(
                targetPlayer,
                worldName,
                profileType,
                newContents,
                newArmor,
                newOffHand
        );

        return saveFuture.thenRun(() -> {
            Logging.info("Inventory for player " + targetPlayer.getName() + " in world " + worldName + " has been modified and saved.");
            updateOnlinePlayerInventoryData(targetPlayer, worldName, newContents, newArmor, newOffHand);
        }).exceptionally(throwable -> {
            Logging.severe("Failed to save inventory for " + targetPlayer.getName() + " in world " + worldName + ": " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    private void updateOnlinePlayerInventoryData(
            OfflinePlayer targetPlayer,
            String worldName,
            @NotNull ItemStack[] newContents,
            @NotNull ItemStack[] newArmor,
            ItemStack newOffHand
    ) {
        // If the target player is online, update their live inventory
        if (!targetPlayer.isOnline()) {
            return;
        }

        Player onlinePlayer = targetPlayer.getPlayer();
        if (onlinePlayer == null) {
            return;
        }

        // Check if the online player is in the world whose inventory was modified
        // This is important to avoid overwriting their current inventory if they are in a different world
        if (!onlinePlayer.getWorld().getName().equalsIgnoreCase(worldName)) {
            Logging.info("Player " + onlinePlayer.getName() + " is online but in a different world (" + onlinePlayer.getWorld().getName() + "), not updating live inventory.");
            return;
        }

        // Run Bukkit API calls on the main thread
        Bukkit.getScheduler().runTask(inventories, () -> {
            onlinePlayer.getInventory().setContents(newContents);
            onlinePlayer.getInventory().setArmorContents(newArmor);
            onlinePlayer.getInventory().setItemInOffHand(newOffHand);
            onlinePlayer.updateInventory(); // Ensure client sees changes
            Logging.info("Updated live inventory for online player " + onlinePlayer.getName() + " in world " + worldName);
        });
    }

    private CompletableFuture<Void> writeInventoryDataToProfile(
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName,
            @NotNull ProfileType profileType,
            @NotNull ItemStack[] newContents,
            @NotNull ItemStack[] newArmor,
            @Nullable ItemStack newOffHand
    ) {
        // Save the updated inventory, armor, and off-hand contents asynchronously
        return CompletableFuture.allOf(
                SingleShareWriter.of(inventories, targetPlayer, worldName, profileType, Sharables.INVENTORY)
                        .write(newContents, true) // true to update if player is online
                        .thenRun(() -> Logging.fine("Saved inventory for " + targetPlayer.getName() + " in " + worldName)),
                SingleShareWriter.of(inventories, targetPlayer, worldName, profileType, Sharables.ARMOR)
                        .write(newArmor, true)
                        .thenRun(() -> Logging.fine("Saved armor for " + targetPlayer.getName() + " in " + worldName)),
                SingleShareWriter.of(inventories, targetPlayer, worldName, profileType, Sharables.OFF_HAND)
                        .write(newOffHand, true)
                        .thenRun(() -> Logging.fine("Saved off-hand for " + targetPlayer.getName() + " in " + worldName))
        );
    }
}
