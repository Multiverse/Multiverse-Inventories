package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.share.ProfileEntry;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
class DefaultPlayerProfile implements PlayerProfile {

    private Map<Sharable, Object> data = new HashMap<Sharable, Object>();

    private ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];
    private ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];
    private Integer health = PlayerStats.HEALTH;
    private Float exp = PlayerStats.EXPERIENCE;
    private Integer totalExperience = PlayerStats.TOTAL_EXPERIENCE;
    private Integer level = PlayerStats.LEVEL;
    private Integer foodLevel = PlayerStats.FOOD_LEVEL;
    private Float exhaustion = PlayerStats.EXHAUSTION;
    private Float saturation = PlayerStats.SATURATION;
    private Location bedSpawnLocation = null;

    private OfflinePlayer player;
    private ProfileType type;
    private String containerName;

    public DefaultPlayerProfile(ProfileType type, String containerName, OfflinePlayer player) {
        this.type = type;
        this.containerName = containerName;
        this.player = player;
        armorContents = MinecraftTools.fillWithAir(armorContents);
        inventoryContents = MinecraftTools.fillWithAir(inventoryContents);
    }

    public DefaultPlayerProfile(ProfileType type, String containerName, String playerName, Map<String, Object> playerData) {
        this(type, containerName, Bukkit.getOfflinePlayer(playerName));
        for (String key : playerData.keySet()) {
            if (key.equalsIgnoreCase("stats")) {
                this.parsePlayerStats(playerData.get(key).toString());
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
                    this.data.put(sharable, sharable.deserialize(playerData.get(key).toString()));
                } catch (Exception e) {
                    Logging.fine("Could not parse fileTag: '" + key + "' with value '" + playerData.get(key).toString() + "'");
                    Logging.fine(e.getMessage());
                }
            }
        }
        Logging.finer("Created player profile from map for '" + playerName + "'.");
    }

    /**
     * @param stats Parses these values to fill out this Profile.
     */
    protected void parsePlayerStats(String stats) {
        String[] statsArray = stats.split(DataStrings.GENERAL_DELIMITER);
        for (String stat : statsArray) {
            try {
                String[] statValues = DataStrings.splitEntry(stat);
                Sharable sharable = ProfileEntry.lookup(true, statValues[0]);
                this.data.put(sharable, sharable.deserialize(statValues[1]));
            } catch (Exception e) {
                if (!stat.isEmpty()) {
                    Logging.fine("Could not parse stat: '" + stat + "'");
                    Logging.fine(e.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> playerData = new LinkedHashMap<String, Object>();
        StringBuilder statBuilder = new StringBuilder();
        for (Map.Entry<Sharable, Object> entry : this.data.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getKey().getProfileEntry() == null) {
                    // This would mean the sharable is not intended for saving in profile files.
                    continue;
                }
                if (entry.getKey().getProfileEntry().isStat()) {
                    if (!statBuilder.toString().isEmpty()) {
                        statBuilder.append(DataStrings.GENERAL_DELIMITER);
                    }
                    statBuilder.append(DataStrings.createEntry(entry.getKey().getProfileEntry().getFileTag(),
                            entry.getKey().serialize(entry.getValue())));
                } else {

                    playerData.put(entry.getKey().getProfileEntry().getFileTag(), entry.getKey().serialize(entry.getValue()));
                }
            }
        }
        playerData.put("stats", statBuilder.toString());
        return playerData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileType getType() {
        return this.type;
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
        return sharable.getType().cast(this.data.get(sharable));
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, value);
    }

}

