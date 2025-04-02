package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.mvplugins.multiverse.inventories.util.DataStrings;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Service
final class FlatFileProfileDataSource implements ProfileDataSource {

    private static final String JSON = ".json";

    private final AsyncFileIO asyncFileIO;
    private final ProfileFilesLocator profileFilesLocator;
    private final ProfileCacheManager profileCacheManager;

    @Inject
    FlatFileProfileDataSource(
            @NotNull AsyncFileIO asyncFileIO,
            @NotNull ProfileFilesLocator profileFilesLocator,
            @NotNull ProfileCacheManager profileCacheManager
    ) {
        this.asyncFileIO = asyncFileIO;
        this.profileFilesLocator = profileFilesLocator;
        this.profileCacheManager = profileCacheManager;
    }

    private FileConfiguration parseToConfiguration(File file) {
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.options().continueOnSerializationError(false);
        Try.run(() -> jsonConfiguration.load(file)).getOrElseThrow(e -> {
            Logging.severe("Could not load file %s : %s", file, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        });
        return jsonConfiguration;
    }

    private FileConfiguration getOrLoadProfileFile(ProfileKey profileKey, File playerFile) {
        ProfileKey fileProfileKey = profileKey.forProfileType(null);
        return Try.of(() ->
                profileCacheManager.getOrLoadPlayerFile(fileProfileKey, (key) -> playerFile.exists()
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
    public CompletableFuture<Void> updatePlayerData(PlayerProfile playerProfile) {
        ProfileKey profileKey = ProfileKey.fromPlayerProfile(playerProfile);
        File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
        return asyncFileIO.queueAction(playerFile, () -> processUpdatePlayerData(profileKey, playerFile, playerProfile.clone()));
    }

    private void processUpdatePlayerData(ProfileKey profileKey, File playerFile, PlayerProfile playerProfile) {
        FileConfiguration playerData = getOrLoadProfileFile(profileKey, playerFile);
        Map<String, Object> serializedData = PlayerProfileJsonSerializer.serialize(playerProfile);
        if (serializedData.isEmpty()) {
            return;
        }
        playerData.createSection(playerProfile.getProfileType().getName(), serializedData);
        Try.run(() -> playerData.save(playerFile)).onFailure(e -> {
            Logging.severe("Could not save data for player: " + playerProfile.getPlayerName()
                    + " for " + playerProfile.getContainerType() + ": " + playerProfile.getContainerName());
            e.printStackTrace();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerDataNow(ProfileKey profileKey) {
        try {
            return getPlayerData(profileKey).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<PlayerProfile> getPlayerData(ProfileKey profileKey) {
        try {
            return profileCacheManager.getOrLoadPlayerProfile(profileKey, (key, executor) -> {
                File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
                if (!playerFile.exists()) {
                    Logging.fine("Not found on disk: %s", playerFile);
                    return CompletableFuture.completedFuture(PlayerProfile.createPlayerProfile(key.getContainerType(), key.getDataName(),
                            key.getProfileType(), key.getPlayerUUID(), key.getPlayerName()));
                }
                Logging.finer("%s not cached. loading from disk...", profileKey);
                return asyncFileIO.queueCallable(playerFile, () -> getPlayerDataFromDisk(key, playerFile));
            });
        } catch (Exception e) {
            Logging.severe("Could not get data for player: " + profileKey.getPlayerName()
                    + " for " + profileKey.getContainerType().toString() + ": " + profileKey.getDataName());
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
                e.printStackTrace();
            }
        }

        ConfigurationSection section = playerData.getConfigurationSection(key.getProfileType().getName());
        if (section == null) {
            section = playerData.createSection(key.getProfileType().getName());
        }
        return PlayerProfileJsonSerializer.deserialize(key, convertSection(section));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> removePlayerData(ProfileKey profileKey) {
        File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
        if (profileKey.getProfileType() == null) {
            for (var type : ProfileTypes.getTypes()) {
                profileCacheManager.getCachedPlayerProfile(profileKey.forProfileType(type))
                        .peek(profile -> profile.getData().clear());
            }
            if (!playerFile.exists()) {
                Logging.warning("Attempted to delete file that did not exist for player " + profileKey.getPlayerName()
                        + " in " + profileKey.getContainerType() + " " + profileKey.getDataName());
                return CompletableFuture.completedFuture(null);
            }
            return asyncFileIO.queueAction(playerFile, playerFile::delete);
        }
        profileCacheManager.getCachedPlayerProfile(profileKey).peek(profile -> profile.getData().clear());
        return asyncFileIO.queueAction(playerFile, () -> processRemovePlayerData(profileKey, playerFile));
    }

    private void processRemovePlayerData(ProfileKey profileKey, File playerFile) {
        try {
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
    public void migratePlayerData(String oldName, String newName, UUID uuid) {
        profileCacheManager.clearPlayerCache(uuid);

        List<File> worldFolders = profileFilesLocator.listProfileContainerFolders(ContainerType.WORLD);
        List<File> groupFolders = profileFilesLocator.listProfileContainerFolders(ContainerType.GROUP);

        migrateForContainerType(worldFolders, ContainerType.WORLD, oldName, newName);
        migrateForContainerType(groupFolders, ContainerType.GROUP, oldName, newName);
    }

    private void migrateForContainerType(List<File> folders, ContainerType containerType, String oldName, String newName) {
        for (File folder : folders) {
            File oldNameFile = profileFilesLocator.getPlayerProfileFile(containerType, folder.getName(), oldName);
            File newNameFile = profileFilesLocator.getPlayerProfileFile(containerType, folder.getName(), newName);
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
    public GlobalProfile getGlobalProfileNow(UUID playerUUID) {
        return getGlobalProfileNow(Bukkit.getOfflinePlayer(playerUUID));
    }

    @NotNull
    @Override
    public GlobalProfile getGlobalProfileNow(OfflinePlayer player) {
        return getGlobalProfileNow(player.getUniqueId(), player.getName());
    }

    @NotNull
    @Override
    public GlobalProfile getGlobalProfileNow(UUID playerUUID, String playerName) {
        try {
            return getGlobalProfile(playerUUID, playerName).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Option<GlobalProfile> getExistingGlobalProfileNow(UUID playerUUID, String playerName) {
        try {
            return getExistingGlobalProfile(playerUUID, playerName).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<GlobalProfile> getGlobalProfile(UUID playerUUID) {
        return getGlobalProfile(Bukkit.getOfflinePlayer(playerUUID));
    }

    @Override
    public CompletableFuture<GlobalProfile> getGlobalProfile(OfflinePlayer player) {
        return getGlobalProfile(player.getUniqueId(), player.getName());
    }

    @NotNull
    @Override
    public CompletableFuture<GlobalProfile> getGlobalProfile(UUID playerUUID, String playerName) {
        try {
            File globalFile = profileFilesLocator.getGlobalFile(playerUUID.toString());
            return profileCacheManager.getOrLoadGlobalProfile(playerUUID, (key, executor) -> {
                Logging.finer("Global profile for player %s (%s) not in cached. Loading...", playerUUID, playerName);
                // Migrate from player name to uuid profile file
                File legacyFile = profileFilesLocator.getGlobalFile(playerName);
                if (legacyFile.exists() && !migrateGlobalProfileToUUID(legacyFile, playerUUID)) {
                    Logging.warning("Could not properly migrate player global data file for " + playerName);
                }

                // Load from existing profile file
                if (!globalFile.exists()) {
                    return CompletableFuture.completedFuture(GlobalProfile.createGlobalProfile(playerUUID, playerName));
                }
                return asyncFileIO.queueCallable(globalFile, () -> getGlobalProfileFromDisk(playerUUID, playerName, globalFile));
            });
        } catch (Exception e) {
            Logging.severe("Unable to get global profile for player: " + playerName);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public CompletableFuture<Option<GlobalProfile>> getExistingGlobalProfile(UUID playerUUID, String playerName) {
        File uuidFile = profileFilesLocator.getGlobalFile(playerUUID.toString());
        if (!uuidFile.exists()) {
            return CompletableFuture.completedFuture(Option.none());
        }
        return getGlobalProfile(playerUUID, playerName).thenApply(Option::of);
    }

    private boolean migrateGlobalProfileToUUID(File legacyFile, UUID playerUUID) {
        return legacyFile.renameTo(profileFilesLocator.getGlobalFile(playerUUID.toString()));
    }

    private GlobalProfile getGlobalProfileFromDisk(UUID playerUUID, String playerName, File globalFile) {
        FileConfiguration playerData = parseToConfiguration(globalFile);
        ConfigurationSection section = playerData.getConfigurationSection(DataStrings.PLAYER_DATA);
        if (section == null) {
            return GlobalProfile.createGlobalProfile(playerUUID, playerName);
        }
        return GlobalProfile.deserialize(playerName, playerUUID, section);
    }

    public CompletableFuture<Void> modifyGlobalProfile(UUID playerUUID, Consumer<GlobalProfile> consumer) {
        return getGlobalProfile(playerUUID).thenCompose(globalProfile -> modifyGlobalProfile(globalProfile, consumer));
    }

    public CompletableFuture<Void> modifyGlobalProfile(OfflinePlayer offlinePlayer, Consumer<GlobalProfile> consumer) {
        return getGlobalProfile(offlinePlayer).thenCompose(globalProfile -> modifyGlobalProfile(globalProfile, consumer));
    }

    private CompletableFuture<Void> modifyGlobalProfile(GlobalProfile globalProfile, Consumer<GlobalProfile> consumer) {
        consumer.accept(globalProfile);
        return updateGlobalProfile(globalProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> updateGlobalProfile(GlobalProfile globalProfile) {
        File globalFile = profileFilesLocator.getGlobalFile(globalProfile.getPlayerUUID().toString());
        return asyncFileIO.queueAction(globalFile, () -> processGlobalProfileWrite(globalProfile, globalFile));
    }

    private void processGlobalProfileWrite(GlobalProfile globalProfile, File globalFile) {
        FileConfiguration playerData = new JsonConfiguration();
        playerData.createSection(DataStrings.PLAYER_DATA, globalProfile.serialize(globalProfile));
        try {
            playerData.save(globalFile);
        } catch (IOException e) {
            Logging.severe("Could not save global data for player: " + globalProfile);
            Logging.severe(e.getMessage());
        }
    }

    @Override
    public Collection<UUID> getGlobalPlayersList() {
        return profileFilesLocator.listGlobalFiles()
                .stream()
                .map(file -> UUID.fromString(com.google.common.io.Files.getNameWithoutExtension(file.getName())))
                .toList();
    }

    @Override
    public Collection<String> getContainerPlayersList(ContainerType containerType, String containerName) {
        return profileFilesLocator.listPlayerProfileFiles(containerType, containerName)
                .stream()
                .map(file -> com.google.common.io.Files.getNameWithoutExtension(file.getName()))
                .toList();
    }

    @Override
    public Collection<String> getContainerNames(ContainerType containerType) {
        return profileFilesLocator.listProfileContainerFolders(containerType)
                .stream()
                .map(File::getName)
                .toList();
    }
}
