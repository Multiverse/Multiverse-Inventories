package com.onarandombox.multiverseinventories.util;

import org.bukkit.inventory.ItemStack;

/**
 * General tools to help with minecraftian things.
 */
public class MinecraftTools {

    private static final int TICKS_PER_SECOND = 20;

    private MinecraftTools() { }

    /**
     * Converts an amount of seconds to the appropriate amount of ticks.
     *
     * @param seconds Amount of seconds to convert
     * @return Ticks converted from seconds.
     */
    public static long convertSecondsToTicks(long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Fills an ItemStack array with air.
     *
     * @param items The ItemStack array to fill.
     * @return The air filled ItemStack array.
     */
    public static ItemStack[] fillWithAir(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            items[i] = new ItemStack(0);
        }
        return items;
    }
}

