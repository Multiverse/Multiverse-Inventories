package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.Sets;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.share.ProfileEntry;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import net.minidev.json.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.mvplugins.multiverse.inventories.util.DataStrings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

@Service
final class FlatFileProfileDataSource implements ProfileDataSource {

    private static final String JSON = ".json";

    private final JSONParser JSON_PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE | JSONParser.ACCEPT_TAILLING_SPACE);

    // TODO these probably need configurable max sizes
    private final Cache<ProfileKey, FileConfiguration> playerFileCache;
    private final Cache<ProfileKey, PlayerProfile> playerProfileCache;
    private final Cache<UUID, GlobalProfile> globalProfileCache;

    private final File worldFolder;
    private final File groupFolder;
    private final File playerFolder;

    private final ProfileFileIO playerProfileIO;
    private final ProfileFileIO globalProfileIO;

    @Inject
    FlatFileProfileDataSource(@NotNull MultiverseInventories plugin, @NotNull InventoriesConfig inventoriesConfig) throws IOException {
        this.playerFileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(inventoriesConfig.getPlayerFileCacheExpiry(), TimeUnit.MINUTES)
                .maximumSize(inventoriesConfig.getPlayerFileCacheSize())
                .recordStats()
                .build();

        this.playerProfileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(inventoriesConfig.getPlayerProfileCacheExpiry(), TimeUnit.MINUTES)
                .maximumSize(inventoriesConfig.getPlayerProfileCacheSize())
                .recordStats()
                .build();

        this.globalProfileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(inventoriesConfig.getGlobalProfileCacheExpiry(), TimeUnit.MINUTES)
                .maximumSize(inventoriesConfig.getGlobalProfileCacheSize())
                .recordStats()
                .build();

        this.playerProfileIO = new ProfileFileIO();
        this.globalProfileIO = new ProfileFileIO();

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

    /**
     * Retrieves the data file for a player based on a given world/group name.
     *
     * @param profileKey The profile target to get the file
     * @return The data file for a player.
     */
    private File getPlayerFile(ProfileKey profileKey) {
        return getPlayerFile(profileKey.getContainerType(), profileKey.getDataName(), profileKey.getPlayerName());
    }

    /**
     * Retrieves the data file for a player based on a given world/group name.
     *
     * @param type       Indicates whether data is for group or world.
     * @param dataName   The name of the group or world.
     * @param playerName The name of the player.
     * @return The data file for a player.
     */
    private File getPlayerFile(ContainerType type, String dataName, String playerName) {
        File jsonPlayerFile = new File(getProfileContainerFolder(type, dataName), playerName + JSON);
        Logging.finer("got data file: %s. Type: %s, DataName: %s, PlayerName: %s",
                jsonPlayerFile.getPath(), type, dataName, playerName);
        return jsonPlayerFile;
    }

    private File getProfileContainerFolder(ContainerType type, String folderName) {
        File folder = switch (type) {
            case GROUP -> new File(this.groupFolder, folderName);
            case WORLD -> new File(this.worldFolder, folderName);
            default -> new File(this.worldFolder, folderName);
        };

        if (!folder.exists() && !folder.mkdirs()) {
            Logging.severe("Could not create profile container folder!");
        }
        return folder;
    }

    private FileConfiguration parseToConfiguration(File file) {
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.options().continueOnSerializationError(true);
        Try.run(() -> jsonConfiguration.load(file)).getOrElseThrow(e -> {
            Logging.severe("Could not load file: " + file);
            throw new RuntimeException(e);
        });
        return jsonConfiguration;
    }

    private FileConfiguration getOrLoadProfileFile(ProfileKey profileKey, File playerFile) {
        ProfileKey fileProfileKey = profileKey.forProfileType(null);
        return Try.of(() ->
                playerFileCache.get(fileProfileKey, () -> playerFile.exists()
                        ? parseToConfiguration(playerFile)
                        : new JsonConfiguration())
        ).getOrElseThrow(e -> {
            Logging.severe("Could not load profile data for player: " + fileProfileKey);
            return new RuntimeException(e);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Void> updatePlayerData(PlayerProfile playerProfile) {
        return playerProfileIO.queueAction(() -> processUpdatePlayerData(playerProfile.clone()));
    }

    private void processUpdatePlayerData(PlayerProfile playerProfile) {
        ProfileKey profileKey = ProfileKey.fromPlayerProfile(playerProfile);
        File playerFile = getPlayerFile(profileKey);
        FileConfiguration playerData = getOrLoadProfileFile(profileKey, playerFile);
        Map<String, Object> serializedData = serializePlayerProfile(playerProfile);
        if (serializedData.isEmpty()) {
            return;
        }
        playerData.createSection(playerProfile.getProfileType().getName(), serializedData);
        Try.run(() -> playerData.save(playerFile)).onFailure(e -> {
            Logging.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                    + " for " + playerProfile.getContainerType() + ": " + playerProfile.getContainerName());
            Logging.severe(e.getMessage());
        });
    }

    private Map<String, Object> serializePlayerProfile(PlayerProfile playerProfile) {
        Map<String, Object> playerData = new LinkedHashMap<>();
        JSONObject jsonStats = new JSONObject();

        for (var entry : playerProfile.getData().entrySet()) {
            Sharable sharable = entry.getKey();
            Object sharableValue = entry.getValue();
            if (sharableValue == null) {
                continue;
            }

            var serializer = sharable.getSerializer();
            var profileEntry = sharable.getProfileEntry();
            if (serializer == null || profileEntry == null) {
                continue;
            }

            String fileTag = profileEntry.fileTag();
            Object serializedValue = serializer.serialize(sharableValue);
            if (profileEntry.isStat()) {
                jsonStats.put(fileTag, serializedValue);
            } else {
                playerData.put(fileTag, serializedValue);
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
    public PlayerProfile getPlayerData(ProfileKey key) {
        try {
            return playerProfileCache.get(key, () -> {
                File playerFile = getPlayerFile(key.getContainerType(), key.getDataName(), key.getPlayerName());
                if (!playerFile.exists()) {
                    return PlayerProfile.createPlayerProfile(key.getContainerType(), key.getDataName(),
                            key.getProfileType(), Bukkit.getOfflinePlayer(key.getPlayerUUID()));
                }
                return playerProfileIO.waitForData(() -> getPlayerDataFromDisk(key, playerFile));
            });
        } catch (ExecutionException e) {
            Logging.severe("Could not get data for player: " + key.getPlayerName()
                    + " for " + key.getContainerType().toString() + ": " + key.getDataName());
            throw new RuntimeException(e);
        }
    }

    private PlayerProfile getPlayerDataFromDisk(ProfileKey key, File playerFile) {
        FileConfiguration playerData = getOrLoadProfileFile(key, playerFile);

        // Migrate from none profile-type data
        if (migrateToProfileType(playerData)) {
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
        return deserializePlayerProfile(key, convertSection(section));
    }

    @Deprecated
    private boolean migrateToProfileType(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection(DataStrings.PLAYER_DATA);
        if (section == null) {
            return false;
        }
        config.set(ProfileTypes.SURVIVAL.getName(), section);
        config.set(ProfileTypes.CREATIVE.getName(), section);
        config.set(ProfileTypes.ADVENTURE.getName(), section);
        config.set(DataStrings.PLAYER_DATA, null);
        Logging.finer("Migrated old player data to new multi-profile format");
        return true;
    }

    private Map<String, Object> convertSection(ConfigurationSection section) {
        Set<String> keys = section.getKeys(false);
        Map<String, Object> resultMap = new HashMap<>(keys.size());
        for (String key : keys) {
            Object obj = section.get(key);
            if (obj instanceof ConfigurationSection) {
                resultMap.put(key, convertSection((ConfigurationSection) obj));
            } else {
                resultMap.put(key, obj);
            }
        }
        return resultMap;
    }

    private PlayerProfile deserializePlayerProfile(ProfileKey pKey, Map playerData) {
        PlayerProfile profile = PlayerProfile.createPlayerProfile(pKey.getContainerType(), pKey.getDataName(),
                pKey.getProfileType(), Bukkit.getOfflinePlayer(pKey.getPlayerUUID()));
        for (Object keyObj : playerData.keySet()) {
            String key = keyObj.toString();
            final Object value = playerData.get(key);
            if (value == null) {
                Logging.fine("Player data '" + key + "' is null for: " + pKey.getPlayerName());
                continue;
            }

            if (key.equalsIgnoreCase(DataStrings.PLAYER_STATS)) {
                if (value instanceof String) {
                    parseJsonPlayerStatsIntoProfile((String) value, profile);
                    continue;
                }
                if (value instanceof Map) {
                    parsePlayerStatsIntoProfile((Map) value, profile);
                } else {
                    Logging.warning("Could not parse stats for " + pKey.getPlayerName());
                }
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
        JSONObject jsonStats = null;
        try {
            jsonStats = (JSONObject) JSON_PARSER.parse(stats);
        } catch (ParseException | ClassCastException e) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Void> removePlayerData(ProfileKey profileKey) {
        if (profileKey.getProfileType() == null) {
            clearProfileCache(key -> key.getPlayerUUID().equals(profileKey.getPlayerUUID())
                    && key.getContainerType().equals(profileKey.getContainerType())
                    && key.getDataName().equals(profileKey.getDataName()));
            File playerFile = getPlayerFile(profileKey);
            if (!playerFile.exists()) {
                Logging.warning("Attempted to delete file that did not exist for player " + profileKey.getPlayerName()
                        + " in " + profileKey.getContainerType() + " " + profileKey.getDataName());
                return CompletableFuture.completedFuture(null);
            }
            return playerProfileIO.queueAction(playerFile::delete);
        }
        playerProfileCache.invalidate(profileKey);
        return playerProfileIO.queueAction(() -> processRemovePlayerData(profileKey));
    }

    private void processRemovePlayerData(ProfileKey profileKey) {
        try {
            File playerFile = getPlayerFile(profileKey.getContainerType(), profileKey.getDataName(), profileKey.getPlayerName());
            FileConfiguration playerData = getOrLoadProfileFile(profileKey, playerFile);
            playerData.set(profileKey.getProfileType().getName(), null);
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not delete data for player: " + profileKey.getPlayerName()
                    + " for " + profileKey.getContainerType() + ": " + profileKey.getDataName());
            Logging.severe(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migratePlayerData(String oldName, String newName, UUID uuid) throws IOException {
        clearPlayerCache(uuid);

        File[] worldFolders = worldFolder.listFiles(File::isDirectory);
        if (worldFolders == null) {
            throw new IOException("Could not enumerate world folders");
        }
        File[] groupFolders = groupFolder.listFiles(File::isDirectory);
        if (groupFolders == null) {
            throw new IOException("Could not enumerate group folders");
        }

        migrateForContainerType(worldFolders, ContainerType.WORLD, oldName, newName);
        migrateForContainerType(groupFolders, ContainerType.GROUP, oldName, newName);
    }

    private void migrateForContainerType(File[] folders, ContainerType containerType, String oldName, String newName) {
        for (File folder : folders) {
            File oldNameFile = getPlayerFile(containerType, folder.getName(), oldName);
            File newNameFile = getPlayerFile(containerType, folder.getName(), newName);
            if (!oldNameFile.exists()) {
                Logging.fine("No old data for player %s in %s %s to migrate.",
                        oldName, containerType.name(), folder.getName());
                continue;
            }
            if (newNameFile.exists()) {
                Logging.warning("Data already exists for player %s in %s %s. Not migrating.",
                        newName, containerType.name(), folder.getName());
                continue;
            }
            if (!oldNameFile.renameTo(newNameFile)) {
                Logging.warning("Could not rename old data file for player %s in %s %s to %s.",
                        oldName, containerType.name(), folder.getName(), newName);
                continue;
            }
            Logging.fine("Migrated data for player %s in %s %s to %s.",
                    oldName, containerType.name(), folder.getName(), newName);
        }
    }

    @NotNull
    @Override
    public GlobalProfile getGlobalProfile(UUID playerUUID) {
        return getGlobalProfile(Bukkit.getOfflinePlayer(playerUUID));
    }

    @NotNull
    @Override
    public GlobalProfile getGlobalProfile(OfflinePlayer player) {
        return getGlobalProfile(player.getUniqueId(), player.getName());
    }

    @NotNull
    @Override
    public GlobalProfile getGlobalProfile(UUID playerUUID, String playerName) {
        try {
            return globalProfileCache.get(playerUUID, () -> getGlobalProfileFromDisk(playerUUID, playerName));
        } catch (ExecutionException e) {
            Logging.severe("Unable to get global profile for player: " + playerName);
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Option<GlobalProfile> getExistingGlobalProfile(UUID playerUUID, String playerName) {
        File uuidFile = getGlobalFile(playerUUID.toString());
        if (!uuidFile.exists()) {
            return Option.none();
        }
        return Option.of(getGlobalProfile(playerUUID, playerName));
    }

    private GlobalProfile getGlobalProfileFromDisk(UUID playerUUID, String playerName) {
        // Migrate from player name to uuid profile file
        File legacyFile = getGlobalFile(playerName);
        if (legacyFile.exists() && !migrateGlobalProfileToUUID(legacyFile, playerUUID)) {
            Logging.warning("Could not properly migrate player global data file for " + playerName);
        }

        // Load from existing profile file
        File uuidFile = getGlobalFile(playerUUID.toString());
        if (!uuidFile.exists()) {
            return GlobalProfile.createGlobalProfile(playerUUID, playerName);
        }
        return loadGlobalProfile(uuidFile, playerName, playerUUID);
    }

    private boolean migrateGlobalProfileToUUID(File legacyFile, UUID playerUUID) {
        return legacyFile.renameTo(getGlobalFile(playerUUID.toString()));
    }

    private GlobalProfile loadGlobalProfile(File globalFile, String playerName, UUID playerUUID) {
        FileConfiguration playerData = globalProfileIO.waitForData(() -> parseToConfiguration(globalFile));
        ConfigurationSection section = playerData.getConfigurationSection(DataStrings.PLAYER_DATA);
        if (section == null) {
            section = playerData.createSection(DataStrings.PLAYER_DATA);
        }
        return GlobalProfile.deserialize(playerName, playerUUID, section);
    }

    public Future<Void> modifyGlobalProfile(UUID playerUUID, Consumer<GlobalProfile> consumer) {
        return modifyGlobalProfile(getGlobalProfile(playerUUID), consumer);
    }

    public Future<Void> modifyGlobalProfile(OfflinePlayer offlinePlayer, Consumer<GlobalProfile> consumer) {
        return modifyGlobalProfile(getGlobalProfile(offlinePlayer), consumer);
    }

    private Future<Void> modifyGlobalProfile(GlobalProfile globalProfile, Consumer<GlobalProfile> consumer) {
        consumer.accept(globalProfile);
        return updateGlobalProfile(globalProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Void> updateGlobalProfile(GlobalProfile globalProfile) {
        return globalProfileIO.queueAction(() -> processGlobalProfileWrite(globalProfile));
    }

    private void processGlobalProfileWrite(GlobalProfile globalProfile) {
        File playerFile = getGlobalFile(globalProfile.getPlayerUUID().toString());
        FileConfiguration playerData = new JsonConfiguration();
        playerData.createSection(DataStrings.PLAYER_DATA, globalProfile.serialize(globalProfile));
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save global data for player: " + globalProfile);
            Logging.severe(e.getMessage());
        }
    }

    /**
     * Retrieves the data file for a player for their global data.
     *
     * @param fileName The name of the file (player name or UUID) without extension.
     * @return The data file for a player.
     */
    private File getGlobalFile(String fileName) {
        return new File(playerFolder, fileName + JSON);
    }

    void clearPlayerCache(UUID playerUUID) {
        clearProfileCache(key -> key.getPlayerUUID().equals(playerUUID));
    }

    @Override
    public void clearProfileCache(ProfileKey key) {
        playerFileCache.invalidate(key);
        playerProfileCache.invalidate(key);
    }

    @Override
    public void clearProfileCache(Predicate<ProfileKey> predicate) {
        playerFileCache.invalidateAll(Sets.filter(playerFileCache.asMap().keySet(), predicate::test));
        playerProfileCache.invalidateAll(Sets.filter(playerProfileCache.asMap().keySet(), predicate::test));
    }

    @Override
    public void clearAllCache() {
        playerFileCache.invalidateAll();
        globalProfileCache.invalidateAll();
        playerProfileCache.invalidateAll();
    }

    @Override
    public Map<String, CacheStats> getCacheStats() {
        Map<String, CacheStats> stats = new HashMap<>();
        stats.put("playerFileCache", playerFileCache.stats());
        stats.put("globalProfileCache", globalProfileCache.stats());
        stats.put("profileCache", playerProfileCache.stats());
        return stats;
    }
}
