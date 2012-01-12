package com.onarandombox.multiverseinventories.migration.multiinv;

import com.onarandombox.multiverseinventories.util.PlayerStats;

/**
 * Contains the player inventories as collected through MultiInv's player files.
 */
public class MIInventory {

    private MIItemStack[] inventoryContents = new MIItemStack[PlayerStats.INVENTORY_SIZE];
    private MIItemStack[] armorContents = new MIItemStack[PlayerStats.ARMOR_SIZE];

    // Create an MIInventory from a string containing inventory data
    public MIInventory(String inventoryString) {
        if (inventoryString != null) {
            // data[0] = inventoryContents
            // data[1] = armourContents
            String[] data = inventoryString.split(":");

            // Fill MIInventoryContents
            String[] inventoryData = data[0].split(";");
            for (int i = 0; i < inventoryData.length; i++) {
                inventoryContents[i] = new MIItemStack(inventoryData[i]);
            }

            // Fill MIArmourContents
            String[] armourData = data[1].split(";");
            for (int i = 0; i < armourData.length; i++) {
                armorContents[i] = new MIItemStack(armourData[i]);
            }
        }
    }
}
