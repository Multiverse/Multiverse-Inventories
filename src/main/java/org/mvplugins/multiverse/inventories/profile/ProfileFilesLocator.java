package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
final class ProfileFilesLocator {

    private static final String JSON = ".json";

    private final File worldFolder;
    private final File groupFolder;
    private final File globalFolder;

    @Inject
    ProfileFilesLocator(@NotNull MultiverseInventories plugin) throws IOException {
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
        this.globalFolder = new File(plugin.getDataFolder(), "players");
        if (!this.globalFolder.exists()) {
            if (!this.globalFolder.mkdirs()) {
                throw new IOException("Could not create player folder!");
            }
        }
    }

    File getWorldFolder() {
        return worldFolder;
    }

    File getGroupFolder() {
        return groupFolder;
    }

    File getContainerFolder(ContainerType type) {
        return switch (type) {
            case GROUP -> this.groupFolder;
            case WORLD -> this.worldFolder;
        };
    }

    List<File> listProfileContainerFolders(ContainerType type) {
        return Option.of(getContainerFolder(type).listFiles())
                .map(filesList -> Arrays.stream(filesList)
                        .filter(File::isDirectory)
                        .toList())
                .getOrElse(Collections::emptyList);
    }

    File getProfileContainerFolder(ContainerType type, String folderName) {
        File folder = new File(getContainerFolder(type), folderName);
        if (!folder.exists() && !folder.mkdirs()) {
            Logging.severe("Could not create profile container folder!");
        }
        return folder;
    }

    List<File> listPlayerProfileFiles(ContainerType type, String dataName) {
        return Option.of(getProfileContainerFolder(type, dataName).listFiles())
                .map(filesList -> Arrays.stream(filesList)
                        .filter(File::isFile)
                        .toList())
                .getOrElse(Collections::emptyList);
    }

    /**
     * Retrieves the data file for a player based on a given world/group name.
     *
     * @param profileKey The profile target to get the file
     * @return The data file for a player.
     */
    File getPlayerProfileFile(ProfileKey profileKey) {
        return getPlayerProfileFile(profileKey.getContainerType(), profileKey.getDataName(), profileKey.getPlayerName());
    }

    /**
     * Retrieves the data file for a player based on a given world/group name.
     *
     * @param type       Indicates whether data is for group or world.
     * @param dataName   The name of the group or world.
     * @param playerName The name of the player.
     * @return The data file for a player.
     */
    File getPlayerProfileFile(ContainerType type, String dataName, String playerName) {
        File jsonPlayerFile = new File(getProfileContainerFolder(type, dataName), playerName + JSON);
        Logging.finer("got data file: %s. Type: %s, DataName: %s, PlayerName: %s",
                jsonPlayerFile.getPath(), type, dataName, playerName);
        return jsonPlayerFile;
    }

    File getGlobalFolder() {
        return this.globalFolder;
    }

    List<File> listGlobalFiles() {
       return Option.of(this.globalFolder.listFiles())
               .map(filesList -> Arrays.stream(filesList)
                       .filter(File::isFile)
                       .toList())
               .getOrElse(Collections::emptyList);
    }

    File getGlobalFile(UUID playerUUID) {
        return getGlobalFile(playerUUID.toString());
    }

    /**
     * Retrieves the data file for a player for their global data.
     *
     * @param playerIdentifier The name of the file (player name or UUID) without extension.
     * @return The data file for a player.
     */
    File getGlobalFile(String playerIdentifier) {
        return new File(globalFolder, playerIdentifier + JSON);
    }
}
