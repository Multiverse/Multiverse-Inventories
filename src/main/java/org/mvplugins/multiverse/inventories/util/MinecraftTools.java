package org.mvplugins.multiverse.inventories.util;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * General tools to help with minecraftian things.
 */
public final class MinecraftTools {

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
            items[i] = new ItemStack(Material.AIR);
        }
        return items;
    }

    public static @Nullable Location findBedFromRespawnLocation(@Nullable Location respawnLocation) {
        if (respawnLocation == null) {
            return null;
        }
        var bedSpawnBlock = respawnLocation.getBlock();
        for(int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    var newBedBlock = bedSpawnBlock.getRelative(x, y, z);
                    Logging.finest("Finding bed at: " + newBedBlock);
                    if (newBedBlock.getBlockData() instanceof Bed) {
                        Logging.finer("Found bed!");
                        return newBedBlock.getLocation();
                    }
                }
            }
        }
        Logging.warning("Unable to anchor, respawn may not work as expected!");
        return respawnLocation;
    }

    public static @Nullable Location findAnchorFromRespawnLocation(@Nullable Location respawnLocation) {
        if (respawnLocation == null) {
            return null;
        }
        var bedSpawnBlock = respawnLocation.getBlock();
        for(int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    var newBedBlock = bedSpawnBlock.getRelative(x, y, z);
                    Logging.finest("Finding anchor at: " + newBedBlock);
                    if (newBedBlock.getBlockData() instanceof RespawnAnchor) {
                        Logging.finer("Found anchor!");
                        return newBedBlock.getLocation();
                    }
                }
            }
        }
        Logging.warning("Unable to anchor, respawn may not work as expected!");
        return respawnLocation;
    }
}
