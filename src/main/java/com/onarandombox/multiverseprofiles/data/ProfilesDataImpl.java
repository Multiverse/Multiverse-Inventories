package com.onarandombox.multiverseprofiles.data;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseprofiles.MultiverseProfiles;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author dumptruckman
 */
public class ProfilesDataImpl implements ProfilesData {

    public enum Path {
        WORLDS("worldData"),
        PLAYERS(".playerdata"),;

        private String path;

        Path(String path) {
            this.path = path;
        }

        /**
         * Retrieves the path for a config option
         *
         * @return The path for a config option
         */
        public String getPath() {
            return this.path;
        }

        public String appendPath(String suffix) {
            return this.getPath() + "." + suffix;
        }
    }

    private FileConfiguration data;
    private int dataSaveTaskId = -1;
    private File dataFile = null;

    /**
     * Loads the data data into memory and sets defaults
     *
     * @throws java.io.IOException
     */
    public void load(MultiverseProfiles mvProfiles) throws IOException {
        // Make the data folders
        MultiverseProfiles.getPlugin().getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.dataFile = new File(MultiverseProfiles.getPlugin().getDataFolder(), MultiverseProfiles.getConfig().getLanguageFileName());
        if (!this.dataFile.exists()) {
            this.dataFile.createNewFile();
        }

        // Load the data file into memory
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);

        // Load data into faster format
        this.loadDataIntoMemory();

        // Start the data save timer
        this.dataSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                MultiverseProfiles.getPlugin(), new Runnable() {
            public void run() {
                MultiverseProfiles.getData().save(false);
            }
        }, MultiverseProfiles.getConfig().getDataSaveInterval(),
                MultiverseProfiles.getConfig().getDataSaveInterval());
    }

    public void save(boolean isReload) {
        if (isReload) {
            MultiverseProfiles.getPlugin().getServer().getScheduler().cancelTask(dataSaveTaskId);
            this.dataSaveTaskId = -1;
        }
        try {
            this.data.save(this.dataFile);
        } catch (IOException e) {
            ProfilesLog.severe("Unable to save data!");
            ProfilesLog.severe(e.getMessage());
        }
    }

    public FileConfiguration getData() {
        return this.data;
    }

    private void loadDataIntoMemory() {
        this.loadWorlds();
    }

    private void loadWorlds() {
        List<String> worldNames = this.getData().getStringList(Path.WORLDS.getPath());
        for (String worldName : worldNames) {
            MultiverseWorld mvWorld = MultiverseProfiles.getCore().getMVWorldManager().getMVWorld(worldName);
            if (mvWorld == null) {
                ProfilesLog.warning("Did not load world data for non-Multiverse World: " + worldName);
                continue;
            }
            WorldProfile worldProfile = (WorldProfile) this.getData().get(Path.WORLDS.appendPath(worldName));
            //this.loadPlayers(worldProfile);
            MultiverseProfiles.addWorldProfile(worldProfile);
        }
    }

    private void loadPlayers(WorldProfile worldProfile) {
        List<String> playerNames = this.getData().getStringList(
                Path.WORLDS.appendPath(worldProfile.getMVWorld().getName()) + Path.PLAYERS.getPath()
        );
        for (String playerName : playerNames) {
            PlayerProfile playerProfile = (PlayerProfile) this.getData().get(
                    Path.WORLDS.appendPath(worldProfile.getMVWorld().getName()) + Path.PLAYERS.appendPath(playerName)
            );
            worldProfile.addPlayerData(playerProfile);
        }
    }
}
