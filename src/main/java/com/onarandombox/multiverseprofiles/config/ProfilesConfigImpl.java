package com.onarandombox.multiverseprofiles.config;

import com.onarandombox.multiverseprofiles.util.MinecraftTools;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author dumptruckman
 */
public class ProfilesConfigImpl implements ProfilesConfig {

    public enum Path {
        LANGUAGE_FILE_NAME("settings.local", "en", "# This is the locale file you wish to use."),
        DEBUG_MODE("settings.debug_mode.enable", false, "# Enables debug mode."),
        DATA_SAVE_PERIOD("settings.data.save_every", 120, "# This is often plugin data is written to the disk.", "# This setting indicates the maximum amount of inventory rollback possible in the event of a server crash."),

        DEFAULT_SHARING_INV("defaults.sharing.inventory", true, "# "),
        DEFAULT_SHARING_ARMOR("defaults.sharing.armor", true, "# "),
        DEFAULT_SHARING_HEALTH("defaults.sharing.health", true, "# "),
        DEFAULT_SHARING_HUNGER("defaults.sharing.hunger", true, "# "),
        DEFAULT_SHARING_EXP("defaults.sharing.experience", true, "# "),
        DEFAULT_SHARING_EFFECTS("defaults.sharing.effects", true, "# "),;

        private String path;
        private Object def;
        private String[] comments;

        Path(String path, Object def, String... comments) {
            this.path = path;
            this.def = def;
            this.comments = comments;
        }

        /**
         * Retrieves the path for a config option
         *
         * @return The path for a config option
         */
        private String getPath() {
            return path;
        }

        /**
         * Retrieves the default value for a config path
         *
         * @return The default value for a config path
         */
        private Object getDefault() {
            return def;
        }

        /**
         * Retrieves the comment for a config path
         *
         * @return The comments for a config path
         */
        private String[] getComments() {
            if (comments != null) {
                return comments;
            }

            String[] comments = new String[1];
            comments[0] = "";
            return comments;
        }
    }

    private CommentedConfiguration config;

    public ProfilesConfigImpl(JavaPlugin plugin) throws Exception {
        // Make the data folders
        plugin.getDataFolder().mkdirs();

        // Check if the config file exists.  If not, create it.
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        // Load the configuration file into memory
        config = new CommentedConfiguration(configFile);
        config.load();

        // Sets defaults config values
        this.setDefaults();

        // Saves the configuration from memory to file
        config.save();
    }

    /**
     * Loads default settings for any missing config values
     */
    private void setDefaults() {
        for (ProfilesConfigImpl.Path path : ProfilesConfigImpl.Path.values()) {
            config.addComment(path.getPath(), path.getComments());
            if (config.getString(path.getPath()) == null) {
                config.set(path.getPath(), path.getDefault());
            }
        }
    }

    private Boolean getBoolean(Path path) {
        return this.getConfig().getBoolean(path.getPath(), (Boolean) path.getDefault());
    }

    private Integer getInt(Path path) {
        return this.getConfig().getInt(path.getPath(), (Integer) path.getDefault());
    }

    private String getString(Path path) {
        return this.getConfig().getString(path.getPath(), (String) path.getDefault());
    }

    /*public void setDefaultShares() {
        MultiverseProfiles.setDefaultShares(this.getDefaultShares());
    }*/

    public FileConfiguration getConfig() {
        return this.config;
    }

    public boolean isDebugging() {
        return this.getBoolean(Path.DEBUG_MODE);
    }

    public long getDataSaveInterval() {
        return MinecraftTools.convertSecondsToTicks(this.getInt(Path.DATA_SAVE_PERIOD));
    }

    public String getLocale() {
        return this.getString(Path.LANGUAGE_FILE_NAME);
    }

    public void loadWorldGroups() {

    }
}
