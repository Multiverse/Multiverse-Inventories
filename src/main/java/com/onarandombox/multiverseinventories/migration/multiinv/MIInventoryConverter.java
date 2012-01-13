package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;

/**
 * Utility class for converting proprietary shit from MultiInv.
 */
public class MIInventoryConverter {

    /**
     * @param oldContents Proprietary shiet from MultiInv.
     * @return Standard ItemStacks.
     */
    public static ItemStack[] convertMIItems(MIItemStack[] oldContents) {
        ItemStack[] newContents = new ItemStack[oldContents.length];
        for (int i = 0; i < oldContents.length; i++) {
            newContents[i] = oldContents[i].getItemStack();
        }
        return newContents;
    }

    private MIInventoryConverter() {
    }
}
