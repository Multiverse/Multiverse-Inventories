package com.onarandombox.multiverseinventories.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman, SwearWord
 */
public class WorldGroupPlayer implements ConfigurationSerializable {

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());
        return result;
    }

    public static WorldGroupPlayer deserialize(Map<String, Object> args) {
        
        return null;
    }

    public Inventory[] getInventoryContents() {

        return null;
    }
}
