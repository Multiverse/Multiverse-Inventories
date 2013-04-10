package com.onarandombox.multiverseinventories.util.data;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

class YamlToJsonConverter {

    private final FlatFilePlayerData data;
    private final File worldsFolder;
    private final File groupsFolder;
    private final File playersFolder;

    YamlToJsonConverter(final FlatFilePlayerData data, final File worldsFolder, final File groupsFolder, final File playersFolder) {
        this.data = data;
        this.worldsFolder = worldsFolder;
        this.groupsFolder = groupsFolder;
        this.playersFolder = playersFolder;
    }

    public void convert() {
        File[] files = worldsFolder.listFiles();
        if (files != null) {
            Logging.fine("Checking for world data to convert...");
            for (final File file : files) {
                if (file.isDirectory()) {
                    processWorldOrGroupFolder(ContainerType.WORLD, file);
                }
            }
        } else {
            Logging.warning("There was an issue converting per-world profiles!");
        }
        files = groupsFolder.listFiles();
        if (files != null) {
            Logging.fine("Checking for group data to convert...");
            for (final File file : files) {
                if (file.isDirectory()) {
                    processWorldOrGroupFolder(ContainerType.GROUP, file);
                }
            }
        } else {
            Logging.warning("There was an issue converting per-group profiles!");
        }
        files = playersFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".yml");
            }
        });
        if (files != null) {
            if (files.length > 0) {
                Logging.fine("Converting global data...");
            }
            for (final File file : files) {
                if (!file.isDirectory()) {
                    Logging.finest("Converting global data for player '%s'...", getPlayerName(file));
                    convertPlayerProfile(file, null, null);
                }
            }
        } else {
            Logging.warning("There was an issue converting global profiles!");
        }
    }

    private void processWorldOrGroupFolder(final ContainerType containerType, final File folder) {
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".yml");
            }
        });
        if (files != null) {
            if (files.length > 0) {
                Logging.fine("Converting player data for %s '%s'...", containerType, folder.getName());
            } else {
                Logging.fine("No player data to convert for %s '%s'.", containerType, folder.getName());
            }
            for (final File file : files) {
                Logging.finest("Converting data for player '%s' for %s '%s'...", getPlayerName(file), containerType, folder.getName());
                convertPlayerProfile(file, containerType, folder.getName());
            }
        } else {
            Logging.warning("There was an issue converting profiles for %s '%s'", containerType, folder.getName());
        }
    }

    private String getPlayerName(final File file) {
        return file.getName().substring(0, file.getName().lastIndexOf(".yml"));
    }

    private void convertPlayerProfile(final File oldFile, final ContainerType containerType, final String containerName) {
        /*final String playerName = getPlayerName(oldFile);
        final File newFile = new File(oldFile.getParentFile(), playerName + ".json");
        if (!newFile.exists()) {
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                Logging.warning("Could not create new profile file %s", newFile);
                return;
            }
        }
        if (containerType == null) {
            FileConfiguration oldConfig = data.getConfigHandle(oldFile);
            FileConfiguration newConfig = data.getConfigHandle(newFile);
            for (final String key : oldConfig.getKeys(false)) {
                if (!newConfig.contains(key)) {
                    newConfig.set(key, oldConfig.get(key));
                }
            }
            try {
                newConfig.save(newFile);
            } catch (IOException e) {
                Logging.warning("Could not save converted file %s", newFile);
                return;
            }
        } else {
            PlayerProfile playerProfile = data.getPlayerData(oldFile, containerType, containerName, ProfileTypes.ADVENTURE, playerName);
            data.updatePlayerData(playerProfile);
            playerProfile = data.getPlayerData(oldFile, containerType, containerName, ProfileTypes.CREATIVE, playerName);
            data.updatePlayerData(playerProfile);
            playerProfile = data.getPlayerData(oldFile, containerType, containerName, ProfileTypes.SURVIVAL, playerName);
            data.updatePlayerData(playerProfile);
        }

        Logging.finer("Successfully converted file [%s] to file [%s]", oldFile, newFile);
        oldFile.renameTo(new File(oldFile.getParentFile(), playerName + ".yml.bak"));
        //oldFile.delete();*/
    }
}
