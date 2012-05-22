package com.onarandombox.multiverseinventories.util.data;

import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of PlayerData.
 */
public class FlatFilePlayerData implements PlayerData {

    private static final String YML = ".yml";
    private File worldFolder = null;
    private File groupFolder = null;

    public FlatFilePlayerData(JavaPlugin plugin) throws IOException {
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
    }

    private FileConfiguration getConfigHandle(File file) {
        return EnhancedConfiguration.loadConfiguration(file);
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
     * Retrieves the yaml data file for a player based on a given world/group name.
     *
     * @param type       Indicates whether data is for group or world.
     * @param dataName   The name of the group or world.
     * @param playerName The name of the player.
     * @return The yaml data file for a player.
     */
    File getPlayerFile(ContainerType type, String dataName, String playerName) {
        File playerFile = new File(this.getFolder(type, dataName), playerName + YML);
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                Logging.severe("Could not create necessary player file: " + playerName + YML);
                Logging.severe("Your data may not be saved!");
                Logging.severe(e.getMessage());
            }
        }
        return playerFile;
    }

    private String getPlayerName(File playerFile) {
        if (playerFile.getName().endsWith(YML)) {
            String fileName = playerFile.getName();
            return fileName.substring(0, fileName.length() - YML.length());
        } else {
            return null;
        }
    }

    /*
    private File[] getWorldFolders() {
        return this.worldFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(YML);
            }
        });
    }
    */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updatePlayerData(String dataName, PlayerProfile playerProfile) {
        File playerFile = this.getPlayerFile(playerProfile.getType(),
                dataName, playerProfile.getPlayer().getName());
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        playerData.createSection("playerData", playerProfile.serialize());
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                    + " for world: " + dataName);
            Logging.severe(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(ContainerType type, String dataName, String playerName) {
        File playerFile = this.getPlayerFile(type, dataName, playerName);
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        ConfigurationSection section = playerData.getConfigurationSection("playerData");
        if (section == null) {
            section = playerData.createSection("playerData");
        }
        return new DefaultPlayerProfile(type, dataName, playerName, convertSection(section));
    }

    @Override
    public boolean removePlayerData(ContainerType type, String dataName, String playerName) {
        File playerFile = this.getPlayerFile(type, dataName, playerName);
        return playerFile.delete();
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
}

