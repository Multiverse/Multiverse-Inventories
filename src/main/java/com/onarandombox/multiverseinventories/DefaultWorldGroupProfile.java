package com.onarandombox.multiverseinventories;

import com.google.common.collect.Lists;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of WorldGroupProfile.
 */
class DefaultWorldGroupProfile extends WeakProfileContainer implements WorldGroupProfile {

    private String name = "";
    private String spawnWorld = null;
    private EventPriority spawnPriority = EventPriority.NORMAL;
    private HashSet<String> worlds = new HashSet<String>();
    private Shares shares = Sharables.noneOf();
    private Shares negativeShares = Sharables.noneOf();
    //private Set<ProfileType> profileTypes = new LinkedHashSet<ProfileType>();

    public DefaultWorldGroupProfile(Inventories inventories, String name) {
        super(inventories, ContainerType.GROUP);
        this.name = name;
    }

    public DefaultWorldGroupProfile(Inventories inventories, String name,
                                    Map<String, Object> dataMap) throws DeserializationException {
        this(inventories, name);
        if (dataMap.containsKey("worlds")) {
            Object worldListObj = dataMap.get("worlds");
            if (worldListObj == null) {
                Logging.fine("No worlds for group: " + name);
            } else {
                if (!(worldListObj instanceof List)) {
                    Logging.fine("World list formatted incorrectly for world group: " + name);
                } else {
                    for (Object worldNameObj : (List) worldListObj) {
                        if (worldNameObj == null) {
                            Logging.fine("Error with a world listed in group: " + name);
                            continue;
                        }
                        this.addWorld(worldNameObj.toString(), false);
                        World world = Bukkit.getWorld(worldNameObj.toString());
                        if (world == null) {
                            Logging.warning("World: " + worldNameObj.toString() + " is not loaded.");
                        }
                    }
                }
            }
        }
        if (dataMap.containsKey("shares")) {
            Object sharesListObj = dataMap.get("shares");
            if (sharesListObj instanceof List) {
                this.getShares().mergeShares(Sharables.fromList((List) sharesListObj));
                this.getNegativeShares().mergeShares(Sharables.negativeFromList((List) sharesListObj));
            } else {
                Logging.warning("Shares formatted incorrectly for group: " + name);
            }
        }
        if (dataMap.containsKey("spawn")) {
            Object spawnPropsObj = dataMap.get("spawn");
            if (spawnPropsObj instanceof ConfigurationSection) {
                // Le sigh, bukkit.
                spawnPropsObj = ((ConfigurationSection) spawnPropsObj).getValues(true);
            }
            if (spawnPropsObj instanceof Map) {
                Map spawnProps = (Map) spawnPropsObj;
                if (spawnProps.containsKey("world")) {
                    this.setSpawnWorld(spawnProps.get("world").toString());
                }
                if (spawnProps.containsKey("priority")) {
                    EventPriority priority = EventPriority.valueOf(
                            spawnProps.get("priority").toString().toUpperCase());
                    if (priority != null) {
                        this.setSpawnPriority(priority);
                    }
                }
            } else {
                Logging.warning("Spawn settings for group formatted incorrectly");
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
        for (Sharable sharable : this.getNegativeShares()) {
            sharesList.add("-" + sharable.getNames()[0]);
        }
        if (!sharesList.isEmpty()) {
            results.put("shares", sharesList);
        }
        Map<String, Object> spawnProps = new LinkedHashMap<String, Object>();
        if (this.getSpawnWorld() != null) {
            spawnProps.put("world", this.getSpawnWorld());
            spawnProps.put("priority", this.getSpawnPriority().toString());
            results.put("spawn", spawnProps);
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
            this.getInventories().getMVIConfig().updateWorldGroup(this);
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
            this.getInventories().getMVIConfig().updateWorldGroup(this);
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
    public Set<String> getWorlds() {
        return this.worlds;
    }

    private void setShares(Shares shares) {
        this.shares = shares;
    }

    @Override
    public boolean isSharing(Sharable sharable) {
        return getShares().isSharing(sharable) && !getNegativeShares().isSharing(sharable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getShares() {
        return this.shares;
    }

    private void setNegativeShares(Shares shares) {
        this.shares = shares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getNegativeShares() {
        return this.negativeShares;
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
        builder.append("], Shares: [").append(this.getShares().toString()).append("]");
        builder.append(", Negative Shares: [").append(this.getNegativeShares().toString()).append("]");
        if (this.getSpawnWorld() != null) {
            builder.append(", Spawn World: ").append(this.getSpawnWorld());
            builder.append(", Spawn Priority: ").append(this.getSpawnPriority().toString());
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpawnWorld() {
        return this.spawnWorld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnWorld(String worldName) {
        this.spawnWorld = worldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventPriority getSpawnPriority() {
        return this.spawnPriority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnPriority(EventPriority priority) {
        this.spawnPriority = priority;
    }

    /*
    @Override
    public Set<ProfileType> getProfileTypes() {
        return this.profileTypes;
    }
    */

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

