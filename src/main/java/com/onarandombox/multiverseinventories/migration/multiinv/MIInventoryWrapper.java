package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class MIInventoryWrapper extends MIInventory implements MIInventoryInterface {

    public MIInventoryWrapper(String inventoryString) {
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
