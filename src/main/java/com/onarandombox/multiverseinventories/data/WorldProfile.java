package com.onarandombox.multiverseinventories.data;

import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author dumptruckman
 */
public class WorldProfile implements ConfigurationSerializable {

    private HashMap<Player, PlayerProfile> playerData = new HashMap<Player, PlayerProfile>();
    private HashMap<World, Shares> relationships = new HashMap<World, Shares>();

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());

        return result;
    }

    public static WorldProfile deserialize(Map<String, Object> args) {

        return null;
    }

    public PlayerProfile getPlayerData(Player player) {
        return this.playerData.get(player);
    }
}
