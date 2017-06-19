package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.collect.Lists;
import com.onarandombox.multiverseinventories.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.DeserializationException;
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
    static final String DEFAULT_GROUP_NAME = "default";

    private final String name;
    private final HashSet<String> worlds = new HashSet<String>();
    private final Shares shares = Sharables.noneOf();

    private String spawnWorld = null;
    private EventPriority spawnPriority = EventPriority.NORMAL;

    public DefaultWorldGroupProfile(final MultiverseInventories inventories, final String name) {
        super(inventories, ContainerType.GROUP);
        this.name = name;
    }

    public DefaultWorldGroupProfile(final MultiverseInventories inventories, final String name,
                                    final Map<String, Object> dataMap) throws DeserializationException {
        this(inventories, name);
        if (dataMap.containsKey("worlds")) {
            Object worldListObj = dataMap.get("worlds");
            if (worldListObj == null) {
                Logging.fine("No worlds for group: " + name);
            } else {
                if (!(worldListObj instanceof List)) {
                    Logging.fine("World list formatted incorrectly for world group: " + name);
                } else {
                    final StringBuilder builder = new StringBuilder();
                    for (Object worldNameObj : (List) worldListObj) {
                        if (worldNameObj == null) {
                            Logging.fine("Error with a world listed in group: " + name);
                            continue;
                        }
                        this.addWorld(worldNameObj.toString(), false);
                        World world = Bukkit.getWorld(worldNameObj.toString());
                        if (world == null) {
                            if (builder.length() != 0) {
                                builder.append(", ");
                            }
                            builder.append(worldNameObj.toString());
                        }
                    }
                    if (builder.length() > 0) {
                        Logging.config("The following worlds for group '%s' are not loaded: %s", name, builder.toString());
                    }
                }
            }
        }
        if (dataMap.containsKey("shares")) {
            Object sharesListObj = dataMap.get("shares");
            if (sharesListObj instanceof List) {
                this.getShares().mergeShares(Sharables.fromList((List) sharesListObj));
                this.getShares().removeAll(Sharables.negativeFromList((List) sharesListObj));
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
    @Deprecated
    public void setName(String name) { }

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
        this.getWorlds().add(worldName.toLowerCase());
        if (updateConfig) {
            getInventories().getGroupManager().updateGroup(this);
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
        this.getWorlds().remove(worldName.toLowerCase());
        if (updateConfig) {
            getInventories().getGroupManager().updateGroup(this);
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

    @Override
    public boolean isSharing(Sharable sharable) {
        return getShares().isSharing(sharable);
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
        return this.getWorlds().contains(worldName.toLowerCase());
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
        this.spawnWorld = worldName.toLowerCase();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefault() {
        return DEFAULT_GROUP_NAME.equals(getName());
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

