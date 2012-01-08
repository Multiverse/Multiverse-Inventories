package com.onarandombox.multiverseinventories.world;

import com.onarandombox.multiverseinventories.util.MILog;
import com.onarandombox.multiverseinventories.util.ProfileDeserializationException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.List;

/**
 * @author dumptruckman
 */
public class SimpleWorldGroup implements WorldGroup {

    private String name = "";
    private String permission = null;
    private HashSet<String> worlds = new HashSet<String>();
    private Shares shares = new SimpleShares();

    /*
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("permission", this.getPermission());
        result.put("worlds", this.getWorlds());
        result.put("shares", this.getShares().toStringList());

        return result;
    }
    */

    public static WorldGroup deserialize(String name, ConfigurationSection data) throws ProfileDeserializationException {
        if (!data.contains("worlds")) {
            throw new ProfileDeserializationException("No worlds specified for world group: " + name);
        }
        List<String> worldList = data.getStringList("worlds");
        if (worldList == null) {
            throw new ProfileDeserializationException("Worlds incorrectly formatted for group: " + name);
        }
        WorldGroup worldGroup = new SimpleWorldGroup();
        for (String worldName : worldList) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                worldGroup.addWorld(world);
            } else {
                MILog.warning("");
            }
        }
        if (data.contains("shares")) {
            List<String> sharesList = data.getStringList("shares");
            if (sharesList != null) {
                worldGroup.setShares(SimpleShares.parseShares(sharesList));
            } else {
                MILog.warning("Shares formatted incorrectly for group: " + name);
            }
        }
        if (data.contains("permission")) {
            String permission = data.getString("permission");
            if (permission != null) {
                worldGroup.setPermission(permission);
            } else {
                MILog.warning("Permission formatted incorrectly for group: " + name);
            }
        }
        return worldGroup;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addWorld(String worldName) {
        this.worlds.add(worldName);
    }

    public void addWorld(World world) {
        this.worlds.add(world.getName());
    }

    public HashSet<String> getWorlds() {
        return this.worlds;
    }

    public void setShares(Shares shares) {
        this.shares = shares;
    }

    public Shares getShares() {
        return this.shares;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
