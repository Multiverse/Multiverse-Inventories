package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.InventoriesConfig;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
         * First Run flag config path, default and comments.
         */
        USE_BYPASS("settings.use_bypass", false, "# If this is set to true, it will enable bypass permissions (Check the wiki for more info.)"),

        /**
         * Whether or not to make ungrouped worlds use the default group.
         */
        DEFAULT_UNGROUPED_WORLDS("settings.default_ungrouped_worlds", false, "# If set to true, any world not listed in a group will automatically use the settings for the default group!"),

        LOGGING_SAVE_LOAD("settings.save_load_on_log_in_out", false,
                "# The default and suggested setting for this is FALSE.",
                "# False means Multiverse-Inventories will not attempt to load or save any player data when they log in and out.",
                "# That means that MINECRAFT will handle that exact thing JUST LIKE IT DOES NORMALLY.",
                "# Changing this to TRUE will have Multiverse-Inventories save player data when they log out and load it when they log in.",
                "# The biggest potential drawback here is that if your server crashes, player stats/inventories may be lost/rolled back!"),
        /**
         * First Run flag config path, default and comments.
         */
        OPTIONAL_SHARES("shares.use_optionals", new ArrayList<String>(),
                "# You must specify optional shares you wish to use here or they will be ignored.",
                "# The only built in optional share is \"economy\""),
        /**
         * Whether or not to split data based on game modes.
         */
        USE_GAME_MODE_PROFILES("settings.use_game_mode_profiles", false,
                "# If this is set to true, players will have different inventories/stats for each game mode.",
                "# Please note that old data migrated to the version that has this feature will have their data copied for both game modes."),
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
                "# Worlds not listed in a group will have a separate personal inventory/stats/bed UNLESS default_ungrouped_worlds is true");

        private String path;
        private Object def;
        private List<String> comments;

        Path(String path, Object def, String... comments) {
            this.path = path;
            this.def = def;
            this.comments = Arrays.asList(comments);
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
        private List<String> getComments() {
            return this.comments;
        }
    }

    private CommentedYamlConfiguration config;
    private CommentedYamlConfiguration groupsConfig;
    private MultiverseInventories plugin;

    public CommentedInventoriesConfig(MultiverseInventories plugin) throws IOException {
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
        config = new CommentedYamlConfiguration(configFile, true);
        config.load();

        // Check if the group config file exists.  If not, create it and migrate group data.
        boolean migrateGroups = false;
        File groupConfigFile = new File(plugin.getDataFolder(), "groups.yml");
        if (!groupConfigFile.exists()) {
            Logging.fine("Created groups file.");
            groupConfigFile.createNewFile();
            migrateGroups = true;
        }

        // Load the configuration file into memory
        groupsConfig = new CommentedYamlConfiguration(groupConfigFile, true);
        groupsConfig.load();

        if (migrateGroups) {
            migrateGroups();
        }

        // Sets defaults config values
        this.setDefaults();

        config.getConfig().options().header("# Multiverse-Inventories Settings");
        groupsConfig.getConfig().options().header("# Multiverse-Inventories Groups");

        // Saves the configuration from memory to file
        groupsConfig.save();
        config.save();

        Logging.setDebugMode(this.getGlobalDebug());
    }

    private void migrateGroups() {
        ConfigurationSection section = config.getConfig().getConfigurationSection("groups");
        if (section != null) {
            groupsConfig.getConfig().set("groups", section);
            config.getConfig().set("groups", null);
            Logging.fine("Migrated groups to groups.yml");
        }
    }

    /**
     * Loads default settings for any missing config values.
     */
    private void setDefaults() {
        for (CommentedInventoriesConfig.Path path : CommentedInventoriesConfig.Path.values()) {
            if (path == Path.GROUPS) {
                continue;
            }
            config.addComment(path.getPath(), path.getComments());
            if (this.getConfig().get(path.getPath()) == null) {
                if (path.getDefault() != null) {
                    Logging.fine("Config: Defaulting '" + path.getPath() + "' to " + path.getDefault());
                    this.getConfig().set(path.getPath(), path.getDefault());
                } else {
                    this.getConfig().createSection(path.getPath());
                }
            }
        }
        groupsConfig.addComment(Path.GROUPS.getPath(), Path.GROUPS.getComments());
        if (groupsConfig.getConfig().get(Path.GROUPS.getPath()) == null) {
            this.getConfig().createSection(Path.GROUPS.getPath());
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

    private FileConfiguration getGroupsConfig() {
        return this.groupsConfig.getConfig();
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
        ConfigurationSection groupsSection = this.getGroupsConfig().getConfigurationSection("groups");
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
                        this.getGroupsConfig().getConfigurationSection("groups." + groupName);
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
    public boolean isUsingBypass() {
        return this.getBoolean(Path.USE_BYPASS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsingBypass(boolean useBypass) {
        this.getConfig().set(Path.USE_BYPASS.getPath(), useBypass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean usingLoggingSaveLoad() {
        return this.getBoolean(Path.LOGGING_SAVE_LOAD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsingLoggingSaveLoad(boolean useLoggingSaveLoad) {
        this.getConfig().set(Path.LOGGING_SAVE_LOAD.getPath(), useLoggingSaveLoad);
    }

    private Shares optionalSharables = null;

    @Override
    public Shares getOptionalShares() {
        if (this.optionalSharables == null) {
            List list = this.getConfig().getList(Path.OPTIONAL_SHARES.getPath());
            if (list != null) {
                this.optionalSharables = Sharables.fromList(list);
            } else {
                Logging.warning("'" + Path.OPTIONAL_SHARES.getPath() + "' is setup incorrectly!");
                this.optionalSharables = Sharables.noneOf();
            }
        }
        return this.optionalSharables;
    }

    @Override
    public boolean isDefaultingUngroupedWorlds() {
        return this.getBoolean(Path.DEFAULT_UNGROUPED_WORLDS);
    }

    @Override
    public void setDefaultingUngroupedWorlds(boolean useDefaultGroup) {
        this.getConfig().set(Path.FIRST_RUN.getPath(), useDefaultGroup);
    }

    @Override
    public boolean isUsingGameModeProfiles() {
        return this.getBoolean(Path.USE_GAME_MODE_PROFILES);
    }

    @Override
    public void setUsingGameModeProfiles(boolean useGameModeProfile) {
        this.getConfig().set(Path.USE_GAME_MODE_PROFILES.getPath(), useGameModeProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateWorldGroup(WorldGroupProfile worldGroup) {
        Logging.finer("Updating group in config: " + worldGroup.getName());
        this.getGroupsConfig().createSection("groups." + worldGroup.getName(), worldGroup.serialize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorldGroup(WorldGroupProfile worldGroup) {
        Logging.finer("Removing group from config: " + worldGroup.getName());
        this.getGroupsConfig().set("groups." + worldGroup.getName(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        if (this.optionalSharables != null) {
            this.getConfig().set(Path.OPTIONAL_SHARES.getPath(), this.optionalSharables.toStringList());
        }
        this.config.save();
        this.groupsConfig.save();
    }
}

