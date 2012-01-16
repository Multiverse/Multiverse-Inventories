package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.SimplePlayerProfile;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of MVIData.
 */
public class FlatfileMVIData implements MVIData {

    private static final String YML = ".yml";
    private File dataFolder = null;

    public FlatfileMVIData(JavaPlugin plugin) throws IOException {
        // Make the data folders
        plugin.getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!this.dataFolder.exists()) {
            if (!this.dataFolder.mkdirs()) {
                throw new IOException("Could not create data folder!");
            }
        }
    }

    private FileConfiguration getConfigHandle(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    private File getWorldFolder(String worldName) {
        File worldFile = new File(this.dataFolder, worldName);
        if (!worldFile.exists()) {
            worldFile.mkdirs();
        }
        return worldFile;
    }

    private File getPlayerFile(String worldName, String playerName) {
        File playerFile = new File(this.getWorldFolder(worldName), playerName + YML);
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                MVILog.severe("Could not create necessary player file: " + playerName + YML);
                MVILog.severe("Your data may not be saved!");
                MVILog.severe(e.getMessage());
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
        return this.dataFolder.listFiles(new FilenameFilter() {
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
    public boolean updatePlayerData(String worldName, PlayerProfile playerProfile) {
        File playerFile = this.getPlayerFile(worldName, playerProfile.getPlayer().getName());
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        playerData.createSection("playerData", playerProfile.serialize());
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            MVILog.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                    + " for world: " + worldName);
            MVILog.severe(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(String worldName, String playerName) {
        File playerFile = this.getPlayerFile(worldName, playerName);
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        ConfigurationSection section = playerData.getConfigurationSection("playerData");
        if (section == null) {
            section = playerData.createSection("playerData");
        }
        return new SimplePlayerProfile(playerName,  section.getValues(true));
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    /*
    public List<WorldProfile> getWorldProfiles() {
        List<WorldProfile> worldProfiles = new ArrayList<WorldProfile>();
        File[] worldFiles = this.getWorldFolders();
        if (worldFiles.length < 1) {
            MVILog.info("No world data to load");
            return worldProfiles;
        }
        for (File worldFile : worldFiles) {
            String worldName = this.getWorldName(worldFile);
            if (worldName == null) {
                // non-yaml file detected
                continue;
            }
            ConfigurationSection worldProfileSection = this.getConfigHandle(worldFile);
            if (worldProfileSection != null) {
                try {
                    WorldProfile worldProfile = new WeakWorldProfile(worldName, worldProfileSection);
                    worldProfiles.add(worldProfile);
                } catch (DeserializationException e) {
                    MVILog.warning("Unable to load world data for world: " + worldName);
                    MVILog.warning("Reason: " + e.getMessage());
                    continue;
                }
            } else {
                MVILog.warning("Problem loading world data!");
            }
        }
        return worldProfiles;
    }
    */
}
