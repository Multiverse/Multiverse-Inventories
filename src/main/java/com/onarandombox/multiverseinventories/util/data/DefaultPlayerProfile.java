package com.onarandombox.multiverseinventories.util.data;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.share.SharableEntry;
import com.onarandombox.multiverseinventories.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.ProfileEntry;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Default implementation of a player profile, that is, the data per world/group/gamemode.
 */
class DefaultPlayerProfile implements PlayerProfile {

    private static final JSONParser JSON_PARSER = new JSONParser();

    private Map<Sharable, SharableEntry> data = new HashMap<Sharable, SharableEntry>();

    private ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];
    private ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];

    private OfflinePlayer player;
    private ContainerType containerType;
    private String containerName;
    private ProfileType profileType;

    DefaultPlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, OfflinePlayer player) {
        this.containerType = containerType;
        this.profileType = profileType;
        this.containerName = containerName;
        this.player = player;
        armorContents = MinecraftTools.fillWithAir(armorContents);
        inventoryContents = MinecraftTools.fillWithAir(inventoryContents);
    }

    public DefaultPlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, String playerName, Map playerData) {
        this(containerType, containerName, profileType, Bukkit.getOfflinePlayer(playerName));
        for (Object keyObj : playerData.keySet()) {
            String key = keyObj.toString();
            if (key.equalsIgnoreCase(DataStrings.PLAYER_STATS)) {
                final Object statsObject = playerData.get(key);
                if (statsObject instanceof String) {
                    this.parsePlayerStats(statsObject.toString());
                } else {
                    if (statsObject instanceof Map) {
                        parsePlayerStats((Map) statsObject);
                    } else {
                        Logging.warning("Could not parse stats for " + playerName);
                    }
                }
            } else {
                if (playerData.get(key) == null) {
                    Logging.fine("Player data '" + key + "' is null for: " + playerName);
                    continue;
                }
                try {
                    Sharable sharable = ProfileEntry.lookup(false, key);
                    if (sharable == null) {
                        Logging.fine("Player fileTag '" + key + "' is unrecognized!");
                        continue;
                    }
                    this.data.put(sharable, new SharableEntry(sharable, sharable.getSerializer().deserialize(playerData.get(key))));
                } catch (Exception e) {
                    Logging.fine("Could not parse fileTag: '" + key + "' with value '" + playerData.get(key) + "'");
                    Logging.getLogger().log(Level.FINE, "Exception: ", e);
                    e.printStackTrace();
                }
            }
        }
        Logging.finer("Created player profile from map for '" + playerName + "'.");
    }

    /**
     * @param stats Parses these values to fill out this Profile.
     */
    protected void parsePlayerStats(String stats) {
        if (stats.isEmpty()) {
            return;
        }
        jsonParsePlayerStats(stats);
    }

    protected void parsePlayerStats(final Map stats) {
        for (Object key : stats.keySet()) {
            Sharable sharable = ProfileEntry.lookup(true, key.toString());
            if (sharable != null) {
                this.data.put(sharable, new SharableEntry(sharable, sharable.getSerializer().deserialize(stats.get(key).toString())));
            } else {
                Logging.warning("Could not parse stat: '" + key + "' for player '" + getPlayer().getName() + "' for "
                        + getContainerType() + " '" + getContainerName() + "'");
            }
        }
    }

    private void jsonParsePlayerStats(String stats) {
        JSONObject jsonStats = null;
        try {
            jsonStats = (JSONObject) JSON_PARSER.parse(stats);
        } catch (ParseException e) {
            Logging.warning("Could not parse stats for player'" + getPlayer().getName() + "' for " +
                    getContainerType() + " '" + getContainerName() + "': " + e.getMessage());
        } catch (ClassCastException e) {
            Logging.warning("Could not parse stats for player'" + getPlayer().getName() + "' for " +
                    getContainerType() + " '" + getContainerName() + "': " + e.getMessage());
        }
        if (jsonStats == null) {
            Logging.warning("Could not parse stats for player'" + getPlayer().getName() + "' for " +
                    getContainerType() + " '" + getContainerName() + "'");
            return;
        }
        parsePlayerStats(jsonStats);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getContainerType() {
        return this.containerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public <T> T get(Sharable<T> sharable) {
        SharableEntry entry = this.data.get(sharable);
        return sharable.getType().cast(entry != null ? entry.getValue() : null);
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, new SharableEntry<T>(sharable, value));
    }

    @Override
    public ProfileType getProfileType() {
        return this.profileType;
    }

    @Override
    public PlayerProfile clone() throws CloneNotSupportedException {
        return (PlayerProfile) super.clone();
    }

    @Override
    public Iterator<SharableEntry> iterator() {
        return new SharablesIterator(data.values().iterator());
    }

    private static class SharablesIterator implements Iterator<SharableEntry> {

        private final Iterator<SharableEntry> backingIterator;

        private SharablesIterator(Iterator<SharableEntry> backingIterator) {
            this.backingIterator = backingIterator;
        }

        @Override
        public boolean hasNext() {
            return backingIterator.hasNext();
        }

        @Override
        public SharableEntry next() {
            return backingIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

