package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.collect.Lists;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.CommentedYamlConfiguration;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class YamlWorldGroupManager extends AbstractWorldGroupManager {

    private final List<String> groupSectionComments = Collections.unmodifiableList(new ArrayList<String>() {{
        add("# To ADD, DELETE, and EDIT groups use the command /mvinv group.");
        add("# No support will be given for those who manually edit these groups.");
    }});

    private final CommentedYamlConfiguration groupsConfig;

    YamlWorldGroupManager(final MultiverseInventories inventories, final File groupConfigFile, final Configuration config)
            throws IOException {
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
            migrateGroups(config);
        }

        groupsConfig.addComment("groups", groupSectionComments);
        if (groupsConfig.getConfig().get("groups") == null) {
            this.getConfig().createSection("groups");
        }

        groupsConfig.getConfig().options().header("Multiverse-Inventories Groups");
        // Saves the configuration from memory to file
        groupsConfig.save();

        // Setup groups in memory
        final List<WorldGroup> worldGroups = getGroupsFromConfig();
        if (worldGroups == null) {
            Logging.info("No world groups have been configured!");
            Logging.info("This will cause all worlds configured for Multiverse to have separate player statistics/inventories.");
            return;
        }

        for (final WorldGroup worldGroup : worldGroups) {
            getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
        }
    }

    private void migrateGroups(final Configuration config) {
        if (config == null) {
            return;
        }
        ConfigurationSection section = config.getConfigurationSection("groups");
        if (section != null) {
            getConfig().set("groups", section);
            config.set("groups", null);
            Logging.fine("Migrated groups to groups.yml");
        }
    }

    private FileConfiguration getConfig() {
        return this.groupsConfig.getConfig();
    }

    private List<WorldGroup> getGroupsFromConfig() {
        Logging.finer("Getting world groups from config file");
        ConfigurationSection groupsSection = getConfig().getConfigurationSection("groups");
        if (groupsSection == null) {
            Logging.finer("Could not find a 'groups' section in config!");
            return null;
        }
        Set<String> groupNames = groupsSection.getKeys(false);
        Logging.finer("Loading groups: " + groupNames.toString());
        List<WorldGroup> worldGroups = new ArrayList<>(groupNames.size());
        for (String groupName : groupNames) {
            Logging.finer("Attempting to load group: " + groupName + "...");
            WorldGroup worldGroup;
            try {
                ConfigurationSection groupSection =
                        getConfig().getConfigurationSection("groups." + groupName);
                if (groupSection == null) {
                    Logging.warning("Group: '" + groupName + "' is not formatted correctly!");
                    continue;
                }
                worldGroup = deserializeGroup(groupName, groupSection.getValues(true));
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

    private WorldGroup deserializeGroup(final String name, final Map<String, Object> dataMap)
            throws DeserializationException {
        WorldGroup profile = new WorldGroup(this.plugin, name);
        if (dataMap.containsKey("worlds")) {
            Object worldListObj = dataMap.get("worlds");
            if (worldListObj == null) {
                Logging.fine("No worlds for group: " + name);
            } else {
                if (!(worldListObj instanceof List)) {
                    Logging.fine("World list formatted incorrectly for world group: " + name);
                } else {
                    final StringBuilder builder = new StringBuilder();
                    for (Object worldNameObj : (List) worldListObj) {
                        if (worldNameObj == null) {
                            Logging.fine("Error with a world listed in group: " + name);
                            continue;
                        }
                        profile.addWorld(worldNameObj.toString(), false);
                        World world = Bukkit.getWorld(worldNameObj.toString());
                        if (world == null) {
                            if (builder.length() != 0) {
                                builder.append(", ");
                            }
                            builder.append(worldNameObj.toString());
                        }
                    }
                    if (builder.length() > 0) {
                        Logging.config("The following worlds for group '%s' are not loaded: %s", name, builder.toString());
                    }
                }
            }
        }
        if (dataMap.containsKey("shares")) {
            Object sharesListObj = dataMap.get("shares");
            if (sharesListObj instanceof List) {
                profile.getShares().mergeShares(Sharables.fromList((List) sharesListObj));
                profile.getShares().removeAll(Sharables.negativeFromList((List) sharesListObj));
            } else {
                Logging.warning("Shares formatted incorrectly for group: " + name);
            }
        }
        if (dataMap.containsKey("spawn")) {
            Object spawnPropsObj = dataMap.get("spawn");
            if (spawnPropsObj instanceof ConfigurationSection) {
                // Le sigh, bukkit.
                spawnPropsObj = ((ConfigurationSection) spawnPropsObj).getValues(true);
            }
            if (spawnPropsObj instanceof Map) {
                Map spawnProps = (Map) spawnPropsObj;
                if (spawnProps.containsKey("world")) {
                    profile.setSpawnWorld(spawnProps.get("world").toString());
                }
                if (spawnProps.containsKey("priority")) {
                    EventPriority priority = EventPriority.valueOf(
                            spawnProps.get("priority").toString().toUpperCase());
                    if (priority != null) {
                        profile.setSpawnPriority(priority);
                    }
                }
            } else {
                Logging.warning("Spawn settings for group formatted incorrectly");
            }
        }
        return profile;
    }

    private void updateWorldGroup(WorldGroup worldGroup) {
        Logging.finer("Updating group in config: " + worldGroup.getName());
        getConfig().createSection("groups." + worldGroup.getName(), serializeWorldGroupProfile(worldGroup));
    }

    private Map<String, Object> serializeWorldGroupProfile(WorldGroup profile) {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("worlds", Lists.newArrayList(profile.getWorlds()));
        List<String> sharesList = profile.getShares().toStringList();
        if (!sharesList.isEmpty()) {
            results.put("shares", sharesList);
        }
        Map<String, Object> spawnProps = new LinkedHashMap<String, Object>();
        if (profile.getSpawnWorld() != null) {
            spawnProps.put("world", profile.getSpawnWorld());
            spawnProps.put("priority", profile.getSpawnPriority().toString());
            results.put("spawn", spawnProps);
        }
        return results;
    }

    private void removeWorldGroup(WorldGroup worldGroup) {
        Logging.finer("Removing group from config: " + worldGroup.getName());
        getConfig().set("groups." + worldGroup.getName(), null);
    }

    private void save() {
        groupsConfig.save();
    }

    @Override
    public void updateGroup(final WorldGroup worldGroup) {
        super.updateGroup(worldGroup);
        updateWorldGroup(worldGroup);
        save();
    }

    @Override
    public boolean removeGroup(WorldGroup worldGroup) {
        if (super.removeGroup(worldGroup)) {
            removeWorldGroup(worldGroup);
            save();
            return true;
        }
        return false;
    }
}
