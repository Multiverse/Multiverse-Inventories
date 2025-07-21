package org.mvplugins.multiverse.inventories.view;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.InventoryDataProvider;

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
            meta.setDisplayName(ChatColor.GOLD + name);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
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
        PersistentDataContainer persistentDataContainer = item.getItemMeta().getPersistentDataContainer();
        return persistentDataContainer.has(IS_FILLER_KEY, PersistentDataType.BYTE) &&
                persistentDataContainer.getOrDefault(IS_FILLER_KEY, PersistentDataType.BYTE, (byte) 0) == (byte) 1;
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

        return switch (slot) {
            case 36 -> item.getType().name().endsWith("_HELMET");
            case 37 -> item.getType().name().endsWith("_CHESTPLATE");
            case 38 -> item.getType().name().endsWith("_LEGGINGS");
            case 39 -> item.getType().name().endsWith("_BOOTS");

            // Off-hand is very permissive in vanilla. Allow any non-air item.
            // If you want to restrict this further (e.g., only shields/totems),
            // add more specific Material checks here.
            case 40 -> true;

            // Padding slot
            // Cannot place items in padding slots
            case 41, 42, 43, 44 -> false;

            // For non-special slots (main inventory), any item is generally allowed.
            default -> true;
        };
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
    public ItemStack createFillerItemForSlot(int slot, boolean isModifiable) {
        String helmetLore = isModifiable ? "Place Helmet Here" : "No Helmet";
        String chestplateLore = isModifiable ? "Place Chestplate Here" : "No Chestplate";
        String leggingsLore = isModifiable ? "Place Leggings Here" : "No Leggings";
        String bootsLore = isModifiable ? "Place Boots Here" : "No Boots";
        String offHandLore = isModifiable ? "Place Off-Hand Item Here" : "No Off-Hand Item";

        return switch (slot) {
            case 36 -> createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Helmet Slot", helmetLore);
            case 37 -> createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Chestplate Slot", chestplateLore);
            case 38 -> createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Leggings Slot", leggingsLore);
            case 39 -> createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Boots Slot", bootsLore);
            case 40 -> createFillerItem(Material.GRAY_STAINED_GLASS_PANE, "Off-Hand Slot", offHandLore);
            case 41, 42, 43, 44 -> createFillerItem(Material.BARRIER, " ", " "); // Padding slots
            default -> new ItemStack(Material.AIR); // Should not happen for these slots
        };
    }

    /**
     * Populates the given custom inventory GUI with player inventory data and appropriate filler items.
     * @param inv The Inventory GUI to populate.
     * @param playerInventoryData The data containing player's contents, armor, and off-hand.
     * @param isModifiable True if the inventory is modifiable, false for read-only.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public void populateInventoryGUI(@NotNull Inventory inv,
                                     @NotNull InventoryDataProvider.PlayerInventoryData playerInventoryData,
                                     boolean isModifiable) {
        // Fill main inventory slots (0–35)
        if (playerInventoryData.contents != null) {
            for (int i = 0; i < Math.min(playerInventoryData.contents.length, 36); i++) {
                inv.setItem(i, playerInventoryData.contents[i]);
            }
        }
        // Armor slot mapping for display in the GUI and add fillers if empty
        // GUI Slots: 36=Helmet, 37=Chestplate, 38=Leggings, 39=Boots
        // Minecraft Internal: armor[3]=Helmet, armor[2]=Chestplate, armor[1]=Leggings, armor[0]=Boots

        // Slot 36: Helmet
        if (playerInventoryData.armor == null || playerInventoryData.armor[3] == null) {
            inv.setItem(36, createFillerItemForSlot(36, isModifiable));
        } else {
            inv.setItem(36, playerInventoryData.armor[3]);
        }
        // Slot 37: Chestplate
        if (playerInventoryData.armor == null || playerInventoryData.armor[2] == null) {
            inv.setItem(37, createFillerItemForSlot(37, isModifiable));
        } else {
            inv.setItem(37, playerInventoryData.armor[2]);
        }
        // Slot 38: Leggings
        if (playerInventoryData.armor == null || playerInventoryData.armor[1] == null) {
            inv.setItem(38, createFillerItemForSlot(38, isModifiable));
        } else {
            inv.setItem(38, playerInventoryData.armor[1]);
        }
        // Slot 39: Boots
        if (playerInventoryData.armor == null || playerInventoryData.armor[0] == null) {
            inv.setItem(39, createFillerItemForSlot(39, isModifiable));
        } else {
            inv.setItem(39, playerInventoryData.armor[0]);
        }

        // Off-hand slot (40) and add filler if empty
        if (playerInventoryData.offHand == null || playerInventoryData.offHand.getType() == Material.AIR) {
            inv.setItem(40, createFillerItemForSlot(40, isModifiable));
        } else {
            inv.setItem(40, playerInventoryData.offHand);
        }

        // Fill the remaining slots (41-44) with non-interactable filler items
        for (int i = 41; i <= 44; i++) {
            inv.setItem(i, createFillerItemForSlot(i, isModifiable)); // Use createFillerItemForSlot for padding too
        }
    }
}
