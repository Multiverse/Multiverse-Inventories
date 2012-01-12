package com.onarandombox.multiverseinventories.group;

import com.google.common.collect.Lists;
import com.onarandombox.multiverseinventories.group.blacklist.ItemBlacklist;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of WorldGroup.
 */
public class SimpleWorldGroup implements WorldGroup {

    private String name = "";
    private HashSet<String> worlds = new HashSet<String>();
    private Shares shares = new SimpleShares();
    private HashMap<String, ItemBlacklist> itemBlacklist = new HashMap<String, ItemBlacklist>();

    public SimpleWorldGroup(String name) {
        this.name = name;
    }

    public SimpleWorldGroup(String name, ConfigurationSection data) throws DeserializationException {
        if (!data.contains("worlds")) {
            throw new DeserializationException("No worlds specified for world group: " + name);
        }
        List<String> worldList = data.getStringList("worlds");
        if (worldList == null) {
            throw new DeserializationException("Worlds incorrectly formatted for group: " + name);
        }
        for (String worldName : worldList) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                this.addWorld(world);
            } else {
                MVILog.warning("");
            }
        }
        if (data.contains("shares")) {
            List<String> sharesList = data.getStringList("shares");
            if (sharesList != null) {
                this.setShares(new SimpleShares(sharesList));
            } else {
                MVILog.warning("Shares formatted incorrectly for group: " + name);
            }
        }
        /*
        if (data.contains("blacklist")) {

        }
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(ConfigurationSection groupData) {
        groupData.set("worlds", Lists.newArrayList(this.getWorlds()));
        List<String> sharesList = this.getShares().toStringList();
        if (!sharesList.isEmpty()) {
            groupData.set("shares", sharesList);
        }
        /*
        if (!this.getItemBlacklist().isEmpty()) {

        }
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(String worldName) {
        this.worlds.add(worldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(World world) {
        this.worlds.add(world.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashSet<String> getWorlds() {
        return this.worlds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShares(Shares shares) {
        this.shares = shares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getShares() {
        return this.shares;
    }

    /*
    protected HashMap<String, ItemBlacklist> getItemBlacklist() {
        return this.itemBlacklist;
    }
    */

    /*
    @Override
    public ItemBlacklist getItemBlacklist(String worldName) {

        return null;
    }
    */
}
