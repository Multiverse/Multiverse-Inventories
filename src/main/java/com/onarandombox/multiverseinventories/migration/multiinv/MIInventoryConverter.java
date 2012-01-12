package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;

public class MIInventoryConverter {

    public static ItemStack[] convertMIItems(MIItemStack[] oldContents) {
        ItemStack[] newContents = new ItemStack[oldContents.length];
        for (int i = 0; i < oldContents.length; i++) {
            newContents[i] = oldContents[i].getItemStack();
        }
        return newContents;
    }
}
