package com.onarandombox.multiverseinventories.migration.multiinv;

import com.onarandombox.multiverseinventories.api.PlayerStats;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.io.File;

/**
 * A replacement for MultiInv's MIPlayerFile class so that it may accept an OfflinePlayer instead of Player.
 */
public class MIPlayerFileLoader {

    private YamlConfiguration playerFile;
    private File file;

    public MIPlayerFileLoader(MultiInv plugin, OfflinePlayer player, String group) {
        // Find and load configuration file for the player
        File worldsFolder = new File(plugin.getDataFolder(), "Groups");
        file = new File(worldsFolder, group + File.separator + player.getName() + ".yml");

        playerFile = new YamlConfiguration();
    }

    /**
     * Loads the player file into memory.
     *
     * @return True if there was a file to load and it loaded successfully.
     */
    public boolean load() {
        if (file.exists()) {
            try {
                playerFile.load(file);
                return true;
            } catch (Exception ignore) { }
        }
        return false;
    }

    /**
     * Load particular inventory for specified player from specified group.
     *
     * @param inventoryName The gamemode for the inventory to load.
     * @return An interface for retrieve the inventory/armor contents.
     */
    public MIInventoryInterface getInventory(String inventoryName) {
        // Get stored string from configuration file
        MIInventoryInterface inventory;
        String inventoryString = playerFile.getString(inventoryName, null);
        // Check for old inventory save
        if (inventoryString == null || inventoryString.contains(";-;")) {
            inventory = new MIInventoryOldWrapper(inventoryString);
        } else {
            inventory = new MIInventoryWrapper(inventoryString);
        }
        return inventory;
    }

    /**
     * @return The player's health.
     */
    public double getHealth() {
        double health = playerFile.getDouble("health", PlayerStats.HEALTH);
        if (health <= 0 || health > PlayerStats.HEALTH) {
            health = PlayerStats.HEALTH;
        }
        return health;
    }

    /**
     * @return The player's hunger.
     */
    public int getHunger() {
        int hunger = playerFile.getInt("hunger", PlayerStats.FOOD_LEVEL);
        if (hunger <= 0 || hunger > PlayerStats.FOOD_LEVEL) {
            hunger = PlayerStats.FOOD_LEVEL;
        }
        return hunger;
    }

    /**
     * @return The player's saturation.
     */
    public float getSaturation() {
        double saturationDouble = playerFile.getDouble("saturation", 0);
        float saturation = (float) saturationDouble;
        return saturation;
    }

    /**
     * @return The player's total exp.
     */
    public int getTotalExperience() {
        return playerFile.getInt("experience", 0);
    }

    /**
     * @return The player's level.
     */
    public int getLevel() {
        return playerFile.getInt("level", 0);
    }

    /**
     * @return The player's exp.
     */
    public float getExperience() {
        double expDouble = playerFile.getDouble("exp", 0);
        float exp = (float) expDouble;
        return exp;
    }
}

