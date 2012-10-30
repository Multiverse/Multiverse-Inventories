package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.CommentedYamlConfiguration;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class YamlGroupManager extends AbstractGroupManager {

    private final List<String> groupSectionComments = Collections.unmodifiableList(new ArrayList<String>() {{
        add("# This is where you configure your world groups");
        add("# example below: ");
        add("#    groups:");
        add("#      example_group:");
        add("#        worlds:");
        add("#        - world1");
        add("#        - world2");
        add("#        shares:");
        add("#        - all");
        add("# In this example, world1 and world2 will share everything sharable.");
        add("# When things are shared this means they are the SAME for each world listed in the group.");
        add("# Options for shares: inventory, exp, health, hunger, beds");
        add("# Worlds not listed in a group will have a separate personal inventory/stats/bed UNLESS default_ungrouped_worlds is true");
    }});

    private final CommentedYamlConfiguration groupsConfig;

    YamlGroupManager(final Inventories inventories, final File groupConfigFile) throws IOException {
        super(inventories);

        // Check if the group config file exists.  If not, create it and migrate group data.
        boolean migrateGroups = false;
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

        groupsConfig.addComment("groups", groupSectionComments);
        if (groupsConfig.getConfig().get("groups") == null) {
            this.getConfig().createSection("groups");
        }

        groupsConfig.getConfig().options().header("# Multiverse-Inventories Groups");
        // Saves the configuration from memory to file
        groupsConfig.save();

        // Setup groups in memory
        final List<WorldGroupProfile> worldGroups = getGroupsFromConfig();
        if (worldGroups == null) {
            Logging.info("No world groups have been configured!");
            Logging.info("This will cause all worlds configured for Multiverse to have separate player statistics/inventories.");
            return;
        }

        for (final WorldGroupProfile worldGroup : worldGroups) {
            getGroupNames().put(worldGroup.getName(), worldGroup);
        }
    }

    private void migrateGroups() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("groups");
        if (section != null) {
            getConfig().set("groups", section);
            plugin.getConfig().set("groups", null);
            Logging.fine("Migrated groups to groups.yml");
        }
    }

    private FileConfiguration getConfig() {
        return this.groupsConfig.getConfig();
    }

    private List<WorldGroupProfile> getGroupsFromConfig() {
        Logging.finer("Getting world groups from config file");
        ConfigurationSection groupsSection = getConfig().getConfigurationSection("groups");
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
                        getConfig().getConfigurationSection("groups." + groupName);
                if (groupSection == null) {
                    Logging.warning("Group: '" + groupName + "' is not formatted correctly!");
                    continue;
                }
                worldGroup = newGroup(groupName, groupSection.getValues(true));
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

    private WorldGroupProfile newGroup(final String name, final Map<String, Object> dataMap) throws DeserializationException {
        return new DefaultWorldGroupProfile(this.plugin, name, dataMap);
    }

    private void updateWorldGroup(WorldGroupProfile worldGroup) {
        Logging.finer("Updating group in config: " + worldGroup.getName());
        getConfig().createSection("groups." + worldGroup.getName(), worldGroup.serialize());
    }

    private void removeWorldGroup(WorldGroupProfile worldGroup) {
        Logging.finer("Removing group from config: " + worldGroup.getName());
        getConfig().set("groups." + worldGroup.getName(), null);
    }

    private void save() {
        groupsConfig.save();
    }

    @Override
    public void updateGroup(final WorldGroupProfile worldGroup) {
        super.updateGroup(worldGroup);
        updateWorldGroup(worldGroup);
        save();
    }

    @Override
    public boolean removeGroup(WorldGroupProfile worldGroup) {
        if (super.removeGroup(worldGroup)) {
            removeWorldGroup(worldGroup);
            save();
            return true;
        }
        return false;
    }
}
