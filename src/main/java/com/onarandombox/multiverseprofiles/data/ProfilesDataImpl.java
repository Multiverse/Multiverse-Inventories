package com.onarandombox.multiverseprofiles.data;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseprofiles.MultiverseProfilesPlugin;
import com.onarandombox.multiverseprofiles.player.PlayerProfile;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import com.onarandombox.multiverseprofiles.world.WorldProfile;
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

    private MultiverseProfilesPlugin plugin;
    
    public ProfilesDataImpl(MultiverseProfilesPlugin plugin) throws IOException {
        this.plugin = plugin;
        // Make the data folders
        this.plugin.getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!this.dataFile.exists()) {
            this.dataFile.createNewFile();
        }

        // Load the data file into memory
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);

        // Load data into faster format
        this.loadWorlds();

        // Start the data save timer
        /*this.dataSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                MultiverseProfiles.getPlugin(), new Runnable() {
            public void run() {
                MultiverseProfiles.getData().save(false);
            }
        }, MultiverseProfiles.getConfig().getDataSaveInterval(),
                MultiverseProfiles.getConfig().getDataSaveInterval());*/
    }

    public void save(boolean isReload) {
        if (isReload) {
            this.plugin.getServer().getScheduler().cancelTask(dataSaveTaskId);
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

    private void loadWorlds() {
        List<String> worldNames = this.getData().getStringList(Path.WORLDS.getPath());
        for (String worldName : worldNames) {
            MultiverseWorld mvWorld = this.plugin.getCore().getMVWorldManager().getMVWorld(worldName);
            if (mvWorld == null) {
                ProfilesLog.warning("Did not load world data for non-Multiverse World: " + worldName);
                continue;
            }
            WorldProfile worldProfile = (WorldProfile) this.getData().get(Path.WORLDS.appendPath(worldName));
            //this.loadPlayers(worldProfile);
            this.plugin.addWorldProfile(worldProfile);
        }
    }

    private void loadPlayers(WorldProfile worldProfile) {
        List<String> playerNames = this.getData().getStringList(
                Path.WORLDS.appendPath(worldProfile.getWorld().getName()) + Path.PLAYERS.getPath()
        );
        for (String playerName : playerNames) {
            PlayerProfile playerProfile = (PlayerProfile) this.getData().get(
                    Path.WORLDS.appendPath(worldProfile.getWorld().getName()) + Path.PLAYERS.appendPath(playerName)
            );
            worldProfile.addPlayerData(playerProfile);
        }
    }
}
