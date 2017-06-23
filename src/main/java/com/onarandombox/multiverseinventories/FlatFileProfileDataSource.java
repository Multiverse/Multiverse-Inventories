package com.onarandombox.multiverseinventories;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.ProfileDataSource;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.share.ProfileEntry;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.SharableEntry;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import net.minidev.json.JSONObject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

class FlatFileProfileDataSource implements ProfileDataSource {

    private static final String JSON = ".json";

    private final JSONParser JSON_PARSER = new JSONParser();

    private final File worldFolder;
    private final File groupFolder;
    private final File playerFolder;
    private final FileWriteThread fileWriteThread;

    FlatFileProfileDataSource(MultiverseInventories plugin) throws IOException {
        // Make the data folders
        plugin.getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.worldFolder = new File(plugin.getDataFolder(), "worlds");
        if (!this.worldFolder.exists()) {
            if (!this.worldFolder.mkdirs()) {
                throw new IOException("Could not create world folder!");
            }
        }
        this.groupFolder = new File(plugin.getDataFolder(), "groups");
        if (!this.groupFolder.exists()) {
            if (!this.groupFolder.mkdirs()) {
                throw new IOException("Could not create group folder!");
            }
        }
        this.playerFolder = new File(plugin.getDataFolder(), "players");
        if (!this.playerFolder.exists()) {
            if (!this.playerFolder.mkdirs()) {
                throw new IOException("Could not create player folder!");
            }
        }
        fileWriteThread = new FileWriteThread();
        fileWriteThread.start();
    }

