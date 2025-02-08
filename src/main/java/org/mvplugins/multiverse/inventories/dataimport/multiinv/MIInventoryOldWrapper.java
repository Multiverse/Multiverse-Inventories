package org.mvplugins.multiverse.inventories.dataimport.multiinv;

import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventoryOld;

/**
 * Wraps MIInventoryOld to provide a way of accessing the inventory/armor contents.
 */
final class MIInventoryOldWrapper extends MIInventoryOld implements MIInventoryInterface {

    public MIInventoryOldWrapper(String inventoryString) {
        super(inventoryString);
    }

    @Override
    public ItemStack[] getInventoryContents() {
        return MIInventoryConverter.convertMIItems(this.MIInventoryContents);
    }

    @Override
    public ItemStack[] getArmorContents() {
        return MIInventoryConverter.convertMIItems(this.MIArmourContents);
    }
}

