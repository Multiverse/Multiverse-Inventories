package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.api.share.ProfileEntry;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
class DefaultPlayerProfile implements PlayerProfile {

    //private Map<Sharable, Object> data = new HashMap<Sharable, Object>();
    private Map<ProfileType, Map<Sharable, Object>> data = new HashMap<ProfileType, Map<Sharable, Object>>();

    private ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];
    private ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];

    private OfflinePlayer player;
    private ContainerType type;
    private String containerName;

    public DefaultPlayerProfile(ContainerType type, String containerName, OfflinePlayer player) {
        this.type = type;
        this.containerName = containerName;
        this.player = player;
        armorContents = MinecraftTools.fillWithAir(armorContents);
        inventoryContents = MinecraftTools.fillWithAir(inventoryContents);
    }

    public DefaultPlayerProfile(ContainerType type, String containerName, String playerName, Map<String, Object> playerData) {
        this(type, containerName, Bukkit.getOfflinePlayer(playerName));
        if (!playerData.isEmpty() && !playerData.containsKey(ProfileType.DEFAULT.getName())) {
            Map<String, Object> dataBackup = new HashMap<String, Object>(playerData);
            playerData = new HashMap<String, Object>(1);
            playerData.put(ProfileType.DEFAULT.getName(), dataBackup);
            Logging.finer("Migrated old player data to new multi-profile format");
        }
        for (String profileTypeKey : playerData.keySet()) {
            Object profileSection = playerData.get(profileTypeKey);
            if (!(profileSection instanceof Map)) {
                Logging.fine("Profile section '" + profileTypeKey + "' not valid format!");
                continue;
            }
            ProfileType profileType = ProfileTypes.lookupType(profileTypeKey, true);
            Map profileSectionMap = (Map) profileSection;
            for (Object key : profileSectionMap.keySet()) {
                if (key.toString().equalsIgnoreCase(DataStrings.PLAYER_STATS)) {
                    this.parsePlayerStats(profileType, profileSectionMap.get(key).toString());
                } else {
                    if (profileSectionMap.get(key) == null) {
                        Logging.fine("Player data '" + key + "' is null for: " + playerName);
                        continue;
                    }
                    try {
                        Sharable sharable = ProfileEntry.lookup(false, key.toString());
                        if (sharable == null) {
                            Logging.fine("Player fileTag '" + key + "' is unrecognized!");
                            continue;
                        }
                        Map<Sharable, Object> profileData = this.getDataForType(profileType);
                        profileData.put(sharable, sharable.getSerializer().deserialize(profileSectionMap.get(key).toString()));
                    } catch (Exception e) {
                        Logging.fine("Could not parse fileTag: '" + key + "' with value '" + profileSectionMap.get(key).toString() + "'");
                        Logging.fine(e.getMessage());
                    }
                }
            }
        }
        Logging.finer("Created player profile from map for '" + playerName + "'.");
    }

    private Map<Sharable, Object> getDataForType(ProfileType type) {
        Map<Sharable, Object> profileData = this.data.get(type);
        if (profileData == null) {
            profileData = new HashMap<Sharable, Object>();
            this.data.put(type, profileData);
        }
        return profileData;
    }

    /**
     * @param stats Parses these values to fill out this Profile.
     */
    protected void parsePlayerStats(ProfileType profileType, String stats) {
        String[] statsArray = stats.split(DataStrings.GENERAL_DELIMITER);
        for (String stat : statsArray) {
            try {
                String[] statValues = DataStrings.splitEntry(stat);
                Sharable sharable = ProfileEntry.lookup(true, statValues[0]);
                this.getDataForType(profileType).put(sharable, sharable.getSerializer().deserialize(statValues[1]));
            } catch (Exception e) {
                Logging.warning("Could not parse stat: '" + stat + "' for player '" + getPlayer().getName() + "' for "
                        + getType() + " '" + getContainerName() + "'");
                Logging.warning("Exception: " + e.getClass() + " Message: " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        for (Map.Entry<ProfileType, Map<Sharable, Object>> typeEntry : this.data.entrySet()) {
            StringBuilder statBuilder = new StringBuilder();
            Map<String, Object> playerData = new HashMap<String, Object>();
            for (Map.Entry<Sharable, Object> entry : typeEntry.getValue().entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getKey().getSerializer() == null) {
                        continue;
                    }
                    Sharable sharable = entry.getKey();
                    if (sharable.getProfileEntry().isStat()) {
                        if (!statBuilder.toString().isEmpty()) {
                            statBuilder.append(DataStrings.GENERAL_DELIMITER);
                        }
                        statBuilder.append(DataStrings.createEntry(sharable.getProfileEntry().getFileTag(),
                                sharable.getSerializer().serialize(entry.getValue())));
                    } else {
                        playerData.put(sharable.getProfileEntry().getFileTag(),
                                sharable.getSerializer().serialize(entry.getValue()));
                    }
                }
            }
            playerData.put(DataStrings.PLAYER_STATS, statBuilder.toString());
            dataMap.put(typeEntry.getKey().getName(), playerData);
        }
        return dataMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getType() {
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
        return this.get(ProfileType.DEFAULT, sharable);
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        this.set(ProfileType.DEFAULT, sharable, value);
    }

    @Override
    public <T> T get(ProfileType profileType, Sharable<T> sharable) {
        return sharable.getType().cast(this.getDataForType(profileType).get(sharable));
    }

    @Override
    public <T> void set(ProfileType profileType, Sharable<T> sharable, T value) {
        this.getDataForType(profileType).put(sharable, value);
    }

}

