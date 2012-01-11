package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.SimpleWorldProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MIDebug;
import com.onarandombox.multiverseinventories.util.MILog;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MIData.
 */
public class FlatfileMIData implements MIData {

    private static final String YML = ".yml";
    private File dataFolder = null;

    public FlatfileMIData(JavaPlugin plugin) throws IOException {
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

    private FileConfiguration getWorldConfig(File worldFile) {
        return YamlConfiguration.loadConfiguration(worldFile);
    }

    private File getWorldFile(String worldName) {
        File worldFile = new File(this.dataFolder, worldName + YML);
        if (worldFile.exists()) {
            try {
                worldFile.createNewFile();
            } catch (IOException e) {
                MILog.severe("Could not create necessary world file: " + worldName + YML);
                MILog.severe("You data may not save!");
                MILog.severe(e.getMessage());
            }
        }
        return worldFile;
    }

    private String getWorldName(File worldFile) {
        if (worldFile.getName().endsWith(YML)) {
            String fileName = worldFile.getName();
            return fileName.substring(0, fileName.length() - YML.length());
        } else {
            return null;
        }
    }

    private File[] getWorldFiles() {
        return this.dataFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(YML);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updatePlayerData(WorldProfile worldProfile, PlayerProfile playerProfile) {
        String playerDataPath = getPlayerDataString(worldProfile, playerProfile);
        File worldFile = this.getWorldFile(worldProfile.getWorld());
        FileConfiguration worldData = this.getWorldConfig(worldFile);
        ConfigurationSection section = worldData.getConfigurationSection(playerDataPath);
        if (section == null) {
            section = worldData.createSection(playerDataPath);
        }
        playerProfile.serialize(section);
        try {
            worldData.save(worldFile);
        } catch (IOException e) {
            MILog.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                    + " for world: " + worldProfile.getWorld());
            MILog.severe(e.getMessage());
            return false;
        }
        return true;
    }

    private String getPlayerDataString(WorldProfile worldProfile, PlayerProfile playerProfile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(worldProfile.getWorld());
        stringBuilder.append(".playerData.");
        stringBuilder.append(playerProfile.getPlayer().getName());
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldProfile> getWorldProfiles() {
        List<WorldProfile> worldProfiles = new ArrayList<WorldProfile>();
        File[] worldFiles = this.getWorldFiles();
        if (worldFiles.length < 1) {
            MILog.info("No world data to load");
            return worldProfiles;
        }
        for (File worldFile : worldFiles) {
            String worldName = this.getWorldName(worldFile);
            if (worldName == null) {
                // non-yaml file detected
                continue;
            }
            ConfigurationSection worldProfileSection = this.getWorldConfig(worldFile);
            if (worldProfileSection != null) {
                try {
                    WorldProfile worldProfile = new SimpleWorldProfile(worldName, worldProfileSection);
                    worldProfiles.add(worldProfile);
                } catch (DeserializationException e) {
                    MILog.warning("Unable to load world data for world: " + worldName);
                    MILog.warning("Reason: " + e.getMessage());
                    continue;
                }
            } else {
                MILog.warning("Problem loading world data!");
            }
        }
        return worldProfiles;
    }
}
