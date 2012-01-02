package com.onarandombox.multiverseprofiles.data;

import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class WorldGroup implements ConfigurationSerializable {

    private String name = "";
    private HashSet<World> worlds = new HashSet<World>();
    private Shares shares = new Shares();

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getMVWorld());

        return result;
    }

    public static WorldGroup deserialize(Map<String, Object> args) {

        return null;
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
}
