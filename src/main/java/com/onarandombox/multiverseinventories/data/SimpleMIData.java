package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.SimpleWorldProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MILog;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author dumptruckman
 */
public class SimpleMIData implements MIData {

    private FileConfiguration data;
    private File dataFile = null;

    public SimpleMIData(JavaPlugin plugin) throws IOException {
        // Make the data folders
        plugin.getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.dataFile = new File(plugin.getDataFolder(), "worldData.yml");
        if (!this.dataFile.exists()) {
            this.dataFile.createNewFile();
        }

        // Load the data file into memory
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);
    }

    public void save() {
        try {
            this.getData().save(this.dataFile);
        } catch (IOException e) {
            MILog.severe("Unable to save data!");
            MILog.severe(e.getMessage());
        }
    }

    public FileConfiguration getData() {
        return this.data;
    }

    public List<WorldProfile> getWorldProfiles() {
        List<WorldProfile> worldProfiles = new ArrayList<WorldProfile>();
        Set<String> worldNames = this.getData().getKeys(false);
        if (worldNames == null) {
            MILog.info("No world data to load");
            return worldProfiles;
        }
        for (String worldName : worldNames) {
            ConfigurationSection worldProfileSection = this.getData().getConfigurationSection(worldName);
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
