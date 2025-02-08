package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;

@Service
final class FlatFileProfileDataSource implements ProfileDataSource {

    private static final String JSON = ".json";

    private final JSONParser JSON_PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE | JSONParser.ACCEPT_TAILLING_SPACE);

    // TODO these probably need configurable max sizes
    private final Cache<ProfileKey, FileConfiguration> configCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

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

    private final ProfileFileIO profileFileIO;

    @Inject
    FlatFileProfileDataSource(@NotNull MultiverseInventories plugin, @NotNull ProfileFileIO profileFileIO) throws IOException {
        this.profileFileIO = profileFileIO;

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
     * Retrieves the data file for a player based on a given world/group name, creating it if necessary.
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

        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlayerData(PlayerProfile playerProfile) {
        profileFileIO.queueAction(() -> processProfileWrite(playerProfile.clone()));
    }

    private void processProfileWrite(PlayerProfile playerProfile) {
        File playerFile = getPlayerFile(
                playerProfile.getContainerType(),
                playerProfile.getContainerName(),
                playerProfile.getPlayer().getName()
        );
        try {
            ProfileKey fileProfileKey = ProfileKey.createProfileKey(
                    playerProfile.getContainerType(),
                    playerProfile.getContainerName(),
                    null,
                    playerProfile.getPlayer().getUniqueId(),
                    playerProfile.getPlayer().getName());
            FileConfiguration playerData = configCache.getIfPresent(fileProfileKey);
            if (playerData == null) {
                playerData = playerFile.exists()
                        ? profileFileIO.getConfigHandleNow(playerFile)
                        : new JsonConfiguration();
                configCache.put(fileProfileKey, playerData);
            }
            Map<String, Object> serializedData = serializePlayerProfile(playerProfile);
            if (serializedData.isEmpty()) {
                return;
            }
            playerData.createSection(playerProfile.getProfileType().getName(), serializedData);
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                    + " for " + playerProfile.getContainerType() + ": " + playerProfile.getContainerName());
            Logging.severe(e.getMessage());
        } catch (Exception e) {
            Logging.severe("Unknown error while attempting to write profile data.", e);
        }
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

            String fileTag = profileEntry.getFileTag();
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
    public PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID) {
        return getPlayerData(ProfileKey.createProfileKey(containerType, dataName, profileType, playerUUID));
    }

    private PlayerProfile getPlayerData(ProfileKey key) {
        PlayerProfile cached = profileCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        File playerFile = getPlayerFile(key.getContainerType(), key.getDataName(), key.getPlayerName());
        if (!playerFile.exists()) {
            PlayerProfile playerProfile = PlayerProfile.createPlayerProfile(key.getContainerType(), key.getDataName(),
                    key.getProfileType(), Bukkit.getOfflinePlayer(key.getPlayerUUID()));
            profileCache.put(key, playerProfile);
            return playerProfile;
        }

        // Migrate from none profile-type data
        ProfileKey fileProfileKey = ProfileKey.createProfileKey(key, (ProfileType) null);
        FileConfiguration playerData = configCache.getIfPresent(fileProfileKey);
        if (playerData == null) {
            playerData = profileFileIO.waitForConfigHandle(playerFile);
            configCache.put(fileProfileKey, playerData);
        }
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
        PlayerProfile playerProfile = deserializePlayerProfile(key, convertSection(section));
        profileCache.put(key, playerProfile);
        return playerProfile;
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

    // TODO Remove this conversion
    private boolean convertConfig(FileConfiguration config) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID) {
        ProfileKey profileKey = ProfileKey.createProfileKey(containerType, dataName, profileType, playerUUID);
        if (profileKey.getProfileType() == null) {
            try {
                File playerFile = getPlayerFile(containerType, dataName, profileKey.getPlayerName());
                configCache.invalidate(profileKey);
                profileCache.invalidateAll(Sets.filter(
                        profileCache.asMap().keySet(),
                        key -> key.getPlayerUUID().equals(playerUUID)
                                && key.getContainerType().equals(containerType)
                                && key.getDataName().equals(dataName)
                ));
                return playerFile.delete();
            } catch (Exception ignore) {
                Logging.warning("Attempted to delete file that did not exist for player " + profileKey.getPlayerName()
                        + " in " + containerType.name().toLowerCase() + " " + dataName);
                return false;
            }
        }
        try {
            File playerFile = getPlayerFile(containerType, dataName, profileKey.getPlayerName());
            ProfileKey fileProfileKey = ProfileKey.createProfileKey(profileKey, (ProfileType) null);
            FileConfiguration playerData = configCache.getIfPresent(fileProfileKey);
            if (playerData == null) {
                if (!playerFile.exists()) {
                    return false;
                }
                playerData = profileFileIO.getConfigHandleNow(playerFile);
            }
            playerData.set(profileType.getName(), null);
            profileCache.invalidate(profileKey);
            FileConfiguration finalPlayerData = playerData;
            profileFileIO.queueAction(() -> {
                try {
                    finalPlayerData.save(playerFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InvalidConfigurationException | IOException e) {
            Logging.severe("Could not delete data for player: " + profileKey.getPlayerName()
                    + " for " + containerType.toString() + ": " + dataName);
            Logging.severe(e.getMessage());
            return false;
        }
        return true;
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
    public GlobalProfile getGlobalProfile(UUID playerUUID) {
        return getGlobalProfile(Bukkit.getOfflinePlayer(playerUUID));
    }

    @Override
    public GlobalProfile getGlobalProfile(OfflinePlayer player) {
        return getGlobalProfile(player.getName(), player.getUniqueId());
    }

    @Override
    public @NotNull GlobalProfile getGlobalProfile(String playerName, UUID playerUUID) {
        return getExistingGlobalProfile(playerName, playerUUID)
                .getOrElse(() -> GlobalProfile.createGlobalProfile(playerName, playerUUID));
    }

    @Override
    public @NotNull Option<GlobalProfile> getExistingGlobalProfile(String playerName, UUID playerUUID) {
        GlobalProfile cached = globalProfileCache.getIfPresent(playerUUID);
        if (cached != null) {
            return Option.of(cached);
        }

        // Migrate from player name to uuid profile file
        File legacyFile = getGlobalFile(playerName);
        if (legacyFile.exists() && !migrateGlobalProfileToUUID(legacyFile, playerUUID)) {
            Logging.warning("Could not properly migrate player global data file for " + playerName);
        }

        // Load from existing profile file
        File uuidFile = getGlobalFile(playerUUID.toString());
        if (uuidFile.exists()) {
            GlobalProfile globalProfile = loadGlobalProfile(uuidFile, playerName, playerUUID);
            globalProfileCache.put(playerUUID, globalProfile);
            return Option.of(globalProfile);
        }
        return Option.none();
    }

    private boolean migrateGlobalProfileToUUID(File legacyFile, UUID playerUUID) {
        return legacyFile.renameTo(getGlobalFile(playerUUID.toString()));
    }

    private GlobalProfile loadGlobalProfile(File globalFile, String playerName, UUID playerUUID) {
        FileConfiguration playerData = profileFileIO.waitForConfigHandle(globalFile);
        ConfigurationSection section = playerData.getConfigurationSection(DataStrings.PLAYER_DATA);
        if (section == null) {
            section = playerData.createSection(DataStrings.PLAYER_DATA);
        }
        return GlobalProfile.deserialize(playerName, playerUUID, section);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateGlobalProfile(GlobalProfile globalProfile) {
        File playerFile = getGlobalFile(globalProfile.getPlayerUUID().toString());
        FileConfiguration playerData = new JsonConfiguration();
        playerData.createSection(DataStrings.PLAYER_DATA, globalProfile.serialize(globalProfile));
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save global data for player: " + globalProfile);
            Logging.severe(e.getMessage());
            return false;
        }
        return true;
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

    @Override
    public void updateLastWorld(UUID playerUUID, String worldName) {
        GlobalProfile globalProfile = getGlobalProfile(playerUUID);
        globalProfile.setLastWorld(worldName);
        updateGlobalProfile(globalProfile);
    }

    @Override
    public void setLoadOnLogin(final UUID playerUUID, final boolean loadOnLogin) {
        final GlobalProfile globalProfile = getGlobalProfile(playerUUID);
        globalProfile.setLoadOnLogin(loadOnLogin);
        updateGlobalProfile(globalProfile);
    }

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

    private void migrateForContainerType(File[] folders, ContainerType containerType, String oldName, String newName) throws IOException {
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

    void clearPlayerCache(UUID playerUUID) {
        profileCache.invalidateAll(Sets.filter(
                profileCache.asMap().keySet(),
                key -> key.getPlayerUUID().equals(playerUUID)
        ));
        configCache.invalidateAll(Sets.filter(
                configCache.asMap().keySet(),
                key -> key.getPlayerUUID().equals(playerUUID)
        ));
    }

    @Override
    public void clearProfileCache(ProfileKey key) {
        profileCache.invalidate(key);
    }

    @Override
    public void clearProfileCache(Predicate<ProfileKey> predicate) {
        configCache.invalidateAll(Sets.filter(configCache.asMap().keySet(), predicate::test));
    }

    @Override
    public void clearAllCache() {
        configCache.invalidateAll();
        globalProfileCache.invalidateAll();
        profileCache.invalidateAll();
    }
}
