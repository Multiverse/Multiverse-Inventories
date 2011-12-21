package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.MVIManager;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author dumptruckman
 */
public class WorldGroup implements ConfigurationSerializable {

    private String name = "";
    private HashSet<World> worlds = new HashSet<World>();
    private Shares shares = new Shares();

    private HashMap<Player, WorldGroupPlayer> playerData = null;

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());

        return result;
    }

    public static WorldGroup deserialize(Map<String, Object> args) {

        return null;
    }

    public static List<WorldGroup> getGroupsForWorld(World world) {
        return MVIManager.getWorldGroups().get(world);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }

    public HashSet<World> getWorlds() {
        return worlds;
    }

    public Shares getShares() {
        return shares;
    }

    public WorldGroupPlayer getPlayerData(Player player) {
        return this.playerData.get(player);
    }
}
