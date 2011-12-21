package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.MVIManager;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * @author dumptruckman
 */
public class MVIDataImpl implements MVIData {

    public enum Path {

        ;

        private String path;

        Path(String path) {
            this.path = path;
        }

        /**
         * Retrieves the path for a config option
         * @return The path for a config option
         */
        public String getPath() {
            return this.path;
        }
    }

    private FileConfiguration data;
    private int dataSaveTaskId = -1;
    private File dataFile = null;

    /**
     * Loads the data data into memory and sets defaults
     * @throws java.io.IOException
     */
    @Override
    public void load() throws IOException {
       // Make the data folders
        MVIManager.getPlugin().getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.dataFile = new File(MVIManager.getPlugin().getDataFolder(), MVIManager.getConfig().getLanguageFileName());
        if (!this.dataFile.exists()) {
            this.dataFile.createNewFile();
        }

        // Load the data file into memory
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);

        // Start the data save timer
        this.dataSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                MVIManager.getPlugin(), new Runnable() {
                    public void run() {
                        MVIManager.getData().save(false);
                    }
                }, MVIManager.getConfig().getDataSaveInterval(),
                MVIManager.getConfig().getDataSaveInterval());
    }

    @Override
    public void save(boolean isReload) {
        if (isReload) {
            MVIManager.getPlugin().getServer().getScheduler().cancelTask(dataSaveTaskId);
            this.dataSaveTaskId = -1;
        }
        try {
            this.data.save(this.dataFile);
        } catch (IOException e) {
            MVILog.severe("Unable to save data!");
            MVILog.severe(e.getMessage());
        }
    }
}
