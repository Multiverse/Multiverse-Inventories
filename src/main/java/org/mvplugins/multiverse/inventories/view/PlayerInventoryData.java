package org.mvplugins.multiverse.inventories.view;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

/**
 * Represents the loaded inventory data.
 *
 * @since 5.2
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince("5.2")
public final class PlayerInventoryData {
    public final ItemStack[] contents;
    public final ItemStack[] armor;
    public final ItemStack offHand;
    public final InventoryStatus status;
    public final ProfileType profileTypeUsed;

    // Non-inventory data
    public final Double health;
    public final Double maxHealth;
    public final Integer level;
    public final Float exp;
    public final Integer foodLevel;
    public final Float saturation;
    public final String lastLocation;

    /**
     *
     * @param contents        The player's main inventory contents (slots 0-35).
     * @param armor           The player's armor contents (boots, leggings, chestplate, helmet).
     * @param offHand         The player's off-hand item.
     * @param status          The status of the inventory data load (e.g., LIVE, STORED, NO_DATA_FOUND).
     * @param profileTypeUsed The profile type that was used to retrieve the data (e.g., SURVIVAL).
     * @param health          The player's current health.
     * @param maxHealth       The player's maximum health.
     * @param level           The player's current experience level.
     * @param exp             The player's current experience progress towards the next level (0.0-1.0).
     * @param foodLevel       The player's current food level (0-20).
     * @param saturation      The player's current saturation level.
     * @param lastLocation    The player's last location.
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public PlayerInventoryData(ItemStack[] contents, ItemStack[] armor, ItemStack offHand, InventoryStatus status,
                               ProfileType profileTypeUsed, Double health, Double maxHealth, Integer level, Float exp, Integer foodLevel,
                               Float saturation, String lastLocation) {
        this.contents = contents;
        this.armor = armor;
        this.offHand = offHand;
        this.status = status;
        this.profileTypeUsed = profileTypeUsed;

        // Non-inventory data
        this.health = health;
        this.maxHealth = maxHealth;
        this.level = level;
        this.exp = exp;
        this.foodLevel = foodLevel;
        this.saturation = saturation;
        this.lastLocation = lastLocation;
    }
}
