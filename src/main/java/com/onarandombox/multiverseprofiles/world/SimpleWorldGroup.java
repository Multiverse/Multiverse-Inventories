package com.onarandombox.multiverseprofiles.world;

import org.bukkit.World;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class SimpleWorldGroup implements WorldGroup {

    private String name = "";
    private HashSet<World> worlds = new HashSet<World>();
    private Shares shares = new SimpleShares();

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());

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
