package com.onarandombox.multiverseinventories.config;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.SimpleWorldGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of MVIConfig.
 */
public class SimpleMVIConfig implements MVIConfig {

    /**
     * Enum for easily keeping track of config paths, defaults and comments.
     */
    public enum Path {
        /**
         * Locale name config path, default and comments.
         */
        LANGUAGE_FILE_NAME("settings.locale", "en", "# This is the locale you wish to use."),
        /**
         * Debug Mode config path, default and comments.
         */
        DEBUG_MODE("settings.debug_mode.enable", false, "# Enables debug mode."),
        /**
         * First Run flag config path, default and comments.
         */
        FIRST_RUN("first_run", true, "# If this is true it will generate world groups for you based on MV worlds."),
        /**
         * First Run flag config path, default and comments.
         */
        BYPASS_PERM("settings.use_bypass_permissions", true,
                "# If this is true it will allow people with sufficient permissions",
                "# to bypass world groups and/or worlds set here in config."),
        /**
         * Groups section path and comments.  No simple default for this.
         */
        GROUPS("groups", null, "# This is where you configure your world groups",
                "# example below: ",
                "# groups:",
                "#   survival_group_1:",
                "#     worlds:",
                "#     - world",
                "#     - world_nether",
                "#     - world_the_end",
                "#     shares:",
                "#     - all",
                "#   survival_group_2:",
                "#     worlds:",
                "#     - world2",
                "#     - world2_nether",
                "#     - world2_the_end",
                "#     shares:",
                "#     - all"/*,
                "#   survival_share_exp:",
                "#     worlds: ",
                "#     - all_survival",
                "#     shares:",
                "#     - experience"*/);

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

    private CommentedConfiguration config;
    private MultiverseInventories plugin;

    public SimpleMVIConfig(MultiverseInventories plugin) throws Exception {
        this.plugin = plugin;
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
     * Loads default settings for any missing config values.
     */
    private void setDefaults() {
        for (SimpleMVIConfig.Path path : SimpleMVIConfig.Path.values()) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentedConfiguration getConfig() {
        return this.config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugging() {
        return this.getBoolean(Path.DEBUG_MODE);
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
    public List<WorldGroup> getWorldGroups() {
        ConfigurationSection groupsSection = this.getConfig().getConfigurationSection("groups");
        if (groupsSection == null) {
            return null;
        }
        Set<String> groupNames = groupsSection.getKeys(false);
        List<WorldGroup> worldGroups = new ArrayList<WorldGroup>(groupNames.size());
        for (String groupName : groupNames) {
            WorldGroup worldGroup;
            try {
                ConfigurationSection groupSection =
                        this.getConfig().getConfigurationSection("groups." + groupName);
                if (groupSection == null) {
                    MVILog.warning("Group: '" + groupName + "' is not formatted correctly!");
                    continue;
                }
                worldGroup = new SimpleWorldGroup(this.plugin,
                        groupName, groupSection.getValues(true));
            } catch (DeserializationException e) {
                MVILog.warning("Unable to load world group: " + groupName);
                MVILog.warning("Reason: " + e.getMessage());
                continue;
            }
            worldGroups.add(worldGroup);
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
    public void updateWorldGroup(WorldGroup worldGroup) {
        this.getConfig().createSection("groups." + worldGroup.getName(), worldGroup.serialize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorldGroup(WorldGroup worldGroup) {
        this.getConfig().set("groups." + worldGroup.getName(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        this.getConfig().save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUsingBypassPerms() {
        return this.getBoolean(Path.BYPASS_PERM);
    }
}
