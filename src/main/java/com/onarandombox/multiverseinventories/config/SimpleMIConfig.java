package com.onarandombox.multiverseinventories.config;

import com.onarandombox.multiverseinventories.group.SimpleWorldGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MILog;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author dumptruckman
 */
public class SimpleMIConfig implements MIConfig {

    public enum Path {
        LANGUAGE_FILE_NAME("settings.locale", "en", "# This is the locale you wish to use."),
        DEBUG_MODE("settings.debug_mode.enable", false, "# Enables debug mode."),
        FIRST_RUN("first_run", true, "# If this is true it will generate world groups for you based on MV worlds."),
        GROUPS("groups", null, "#This is where you configure your world groups"),;

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

    public SimpleMIConfig(JavaPlugin plugin) throws Exception {
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
        for (SimpleMIConfig.Path path : SimpleMIConfig.Path.values()) {
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

    @Override
    public CommentedConfiguration getConfig() {
        return this.config;
    }

    @Override
    public boolean isDebugging() {
        return this.getBoolean(Path.DEBUG_MODE);
    }

    @Override
    public String getLocale() {
        return this.getString(Path.LANGUAGE_FILE_NAME);
    }

    @Override
    public List<WorldGroup> getWorldGroups() {
        if (!this.getConfig().contains("groups")) {
            return null;
        }
        ConfigurationSection groupsSection = this.getConfig().getConfigurationSection("groups");
        Set<String> groupNames = groupsSection.getKeys(false);
        List<WorldGroup> worldGroups = new ArrayList<WorldGroup>(groupNames.size());
        for (String groupName : groupNames) {
            WorldGroup worldGroup;
            try {
                worldGroup = new SimpleWorldGroup(groupName,
                        groupsSection.getConfigurationSection(groupName));
            } catch (DeserializationException e) {
                MILog.warning("Unable to load world group: " + groupName);
                MILog.warning("Reason: " + e.getMessage());
                continue;
            }
            worldGroups.add(worldGroup);
        }
        return worldGroups;
    }

    @Override
    public boolean isFirstRun() {
        return this.getBoolean(Path.FIRST_RUN);
    }

    @Override
    public void setFirstRun(boolean firstRun) {
        this.getConfig().set(Path.FIRST_RUN.getPath(), firstRun);
    }

    @Override
    public void updateWorldGroup(WorldGroup worldGroup) {
        ConfigurationSection groupSection = this.getConfig().getConfigurationSection("groups." + worldGroup.getName());
        if (groupSection == null) {
            groupSection = this.getConfig().createSection("groups." + worldGroup.getName());
        }
        worldGroup.serialize(groupSection);
    }

    @Override
    public void save() {
        this.getConfig().save();
    }
}
