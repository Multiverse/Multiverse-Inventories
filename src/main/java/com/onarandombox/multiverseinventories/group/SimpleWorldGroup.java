package com.onarandombox.multiverseinventories.group;

import com.google.common.collect.Lists;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.WeakProfileContainer;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of WorldGroup.
 */
public class SimpleWorldGroup extends WeakProfileContainer implements WorldGroup {

    private String name = "";
    private HashSet<String> worlds = new HashSet<String>();
    private Shares shares = new SimpleShares();
    //private HashMap<String, ItemBlacklist> itemBlacklist = new HashMap<String, ItemBlacklist>();

    public SimpleWorldGroup(MultiverseInventories plugin, String name) {
        super(plugin, ProfileType.GROUP);
        this.name = name;
    }

    public SimpleWorldGroup(MultiverseInventories plugin, String name,
                            Map<String, Object> dataMap) throws DeserializationException {
        this(plugin, name);
        if (!dataMap.containsKey("worlds")) {
            throw new DeserializationException("No worlds specified for world group: " + name);
        }
        Object worldListObj = dataMap.get("worlds");
        if (!(worldListObj instanceof List)) {
            throw new DeserializationException("World list formatted incorrectly for world group: " + name);
        }
        for (Object worldNameObj : (List) worldListObj) {
            this.addWorld(worldNameObj.toString(), false);
            World world = Bukkit.getWorld(worldNameObj.toString());
            if (world == null) {
                MVILog.debug("World: " + worldNameObj.toString() + " is not loaded.");
            }
        }
        if (dataMap.containsKey("shares")) {
            Object sharesListObj = dataMap.get("shares");
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
        this.addWorld(worldName, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(String worldName, boolean updateConfig) {
        this.getWorlds().add(worldName);
        if (updateConfig) {
            this.getPlugin().getSettings().updateWorldGroup(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(World world) {
        this.addWorld(world.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorld(String worldName) {
        this.removeWorld(worldName, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorld(String worldName, boolean updateConfig) {
        this.getWorlds().remove(worldName);
        if (updateConfig) {
            this.getPlugin().getSettings().updateWorldGroup(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorld(World world) {
        this.removeWorld(world.getName());
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
        this.getPlugin().getSettings().updateWorldGroup(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getShares() {
        return this.shares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsWorld(String worldName) {
        return this.getWorlds().contains(worldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataName() {
        return this.getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getName()).append(": {Worlds: [");
        String[] worldsString = this.getWorlds().toArray(new String[this.getWorlds().size()]);
        for (int i = 0; i < worldsString.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(worldsString[i]);
        }
        builder.append("], Shares: [").append(this.getShares().toString()).append("]}");
        return builder.toString();
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

