package org.mvplugins.multiverse.inventories.view;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.MultiverseInventories;

import java.util.Collections;

/**
 * A helper class for creating and validating items within the custom inventory GUIs.
 * This centralizes logic for filler items and slot-specific item validation.
 *
 * @since 5.2
 */
@ApiStatus.AvailableSince("5.2")
@Service
public final class InventoryGUIHelper {

    private final NamespacedKey IS_FILLER_KEY; // Key to mark filler items

    @Inject
    InventoryGUIHelper(@NotNull MultiverseInventories inventories) {
        this.IS_FILLER_KEY = new NamespacedKey(inventories, "is_mvinv_filler");
    }

    /**
     * Creates a generic filler item for GUI slots.
     *
     * @param material The material of the filler item.
     * @param name The display name of the item.
     * @param lore The lore text for the item.
     * @return The created ItemStack.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public ItemStack createFillerItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.GOLD));
            meta.lore(Collections.singletonList(Component.text(lore, NamedTextColor.GRAY)));
            meta.getPersistentDataContainer().set(IS_FILLER_KEY, PersistentDataType.BYTE, (byte) 1); // store 1 for true
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Checks if a given ItemStack is a filler item created by this helper.
     *
     * @param item The ItemStack to check.
     * @return True if the item is a filler, false otherwise.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public boolean isFillerItem(@NotNull ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(IS_FILLER_KEY, PersistentDataType.BYTE) &&
                item.getItemMeta().getPersistentDataContainer().get(IS_FILLER_KEY, PersistentDataType.BYTE) == (byte) 1;
    }

    /**
     * Determines if an ItemStack is valid for a given special inventory slot in the custom GUI.
     * This method is used for both armor and off-hand slot validation.
     *
     * @param item The ItemStack to check.
     * @param slot The raw slot number (36-40 for armor/off-hand).
     * @return True if the item is valid for the slot, false otherwise.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public boolean isValidItemForSlot(@NotNull ItemStack item, int slot) {
        if (item.getType() == Material.AIR) {
            return true; // Air is always valid (it means the slot is empty)
        }
        // If it's a filler item, it's considered valid for its own slot context (e.g., when checking if slot is empty)
        if (isFillerItem(item)) {
            return true;
        }

        switch (slot) {
            case 36: // Helmet slot
                return item.getType().name().endsWith("_HELMET");
            case 37: // Chestplate slot
                return item.getType().name().endsWith("_CHESTPLATE");
            case 38: // Leggings slot
                return item.getType().name().endsWith("_LEGGINGS");
            case 39: // Boots slot
                return item.getType().name().endsWith("_BOOTS");
            case 40: // Off-hand slot
                // Off-hand is very permissive in vanilla. Allow any non-air item.
                // If you want to restrict this further (e.g., only shields/totems),
                // add more specific Material checks here.
                return true;
            case 41: // Padding slot
            case 42: // Padding slot
            case 43: // Padding slot
            case 44: // Padding slot
                return false; // Cannot place items in padding slots
            default:
                return true; // For non-special slots (main inventory), any item is generally allowed.
        }
    }

    /**
     * Creates the appropriate filler item for a given special slot in the custom GUI.
     *
     * @param slot The raw slot number (36-40).
     * @return The specific filler ItemStack for that slot.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public ItemStack createFillerItemForSlot(int slot) {
        switch (slot) {
            case 36: return createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Helmet Slot", "Place Helmet Here");
            case 37: return createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Chestplate Slot", "Place Chestplate Here");
            case 38: return createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Leggings Slot", "Place Leggings Here");
            case 39: return createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Boots Slot", "Place Boots Here");
            case 40: return createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Off-Hand Slot", "Place Off-Hand Item Here");
            case 41:
            case 42:
            case 43:
            case 44: return createFillerItem(Material.BARRIER, " ", " "); // Padding slots
            default: return new ItemStack(Material.AIR); // Should not happen for these slots
        }
    }
}