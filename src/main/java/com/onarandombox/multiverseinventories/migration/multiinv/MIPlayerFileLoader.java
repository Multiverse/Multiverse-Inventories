package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventoryOld;

import java.io.File;

public class MIPlayerFileLoader {

    private YamlConfiguration playerFile;
    private File file;

    public MIPlayerFileLoader(MultiInv plugin, OfflinePlayer player, String group) {
        // Find and load configuration file for the player
        File worldsFolder = new File(plugin.getDataFolder(), "Groups");
        file = new File(worldsFolder, group + File.separator + player.getName() + ".yml");

        playerFile = new YamlConfiguration();
    }

    public boolean load(){
        if (file.exists()){
            try{
                playerFile.load(file);
                return true;
            }catch (Exception ignore){ }
        }
        return false;
    }

    // Load particular inventory for specified player from specified group
    public MIInventoryInterface getInventory(String inventoryName){
        // Get stored string from configuration file
        MIInventoryInterface inventory;
        String inventoryString = playerFile.getString(inventoryName, null);
        // Check for old inventory save
        if (inventoryString == null || inventoryString.contains(";-;")){
            inventory = new MIInventoryOldWrapper(inventoryString);
        }else{
            inventory = new MIInventoryWrapper(inventoryString);
        }
        return inventory;
    }

    public int getHealth(){
        int health = playerFile.getInt("health", 20);
        if (health <= 0 || health > 20) {
            health = 20;
        }
        return health;
    }

    public int getHunger(){
        int hunger = playerFile.getInt("hunger", 20);
        if (hunger <= 0 || hunger > 20) {
            hunger = 20;
        }
        return hunger;
    }

    public float getSaturation(){
        double saturationDouble = playerFile.getDouble("saturation", 0);
        float saturation = (float)saturationDouble;
        return saturation;
    }


    public int getTotalExperience(){
        return playerFile.getInt("experience", 0);
    }

    public int getLevel(){
        return playerFile.getInt("level", 0);
    }

    public float getExperience(){
        double expDouble = playerFile.getDouble("exp", 0);
        float exp = (float)expDouble;
        return exp;
    }
}
