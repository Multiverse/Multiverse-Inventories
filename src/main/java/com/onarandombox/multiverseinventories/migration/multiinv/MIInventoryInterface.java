package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.inventory.ItemStack;

public interface MIInventoryInterface {

    ItemStack[] getInventoryContents();

    ItemStack[] getArmorContents();
}
