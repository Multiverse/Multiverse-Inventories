package org.mvplugins.multiverse.inventories.dataimport.multiinv;

import org.bukkit.inventory.ItemStack;

/**
 * A little interface for retrieving normal ItemStack from the MultiInv inventory classes.
 */
sealed interface MIInventoryInterface permits MIInventoryWrapper, MIInventoryOldWrapper {

    /**
     * @return The inventory contents.
     */
    ItemStack[] getInventoryContents();

    /**
     * @return The armor contents.
     */
    ItemStack[] getArmorContents();
}

