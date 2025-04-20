package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.mvplugins.multiverse.inventories.util.DataStrings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
final class FlatFileProfileDataSource implements ProfileDataSource {

    private final AsyncFileIO asyncFileIO;
    private final ProfileFilesLocator profileFilesLocator;
    private final ProfileCacheManager profileCacheManager;
    private final PlayerNamesMapper playerNamesMapper;

    @Inject
    FlatFileProfileDataSource(
            @NotNull AsyncFileIO asyncFileIO,
            @NotNull ProfileFilesLocator profileFilesLocator,
            @NotNull ProfileCacheManager profileCacheManager,
            @NotNull PlayerNamesMapper playerNamesMapper
    ) {
        this.asyncFileIO = asyncFileIO;
        this.profileFilesLocator = profileFilesLocator;
        this.profileCacheManager = profileCacheManager;
        this.playerNamesMapper = playerNamesMapper;
    }

    private FileConfiguration loadFileToJsonConfiguration(File file) {
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.options().continueOnSerializationError(false);
        Try.run(() -> jsonConfiguration.load(file)).getOrElseThrow(e -> {
            Logging.severe("Could not load file %s : %s", file, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        });
        return jsonConfiguration;
    }

    private FileConfiguration getOrLoadPlayerProfileFile(ProfileFileKey profileKey, File playerFile) {
        ProfileKey fileProfileKey = profileKey.forProfileType(null);
        return Try.of(() ->
                profileCacheManager.getOrLoadPlayerFile(fileProfileKey, (key) -> playerFile.exists()
                        ? loadFileToJsonConfiguration(playerFile)
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
    public CompletableFuture<PlayerProfile> getPlayerProfile(ProfileKey profileKey) {
        try {
            if (Strings.isNullOrEmpty(profileKey.getPlayerName())) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("Player name cannot be null or empty. " + profileKey));
            }
            return profileCacheManager.getOrLoadPlayerProfile(profileKey, (key, executor) -> {
                File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
                if (!playerFile.exists()) {
                    Logging.fine("Not found on disk: %s", playerFile);
                    return CompletableFuture.completedFuture(PlayerProfile.createPlayerProfile(key));
                }
                Logging.finer("%s not cached. loading from disk...", profileKey);
                return asyncFileIO.queueFileCallable(playerFile, () -> getPlayerProfileFromDisk(key, playerFile));
            });
        } catch (Exception e) {
            Logging.severe("Could not get data for player: " + profileKey.getPlayerName()
                    + " for " + profileKey.getContainerType().toString() + ": " + profileKey.getDataName());
            throw new RuntimeException(e);
        }
    }

    private PlayerProfile getPlayerProfileFromDisk(ProfileKey key, File playerFile) {
        FileConfiguration playerData = getOrLoadPlayerProfileFile(key, playerFile);

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
    public CompletableFuture<Void> updatePlayerProfile(PlayerProfile playerProfile) {
        ProfileKey profileKey = ProfileKey.fromPlayerProfile(playerProfile);
        File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
        return asyncFileIO.queueFileAction(
                playerFile,
                () -> savePlayerProfileToDisk(profileKey, playerFile, playerProfile.clone())
        );
    }

    private void savePlayerProfileToDisk(ProfileKey profileKey, File playerFile, PlayerProfile playerProfile) {
        FileConfiguration playerData = getOrLoadPlayerProfileFile(profileKey, playerFile);
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
    public CompletableFuture<Void> deletePlayerProfile(ProfileKey profileKey) {
        if (Strings.isNullOrEmpty(profileKey.getPlayerName())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Player name cannot be null or empty. " + profileKey));
        }
        File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
        profileCacheManager.getCachedPlayerProfile(profileKey).peek(profile -> profile.getData().clear());
        return asyncFileIO.queueFileAction(playerFile, () ->
                deletePlayerProfileFromDisk(profileKey, playerFile, new ProfileType[]{profileKey.getProfileType()}));
    }

    @Override
    public CompletableFuture<Void> deletePlayerProfiles(ProfileFileKey profileKey, ProfileType[] profileTypes) {
        if (Strings.isNullOrEmpty(profileKey.getPlayerName())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Player name cannot be null or empty. " + profileKey));
        }
        if (ProfileTypes.isAll(profileTypes)) {
            Logging.finer("Deleting profile: " + profileKey + " for all profile-types");
            return deletePlayerFile(profileKey);
        }
        for (var profileType : profileTypes) {
            profileCacheManager.getCachedPlayerProfile(profileKey.forProfileType(profileType))
                    .peek(profile -> profile.getData().clear());
        }
        File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
        return asyncFileIO.queueFileAction(playerFile, () ->
                deletePlayerProfileFromDisk(profileKey, playerFile, profileTypes));
    }

    private void deletePlayerProfileFromDisk(ProfileFileKey profileKey, File playerFile, ProfileType[] profileTypes) {
        try {
            FileConfiguration playerData = getOrLoadPlayerProfileFile(profileKey, playerFile);
            for (var profileType : profileTypes) {
                playerData.set(profileType.getName(), null);
            }
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
    public CompletableFuture<Void> deletePlayerFile(ProfileFileKey profileKey) {
        for (var type : ProfileTypes.getTypes()) {
            profileCacheManager.getCachedPlayerProfile(profileKey.forProfileType(type))
                    .peek(profile -> profile.getData().clear());
        }
        File playerFile = profileFilesLocator.getPlayerProfileFile(profileKey);
        if (!playerFile.exists()) {
            Logging.finer("Attempted to delete file that did not exist for player " + profileKey.getPlayerName()
                    + " in " + profileKey.getContainerType() + " " + profileKey.getDataName());
            return CompletableFuture.completedFuture(null);
        }
        return asyncFileIO.queueFileAction(playerFile, () -> {
            if (!playerFile.delete()) {
                Logging.warning("Could not delete file for player " + profileKey.getPlayerName()
                        + " in " + profileKey.getContainerType() + " " + profileKey.getDataName());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migratePlayerProfileName(String oldName, String newName) {
        profileCacheManager.clearPlayerCache(oldName);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<GlobalProfile> getGlobalProfile(GlobalProfileKey key) {
        File globalFile = profileFilesLocator.getGlobalFile(key.getPlayerUUID());
        return profileCacheManager.getOrLoadGlobalProfile(key.getPlayerUUID(), (uuid, executor) -> {
            Logging.finer("Global profile for player %s (%s) not in cached. Loading...", uuid, key.getPlayerName());
            migrateGlobalProfileToUUID(uuid, key.getPlayerName());
            if (!globalFile.exists()) {
                GlobalProfile globalProfile = new GlobalProfile(key.getPlayerUUID(), globalFile.toPath());
                globalProfile.setLastKnownName(key.getPlayerName());
                return CompletableFuture.completedFuture(globalProfile);
            }
            return asyncFileIO.queueFileCallable(globalFile, () -> getGlobalProfileFromDisk(key.getPlayerUUID(), globalFile));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Option<GlobalProfile>> getExistingGlobalProfile(GlobalProfileKey key) {
        File uuidFile = profileFilesLocator.getGlobalFile(key.getPlayerUUID());
        if (!uuidFile.exists()) {
            return CompletableFuture.completedFuture(Option.none());
        }
        return getGlobalProfile(key).thenApply(Option::of);
    }

    private void migrateGlobalProfileToUUID(UUID playerUUID, String playerName) {
        File legacyFile = profileFilesLocator.getGlobalFile(playerName);
        if (!legacyFile.exists()) {
            return;
        }
        if (!legacyFile.renameTo(profileFilesLocator.getGlobalFile(playerUUID.toString()))) {
            Logging.warning("Could not properly migrate player global data file for " + playerName);
        }
    }

    private GlobalProfile getGlobalProfileFromDisk(UUID playerUUID, File globalFile) {
        return new GlobalProfile(playerUUID, globalFile.toPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> modifyGlobalProfile(GlobalProfileKey key, Consumer<GlobalProfile> consumer) {
        return getGlobalProfile(key).thenCompose(globalProfile -> modifyGlobalProfile(globalProfile, consumer));
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
        boolean didPlayerNameChange = playerNamesMapper.setPlayerName(globalProfile.getPlayerUUID(), globalProfile.getLastKnownName());
        return asyncFileIO.queueFileAction(globalFile, () -> processGlobalProfileWrite(globalProfile))
                .thenCompose(ignore -> didPlayerNameChange
                        ? playerNamesMapper.savePlayerNames()
                        : CompletableFuture.completedFuture(null));
    }

    private void processGlobalProfileWrite(GlobalProfile globalProfile) {
        globalProfile.save().onFailure(throwable -> {
            Logging.severe("Could not save global data for player: " + globalProfile);
            Logging.severe(throwable.getMessage());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> deleteGlobalProfile(GlobalProfileKey key) {
        return deleteGlobalProfile(key, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> deleteGlobalProfile(GlobalProfileKey key, boolean clearPlayerFiles) {
        return getExistingGlobalProfile(key)
                .thenCompose(globalProfile -> {
                    if (globalProfile.isEmpty()) {
                        return CompletableFuture.failedFuture(new MultiverseException("Invalid global profile for player: " + key));
                    }
                    return deleteGlobalProfileFromDisk(globalProfile.get());
                })
                .thenCompose(ignore -> clearPlayerFiles
                        ? clearAllPlayerProfileFiles(key.getPlayerUUID(), key.getPlayerName())
                        : CompletableFuture.completedFuture(null));
    }

    private CompletableFuture<Void> deleteGlobalProfileFromDisk(GlobalProfile globalProfile) {
        File globalFile = profileFilesLocator.getGlobalFile(globalProfile.getPlayerUUID().toString());
        return asyncFileIO.queueFileAction(globalFile, () -> {
            if (!globalFile.delete()) {
                throw new RuntimeException("Could not delete global profile file: " + globalFile);
            }
        });
    }

    private CompletableFuture<Void> clearAllPlayerProfileFiles(UUID playerUUID, String playerName) {
        return CompletableFuture.allOf(Arrays.stream(ContainerType.values())
                .flatMap(containerType -> listContainerDataNames(containerType)
                        .stream()
                        .map(containerName -> deletePlayerFile(ProfileFileKey.of(
                                containerType,
                                containerName,
                                playerUUID,
                                playerName))))
                .toArray(CompletableFuture[]::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listContainerDataNames(ContainerType containerType) {
        return profileFilesLocator.listProfileContainerFolders(containerType)
                .stream()
                .map(File::getName)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listPlayerProfileNames(ContainerType containerType, String containerName) {
        return profileFilesLocator.listPlayerProfileFiles(containerType, containerName)
                .stream()
                .map(file -> com.google.common.io.Files.getNameWithoutExtension(file.getName()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UUID> listGlobalProfileUUIDs() {
        return profileFilesLocator.listGlobalFiles()
                .stream()
                .map(file -> UUID.fromString(com.google.common.io.Files.getNameWithoutExtension(file.getName())))
                .toList();
    }
}
