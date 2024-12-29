package org.mvplugins.multiverse.inventories.migration.multiinv;

import org.bukkit.inventory.ItemStack;

/**
 * A little interface for retrieving normal ItemStack from the MultiInv inventory classes.
 */
public interface MIInventoryInterface {

    /**
     * @return The inventory contents.
     */
    ItemStack[] getInventoryContents();

    /**
     * @return The armor contents.
     */
    ItemStack[] getArmorContents();
}

