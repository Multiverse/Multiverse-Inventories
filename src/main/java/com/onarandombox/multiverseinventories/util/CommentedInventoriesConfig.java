package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.InventoriesConfig;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of Config.
 */
public class CommentedInventoriesConfig implements InventoriesConfig {

    /**
     * Enum for easily keeping track of config paths, defaults and comments.
     */
    public enum Path {
        /**
         * Add a comment to the top of file.
         */
        SETTINGS("settings", null, "# ===[ Multiverse Inventories Config ]==="),
        /**
         * Locale name config path, default and comments.
         */
        LANGUAGE_FILE_NAME("settings.locale", "en", "# This is the locale you wish to use."),
        /**
         * Debug Mode config path, default and comments.
         */
        DEBUG_LEVEL("settings.debug_level", 0, "# Level of debugging information to display.", "# 0 = off, "
                + "1-3 increasing amount of debug spam."),
        /**
         * First Run flag config path, default and comments.
         */
        FIRST_RUN("settings.first_run", true, "# If this is true it will generate world groups for you based on MV worlds."),
        /**
         * Groups section path and comments.  No simple default for this.
         */
        GROUPS("groups", null, "# This is where you configure your world groups",
                "# example below: ",
                "#    groups:",
                "#      example_group:",
                "#        worlds:",
                "#        - world1",
                "#        - world2",
                "#        shares:",
                "#        - all",
                "# In this example, world1 and world2 will share everything sharable.",
                "# When things are shared this means they are the SAME for each world listed in the group.",
                "# Options for shares: inventory, exp, health, hunger, beds",
                "# Worlds not listed in a group will have a separate personal inventory/stats/bed.");

        private String path;
        private Object def;
        private String[] comments;

        Path(String path, Object def, String... comments) {
            this.path = path;
            this.def = def;
            this.comments = comments;
        }

        /**
         * Retrieves the path for a config option.
         *
         * @return The path for a config option.
         */
        private String getPath() {
            return this.path;
        }

        /**
         * Retrieves the default value for a config path.
         *
         * @return The default value for a config path.
         */
        private Object getDefault() {
            return this.def;
        }

        /**
         * Retrieves the comment for a config path.
         *
         * @return The comments for a config path.
         */
        private String[] getComments() {
            if (this.comments != null) {
                return this.comments;
            }

            String[] emptyComments = new String[1];
            emptyComments[0] = "";
            return emptyComments;
        }
    }

    private CommentedYamlConfiguration config;
    private MultiverseInventories plugin;

    public CommentedInventoriesConfig(MultiverseInventories plugin) throws Exception {
        this.plugin = plugin;
        // Make the data folders
        if (plugin.getDataFolder().mkdirs()) {
            Logging.fine("Created data folder.");
        }

        // Check if the config file exists.  If not, create it.
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Logging.fine("Created config file.");
            configFile.createNewFile();
        }

        // Load the configuration file into memory
        config = new CommentedYamlConfiguration(configFile);
        config.load();

        // Sets defaults config values
        this.setDefaults();

        // Saves the configuration from memory to file
        config.save();

        Logging.setDebugMode(this.getGlobalDebug());
    }

    /**
     * Loads default settings for any missing config values.
     */
    private void setDefaults() {
        for (CommentedInventoriesConfig.Path path : CommentedInventoriesConfig.Path.values()) {
            config.addComment(path.getPath(), path.getComments());
            if (this.getConfig().get(path.getPath()) == null) {
                if (path.getDefault() != null) {
                    Logging.fine("Config: Defaulting '" + path.getPath() + "' to " + path.getDefault());
                    this.getConfig().set(path.getPath(), path.getDefault());
                }
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

    private FileConfiguration getConfig() {
        return this.config.getConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGlobalDebug(int globalDebug) {
        this.getConfig().set(Path.DEBUG_LEVEL.getPath(), globalDebug);
        Logging.setDebugMode(globalDebug);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGlobalDebug() {
        return this.getInt(Path.DEBUG_LEVEL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocale() {
        return this.getString(Path.LANGUAGE_FILE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroupProfile> getWorldGroups() {
        Logging.finer("Getting world groups from config file");
        ConfigurationSection groupsSection = this.getConfig().getConfigurationSection("groups");
        if (groupsSection == null) {
            Logging.finer("Could not find a 'groups' section in config!");
            return null;
        }
        Set<String> groupNames = groupsSection.getKeys(false);
        Logging.finer("Loading groups: " + groupNames.toString());
        List<WorldGroupProfile> worldGroups = new ArrayList<WorldGroupProfile>(groupNames.size());
        for (String groupName : groupNames) {
            Logging.finer("Attempting to load group: " + groupName + "...");
            WorldGroupProfile worldGroup;
            try {
                ConfigurationSection groupSection =
                        this.getConfig().getConfigurationSection("groups." + groupName);
                if (groupSection == null) {
                    Logging.warning("Group: '" + groupName + "' is not formatted correctly!");
                    continue;
                }
                worldGroup = this.plugin.getGroupManager().newGroupFromMap(groupName, groupSection.getValues(true));
            } catch (DeserializationException e) {
                Logging.warning("Unable to load world group: " + groupName);
                Logging.warning("Reason: " + e.getMessage());
                continue;
            }
            worldGroups.add(worldGroup);
            Logging.finer("Group: " + worldGroup.getName() + " added to memory");
        }
        return worldGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirstRun() {
        return this.getBoolean(Path.FIRST_RUN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstRun(boolean firstRun) {
        this.getConfig().set(Path.FIRST_RUN.getPath(), firstRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateWorldGroup(WorldGroupProfile worldGroup) {
        Logging.finer("Updating group in config: " + worldGroup.getName());
        this.getConfig().createSection("groups." + worldGroup.getName(), worldGroup.serialize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorldGroup(WorldGroupProfile worldGroup) {
        Logging.finer("Removing group from config: " + worldGroup.getName());
        this.getConfig().set("groups." + worldGroup.getName(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        this.config.save();
    }
}

