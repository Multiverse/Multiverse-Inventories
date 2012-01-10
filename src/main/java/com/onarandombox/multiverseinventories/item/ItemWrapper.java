package com.onarandombox.multiverseinventories.item;

import org.bukkit.inventory.ItemStack;

/**
 * This is meant to wrap an ItemStack so that it can easily be serialized/deserialized in FileConfiguration format.
 */
public interface ItemWrapper {

    /**
     * Retrieves the ItemStack that this class is wrapping.
     *
     * @return The ItemStack this class is wrapping.
     */
    ItemStack getItem();
}
