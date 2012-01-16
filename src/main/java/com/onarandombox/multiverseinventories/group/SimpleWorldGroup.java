package com.onarandombox.multiverseinventories.group;

import com.google.common.collect.Lists;
import com.onarandombox.multiverseinventories.group.blacklist.ItemBlacklist;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public SimpleWorldGroup(String name, Map<String, Object> data) throws DeserializationException {
        if (!data.containsKey("worlds")) {
            throw new DeserializationException("No worlds specified for world group: " + name);
        }
        Object worldListObj = data.get("worlds");
        if (!(worldListObj instanceof List)) {
            throw new DeserializationException("World list formatted incorrectly for world group: " + name);
        }
        for (Object worldNameObj : (List) worldListObj) {
            World world = Bukkit.getWorld(worldNameObj.toString());
            if (world != null) {
                this.addWorld(world);
            } else {
                MVILog.warning("");
            }
        }
        if (data.containsKey("shares")) {
            Object sharesListObj = data.get("shares");
            if (sharesListObj instanceof List) {
                this.setShares(new SimpleShares((List) sharesListObj));
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
    public Map<String, Object> serialize() {
        Map<String, Object> results = new LinkedHashMap<String, Object>();
        results.put("worlds", Lists.newArrayList(this.getWorlds()));
        List<String> sharesList = this.getShares().toStringList();
        if (!sharesList.isEmpty()) {
            results.put("shares", sharesList);
        }
        /*
        if (!this.getItemBlacklist().isEmpty()) {

        }
        */
        return results;
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
