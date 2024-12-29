package org.mvplugins.multiverse.inventories;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.ProfileEntry;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.SharableEntry;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.GlobalProfile;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileType;
import net.minidev.json.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class FlatFileProfileDataSource implements ProfileDataSource {

    private static final String JSON = ".json";

    private final JSONParser JSON_PARSER = new JSONParser();

    private final ExecutorService fileIOExecutorService = Executors.newSingleThreadExecutor();

    // TODO these probably need configurable max sizes
    private final Cache<ProfileKey, PlayerProfile> profileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    private final Cache<UUID, GlobalProfile> globalProfileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

    private final File worldFolder;
    private final File groupFolder;
    private final File playerFolder;

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
    }

    private FileConfiguration waitForConfigHandle(File file) {
        Future<FileConfiguration> future = fileIOExecutorService.submit(new ConfigLoader(file));
        while (true) {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private static FileConfiguration getConfigHandleNow(File file) {
        return JsonConfiguration.loadConfiguration(file);
    }

    private static class ConfigLoader implements Callable<FileConfiguration> {
        private final File file;

        private ConfigLoader(File file) {
            this.file = file;
        }

        @Override
        public FileConfiguration call() throws Exception {
            return getConfigHandleNow(file);
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
        Logging.finer("got data file: %s. Type: %s, DataName: %s, PlayerName: %s",
                jsonPlayerFile.getPath(), type, dataName, playerName);
        return jsonPlayerFile;
    }

    /**
     * Retrieves the data file for a player for their global data, creating it if necessary.
     *
     * @param fileName The name of the file (player name or UUID) without extension.
     * @param createIfMissing If true, the file will be created it it does not exist.
     * @return The data file for a player.
     * @throws IOException if there was a problem creating the file.
     */
    File getGlobalFile(String fileName, boolean createIfMissing) throws IOException {
        File jsonPlayerFile = new File(playerFolder, fileName + JSON);
        if (createIfMissing && !jsonPlayerFile.exists()) {
            try {
                jsonPlayerFile.createNewFile();
            } catch (IOException e) {
                throw new IOException("Could not create necessary player file: " + jsonPlayerFile.getPath() + ". "
                        + "There may be issues with " + fileName + "'s metadata", e);
            }
        }
        return jsonPlayerFile;
    }

    private void queueWrite(PlayerProfile profile) {
        fileIOExecutorService.submit(new FileWriter(profile.clone()));
    }

    private class FileWriter implements Callable<Void> {
        private final PlayerProfile profile;

        private FileWriter(PlayerProfile profile) {
            this.profile = profile;
        }

        @Override
        public Void call() throws Exception {
            processProfileWrite(profile);
            return null;
        }
    }

    private void processProfileWrite(PlayerProfile playerProfile) {
        try {
            File playerFile = this.getPlayerFile(playerProfile.getContainerType(),
                    playerProfile.getContainerName(), playerProfile.getPlayer().getName());
            FileConfiguration playerData = getConfigHandleNow(playerFile);
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
        queueWrite(playerProfile);
    }

    private PlayerProfile getPlayerData(ProfileKey key) {
        PlayerProfile cached = profileCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        File playerFile = null;
        try {
            playerFile = getPlayerFile(key.getContainerType(), key.getDataName(), key.getPlayerName());
        } catch (IOException e) {
            e.printStackTrace();
            // Return an empty profile
            return PlayerProfile.createPlayerProfile(key.getContainerType(), key.getDataName(), key.getProfileType(),
                    Bukkit.getOfflinePlayer(key.getPlayerUUID()));
        }
        FileConfiguration playerData = this.waitForConfigHandle(playerFile);
        if (convertConfig(playerData)) {
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not save data for player: " + key.getPlayerName()
                        + " for " + key.getContainerType().toString() + ": " + key.getDataName() + " after conversion.");
                Logging.severe(e.getMessage());
            }
        }
        ConfigurationSection section = playerData.getConfigurationSection(key.getProfileType().getName());
        if (section == null) {
            section = playerData.createSection(key.getProfileType().getName());
        }
        PlayerProfile result = deserializePlayerProfile(key, convertSection(section));
        profileCache.put(key, result);
        return result;
    }

    @Override
    public PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID) {
        return getPlayerData(ProfileKey.createProfileKey(containerType, dataName, profileType, playerUUID));
    }

    private PlayerProfile deserializePlayerProfile(ProfileKey pKey, Map playerData) {
        PlayerProfile profile = PlayerProfile.createPlayerProfile(pKey.getContainerType(), pKey.getDataName(),
                pKey.getProfileType(), Bukkit.getOfflinePlayer(pKey.getPlayerUUID()));
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
                        Logging.warning("Could not parse stats for " + pKey.getPlayerName());
                    }
                }
            } else {
                if (playerData.get(key) == null) {
                    Logging.fine("Player data '" + key + "' is null for: " + pKey.getPlayerName());
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
        Logging.finer("Created player profile from map for '" + pKey.getPlayerName() + "'.");
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
            FileConfiguration playerData = this.waitForConfigHandle(playerFile);
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

    @Override
    @Deprecated
    public GlobalProfile getGlobalProfile(String playerName) {
        return getGlobalProfile(playerName, Bukkit.getOfflinePlayer(playerName).getUniqueId());
    }

    @Override
    public GlobalProfile getGlobalProfile(String playerName, UUID playerUUID) {
        GlobalProfile cached = globalProfileCache.getIfPresent(playerUUID);
        if (cached != null) {
            return cached;
        }
        File playerFile;

        // Migrate old data if necessary
        try {
            playerFile = getGlobalFile(playerName, false);
        } catch (IOException e) {
            // This won't ever happen
            e.printStackTrace();
            return GlobalProfile.createGlobalProfile(playerName);
        }
        if (playerFile.exists()) {
            GlobalProfile profile = loadGlobalProfile(playerFile, playerName, playerUUID);
            if (!migrateGlobalProfileToUUID(profile, playerFile)) {
                Logging.warning("Could not properly migrate player global data file for " + playerName);
            }
            globalProfileCache.put(playerUUID, profile);
            return profile;
        }

        // Load current format
        try {
            playerFile = getGlobalFile(playerUUID.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
            return GlobalProfile.createGlobalProfile(playerName, playerUUID);
        }
        GlobalProfile profile = loadGlobalProfile(playerFile, playerName, playerUUID);
        globalProfileCache.put(playerUUID, profile);
        return profile;
    }

    private boolean migrateGlobalProfileToUUID(GlobalProfile profile, File playerFile) {
        updateGlobalProfile(profile);
        return playerFile.delete();
    }

    private GlobalProfile loadGlobalProfile(File playerFile, String playerName, UUID playerUUID) {
        FileConfiguration playerData = this.waitForConfigHandle(playerFile);
        ConfigurationSection section = playerData.getConfigurationSection("playerData");
        if (section == null) {
            section = playerData.createSection("playerData");
        }
        return deserializeGlobalProfile(playerName, playerUUID, convertSection(section));
    }

    private GlobalProfile deserializeGlobalProfile(String playerName, UUID playerUUID,
                                                   Map<String, Object> playerData) {
        GlobalProfile globalProfile = GlobalProfile.createGlobalProfile(playerName, playerUUID);
        for (String key : playerData.keySet()) {
            if (key.equalsIgnoreCase(DataStrings.PLAYER_LAST_WORLD)) {
                globalProfile.setLastWorld(playerData.get(key).toString());
            } else if (key.equalsIgnoreCase(DataStrings.PLAYER_SHOULD_LOAD)) {
                globalProfile.setLoadOnLogin(Boolean.valueOf(playerData.get(key).toString()));
            } else if (key.equalsIgnoreCase(DataStrings.PLAYER_LAST_KNOWN_NAME)) {
                globalProfile.setLastKnownName(playerData.get(key).toString());
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
            playerFile = this.getGlobalFile(globalProfile.getPlayerUUID().toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        FileConfiguration playerData = this.waitForConfigHandle(playerFile);
        playerData.createSection("playerData", serializeGlobalProfile(globalProfile));
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save global data for player: " + globalProfile);
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
        result.put(DataStrings.PLAYER_LAST_KNOWN_NAME, profile.getLastKnownName());
        return result;
    }

    @Override
    @Deprecated
    // TODO replace for UUID
    public void updateLastWorld(String playerName, String worldName) {
        GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setLastWorld(worldName);
        updateGlobalProfile(globalProfile);
    }

    @Override
    @Deprecated
    // TODO replace for UUID
    public void setLoadOnLogin(final String playerName, final boolean loadOnLogin) {
        final GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setLoadOnLogin(loadOnLogin);
        updateGlobalProfile(globalProfile);
    }

    @Override
    public void migratePlayerData(String oldName, String newName, UUID uuid, boolean removeOldData) throws IOException {
        File[] worldFolders = worldFolder.listFiles(File::isDirectory);
        if (worldFolders == null) {
            throw new IOException("Could not enumerate world folders");
        }
        File[] groupFolders = groupFolder.listFiles(File::isDirectory);
        if (groupFolders == null) {
            throw new IOException("Could not enumerate group folders");
        }

        for (File worldFolder : worldFolders) {
            ProfileKey key = ProfileKey.createProfileKey(ContainerType.WORLD, worldFolder.getName(),
                    ProfileTypes.ADVENTURE, uuid, oldName);
            updatePlayerData(getPlayerData(key));
            updatePlayerData(getPlayerData(ProfileKey.createProfileKey(key, ProfileTypes.CREATIVE)));
            updatePlayerData(getPlayerData(ProfileKey.createProfileKey(key, ProfileTypes.SURVIVAL)));
        }

        for (File groupFolder : groupFolders) {
            ProfileKey key = ProfileKey.createProfileKey(ContainerType.GROUP, groupFolder.getName(),
                    ProfileTypes.ADVENTURE, uuid, oldName);
            updatePlayerData(getPlayerData(key));
            updatePlayerData(getPlayerData(ProfileKey.createProfileKey(key, ProfileTypes.CREATIVE)));
            updatePlayerData(getPlayerData(ProfileKey.createProfileKey(key, ProfileTypes.SURVIVAL)));
        }

        if (removeOldData) {
            for (File worldFolder : worldFolders) {
                removePlayerData(ContainerType.WORLD, worldFolder.getName(), ProfileTypes.ADVENTURE, oldName);
                removePlayerData(ContainerType.WORLD, worldFolder.getName(), ProfileTypes.CREATIVE, oldName);
                removePlayerData(ContainerType.WORLD, worldFolder.getName(), ProfileTypes.SURVIVAL, oldName);
            }
            for (File groupFolder : groupFolders) {
                removePlayerData(ContainerType.GROUP, groupFolder.getName(), ProfileTypes.ADVENTURE, oldName);
                removePlayerData(ContainerType.GROUP, groupFolder.getName(), ProfileTypes.CREATIVE, oldName);
                removePlayerData(ContainerType.GROUP, groupFolder.getName(), ProfileTypes.SURVIVAL, oldName);
            }
        }
    }

    @Override
    public void clearProfileCache(ProfileKey key) {
        profileCache.invalidate(key);
    }

    void clearCache() {
        globalProfileCache.invalidateAll();
        profileCache.invalidateAll();
    }
}

