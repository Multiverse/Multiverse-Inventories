package com.onarandombox.multiverseinventories.util;

import org.bukkit.inventory.ItemStack;

/**
 * @author dumptruckman
 */
public class MinecraftTools {

    public static long convertSecondsToTicks(long seconds) {
        return seconds * 20;
    }

    public static ItemStack[] fillWithAir(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            items[i] = new ItemStack(0);
        }
        return items;
    }
}
