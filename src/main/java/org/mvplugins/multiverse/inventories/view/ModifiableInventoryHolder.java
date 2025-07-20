package org.mvplugins.multiverse.inventories.view;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

/**
 * A custom InventoryHolder that serves as a marker for modifiable inventories.
 * It stores the necessary context (player, world, profile type, plugin instance)
 * to save changes back to the player's profile when the inventory is closed.
 */
public final class ModifiableInventoryHolder implements InventoryHolder {
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
        // This method is required by the interface but is not directly used for the marker purpose.
        // Throwing UnsupportedOperationException clearly indicates it's not meant to be called.
        throw new UnsupportedOperationException("ModifiableInventoryHolder does not provide an Inventory directly.");
    }
}