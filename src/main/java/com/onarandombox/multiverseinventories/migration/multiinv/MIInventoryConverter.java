package com.onarandombox.multiverseinventories.migration.multiinv;

import com.onarandombox.multiverseinventories.util.MinecraftTools;
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
        ItemStack[] newContents = MinecraftTools.fillWithAir(new ItemStack[oldContents.length]);
        for (int i = 0; i < oldContents.length; i++) {
            if (oldContents[i] != null && oldContents[i].getItemStack() != null) {
                newContents[i] = oldContents[i].getItemStack();
            }
        }
        return newContents;
    }

    private MIInventoryConverter() {
    }
}

