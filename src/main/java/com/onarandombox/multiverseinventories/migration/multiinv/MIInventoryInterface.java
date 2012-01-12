package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;

public interface MIInventoryInterface {

    ItemStack[] getInventoryContents();

    ItemStack[] getArmorContents();
}