    private FileConfiguration getConfigHandle(File file) {
        try {
            return JsonConfiguration.loadConfiguration(file, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return JsonConfiguration.loadConfiguration(file);
        }
    }

    private File getFolder(ContainerType type, String folderName) {
        File folder;
        switch (type) {
            case GROUP:
                folder = new File(this.groupFolder, folderName);
                break;
            case WORLD:
                folder = new File(this.worldFolder, folderName);
                break;
            default:
                folder = new File(this.worldFolder, folderName);
                break;
        }

        if (!folder.exists()) {
            folder.mkdirs();
        }
        Logging.finer("got data folder: " + folder.getPath() + " from type: " + type);
        return folder;
    }

    /**
     * Retrieves the data file for a player based on a given world/group name, creating it if necessary.
     *
     * @param type       Indicates whether data is for group or world.
     * @param dataName   The name of the group or world.
     * @param playerName The name of the player.
     * @return The data file for a player.
     * @throws IOException if there was a problem creating the file.
     */
    File getPlayerFile(ContainerType type, String dataName, String playerName) throws IOException {
        File jsonPlayerFile = new File(this.getFolder(type, dataName), playerName + JSON);
        if (!jsonPlayerFile.exists()) {
            try {
                jsonPlayerFile.createNewFile();
            } catch (IOException e) {
                throw new IOException("Could not create necessary player data file: " + jsonPlayerFile.getPath()
                        + ". Data for " + playerName + " in " + type.name().toLowerCase() + " " + dataName
                        + " may not be saved.", e);
            }
        }
        return jsonPlayerFile;
    }

    /**
     * Retrieves the data file for a player for their global data, creating it if necessary.
     *
     * @param playerName The name of the player.
     * @return The data file for a player.
     * @throws IOException if there was a problem creating the file.
     */
    File getGlobalFile(String playerName) throws IOException {
        File jsonPlayerFile = new File(playerFolder, playerName + JSON);
        if (!jsonPlayerFile.exists()) {
            try {
                jsonPlayerFile.createNewFile();
            } catch (IOException e) {
                throw new IOException("Could not create necessary player file: " + jsonPlayerFile.getPath() + ". "
                        + "There may be issues with " + playerName + "'s metadata", e);
            }
        }
        return jsonPlayerFile;
    }

    private class FileWriteThread extends Thread {

        FileWriteThread() {
            super("MV-Inv Profile Write Thread");
        }

        private final BlockingQueue<PlayerProfile> profileWriteQueue = new LinkedBlockingQueue<PlayerProfile>();
        private final BlockingQueue<PlayerProfile> waitingQueue = new LinkedBlockingQueue<PlayerProfile>();
        
        private volatile boolean waiting = false;

        @Override
        public void run() {
            while(true) {
                try {
                    final PlayerProfile profile = profileWriteQueue.take();
                    processProfileWrite(profile);
                } catch (InterruptedException ignore) { }
            }
        }

        void queue(final PlayerProfile profile) {
            try {
                final PlayerProfile clonedProfile = profile.clone();
                if (waiting) {
                    waitingQueue.add(clonedProfile);
                } else {
                    profileWriteQueue.add(clonedProfile);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        void waitUntilEmpty() {
            waiting = true;
            while(!profileWriteQueue.isEmpty()) { }
            waiting = false;
            profileWriteQueue.addAll(waitingQueue);
            waitingQueue.clear();
        }
    }

    private void processProfileWrite(PlayerProfile playerProfile) {
        try {
            File playerFile = this.getPlayerFile(playerProfile.getContainerType(),
                    playerProfile.getContainerName(), playerProfile.getPlayer().getName());
            FileConfiguration playerData = this.getConfigHandle(playerFile);
            playerData.createSection(playerProfile.getProfileType().getName(), serializePlayerProfile(playerProfile));
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                        + " for " + playerProfile.getContainerType().toString() + ": " + playerProfile.getContainerName());
                Logging.severe(e.getMessage());
            }
        } catch (final Exception e) {
            Logging.getLogger().log(Level.WARNING, "Error while attempting to write profile data.", e);
        }
    }

    private Map<String, Object> serializePlayerProfile(PlayerProfile playerProfile) {
        Map<String, Object> playerData = new LinkedHashMap<String, Object>();
        JSONObject jsonStats = new JSONObject();
        for (SharableEntry entry : playerProfile) {
            if (entry.getValue() != null) {
                if (entry.getSharable().getSerializer() == null) {
                    continue;
                }
                Sharable sharable = entry.getSharable();
                if (sharable.getProfileEntry().isStat()) {
                    jsonStats.put(sharable.getProfileEntry().getFileTag(),
                            sharable.getSerializer().serialize(entry.getValue()));
                } else {
                    playerData.put(sharable.getProfileEntry().getFileTag(),
                            sharable.getSerializer().serialize(entry.getValue()));
                }
            }
        }
        if (!jsonStats.isEmpty()) {
            playerData.put(DataStrings.PLAYER_STATS, jsonStats);
        }
        return playerData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlayerData(PlayerProfile playerProfile) {
        fileWriteThread.queue(playerProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName) {
        fileWriteThread.waitUntilEmpty();
        File playerFile = null;
        try {
            playerFile = getPlayerFile(containerType, dataName, playerName);
        } catch (IOException e) {
            e.printStackTrace();
            // Return an empty profile
            return PlayerProfile.createPlayerProfile(containerType, dataName, profileType, playerName);
        }
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        if (convertConfig(playerData)) {
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not save data for player: " + playerName
                        + " for " + containerType.toString() + ": " + dataName + " after conversion.");
                Logging.severe(e.getMessage());
            }
        }
        ConfigurationSection section = playerData.getConfigurationSection(profileType.getName());
        if (section == null) {
            section = playerData.createSection(profileType.getName());
        }
        return deserializePlayerProfile(containerType, dataName, profileType, playerName, convertSection(section));
    }

    private PlayerProfile deserializePlayerProfile(ContainerType containerType, String containerName,
                                                          ProfileType profileType, String playerName, Map playerData) {
        PlayerProfile profile = PlayerProfile.createPlayerProfile(containerType, containerName,
                profileType, playerName);
        for (Object keyObj : playerData.keySet()) {
            String key = keyObj.toString();
            if (key.equalsIgnoreCase(DataStrings.PLAYER_STATS)) {
                final Object statsObject = playerData.get(key);
                if (statsObject instanceof String) {
                    parseJsonPlayerStatsIntoProfile(statsObject.toString(), profile);
                } else {
                    if (statsObject instanceof Map) {
                        parsePlayerStatsIntoProfile((Map) statsObject, profile);
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
                    profile.set(sharable, sharable.getSerializer().deserialize(playerData.get(key)));
                } catch (Exception e) {
                    Logging.fine("Could not parse fileTag: '" + key + "' with value '" + playerData.get(key) + "'");
                    Logging.getLogger().log(Level.FINE, "Exception: ", e);
                    e.printStackTrace();
                }
            }
        }
        Logging.finer("Created player profile from map for '" + playerName + "'.");
        return profile;
    }

    private void parsePlayerStatsIntoProfile(Map stats, PlayerProfile profile) {
        for (Object key : stats.keySet()) {
            Sharable sharable = ProfileEntry.lookup(true, key.toString());
            if (sharable != null) {
                profile.set(sharable, sharable.getSerializer().deserialize(stats.get(key).toString()));
            } else {
                Logging.warning("Could not parse stat: '" + key + "' for player '"
                        + profile.getPlayer().getName() + "' for " + profile.getContainerType() + " '"
                        + profile.getContainerName() + "'");
            }
        }
    }

    private void parseJsonPlayerStatsIntoProfile(String stats, PlayerProfile profile) {
        if (stats.isEmpty()) {
            return;
        }
        org.json.simple.JSONObject jsonStats = null;
        try {
            jsonStats = (org.json.simple.JSONObject) JSON_PARSER.parse(stats);
        } catch (org.json.simple.parser.ParseException e) {
            Logging.warning("Could not parse stats for player'" + profile.getPlayer().getName() + "' for " +
                    profile.getContainerType() + " '" + profile.getContainerName() + "': " + e.getMessage());
        } catch (ClassCastException e) {
            Logging.warning("Could not parse stats for player'" + profile.getPlayer().getName() + "' for " +
                    profile.getContainerType() + " '" + profile.getContainerName() + "': " + e.getMessage());
        }
        if (jsonStats == null) {
            Logging.warning("Could not parse stats for player'" + profile.getPlayer().getName() + "' for " +
                    profile.getContainerType() + " '" + profile.getContainerName() + "'");
            return;
        }
        parsePlayerStatsIntoProfile(jsonStats, profile);
    }

    // TODO Remove this conversion
    private boolean convertConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("playerData");
        if (section != null) {
            config.set(ProfileTypes.SURVIVAL.getName(), section);
            config.set(ProfileTypes.CREATIVE.getName(), section);
            config.set(ProfileTypes.ADVENTURE.getName(), section);
            config.set("playerData", null);
            Logging.finer("Migrated old player data to new multi-profile format");
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName) {
        if (profileType == null) {
            try {
                File playerFile = getPlayerFile(containerType, dataName, playerName);
                return playerFile.delete();
            } catch (IOException ignore) {
                Logging.warning("Attempted to delete file that did not exist for player " + playerName
                        + " in " + containerType.name().toLowerCase() + " " + dataName);
                return false;
            }
        } else {
            File playerFile;
            try {
                playerFile = getPlayerFile(containerType, dataName, playerName);
            } catch (IOException e) {
                Logging.warning("Attempted to delete " + playerName + "'s data for "
                        + profileType.getName().toLowerCase() + " mode in "  + containerType.name().toLowerCase()
                        + " " + dataName + " but the file did not exist.");
                return false;
            }
            FileConfiguration playerData = this.getConfigHandle(playerFile);
            playerData.set(profileType.getName(), null);
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not delete data for player: " + playerName
                        + " for " + containerType.toString() + ": " + dataName);
                Logging.severe(e.getMessage());
                return false;
            }
            return true;
        }
    }

    private Map<String, Object> convertSection(ConfigurationSection section) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (String key : section.getKeys(false)) {
            Object obj = section.get(key);
            if (obj instanceof ConfigurationSection) {
                resultMap.put(key, convertSection((ConfigurationSection) obj));
            } else {
                resultMap.put(key, obj);
            }
        }
        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalProfile getGlobalProfile(String playerName) {
        // TODO use data caching to avoid excess object creation.
        File playerFile;
        try {
            playerFile = getGlobalFile(playerName);
        } catch (IOException e) {
            e.printStackTrace();
            return GlobalProfile.createGlobalProfile(playerName);
        }
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        ConfigurationSection section = playerData.getConfigurationSection("playerData");
        if (section == null) {
            section = playerData.createSection("playerData");
        }
        return deserializeGlobalProfile(playerName, convertSection(section));
    }

    private GlobalProfile deserializeGlobalProfile(String playerName, Map<String, Object> playerData) {
        GlobalProfile globalProfile = GlobalProfile.createGlobalProfile(playerName);
        for (String key : playerData.keySet()) {
            if (key.equalsIgnoreCase(DataStrings.PLAYER_LAST_WORLD)) {
                globalProfile.setLastWorld(playerData.get(key).toString());
            } else if (key.equalsIgnoreCase(DataStrings.PLAYER_SHOULD_LOAD)) {
                globalProfile.setLoadOnLogin(Boolean.valueOf(playerData.get(key).toString()));
            }
        }
        return globalProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateGlobalProfile(GlobalProfile globalProfile) {
        File playerFile = null;
        try {
            playerFile = this.getGlobalFile(globalProfile.getPlayerName());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        playerData.createSection("playerData", serializeGlobalProfile(globalProfile));
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save global data for player: " + globalProfile.getPlayerName());
            Logging.severe(e.getMessage());
            return false;
        }
        return true;
    }

    private Map<String, Object> serializeGlobalProfile(GlobalProfile profile) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        if (profile.getLastWorld() != null) {
            result.put(DataStrings.PLAYER_LAST_WORLD, profile.getLastWorld());
        }
        result.put(DataStrings.PLAYER_SHOULD_LOAD, profile.shouldLoadOnLogin());
        return result;
    }

    @Override
    public void updateLastWorld(String playerName, String worldName) {
        GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setLastWorld(worldName);
        updateGlobalProfile(globalProfile);
    }

    @Override
    public void setLoadOnLogin(final String playerName, final boolean loadOnLogin) {
        final GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setLoadOnLogin(loadOnLogin);
        updateGlobalProfile(globalProfile);
    }
}

