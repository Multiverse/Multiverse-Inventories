package org.mvplugins.multiverse.inventories.profile.group;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.commentedconfiguration.CommentedConfiguration;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.DeserializationException;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manager class for manipulating the groups of this plugin that are contained in the groups configuration.
 */
@Service
public final class WorldGroupManager {

    static final String DEFAULT_GROUP_NAME = "default";
    private static final String[] groupSectionComments = {
            "# Multiverse-Inventories Groups",
            "",
            "# To ADD, DELETE, and EDIT groups use the command /mvinv group.",
            "# No support will be given for those who manually edit these groups."
    };

    private final Map<String, WorldGroup> groupNamesMap = new LinkedHashMap<>();
    private final MultiverseInventories inventories;
    private final WorldManager worldManager;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final InventoriesConfig config;

    private CommentedConfiguration groupsConfig;

    @Inject
    WorldGroupManager(
            final MultiverseInventories inventories,
            final ProfileContainerStoreProvider profileContainerStoreProvider,
            final InventoriesConfig config) {
        this.inventories = inventories;
        this.worldManager = inventories.getServiceLocator().getService(WorldManager.class);
        this.profileContainerStoreProvider = profileContainerStoreProvider;

        this.config = config;
    }

    public void load() throws IOException {
        // Check if the group config file exists. If not, create it and migrate group data.
        File groupsConfigFile = new File(inventories.getDataFolder(), "groups.yml");
        boolean migrateGroups = false;
        if (!groupsConfigFile.exists()) {
            Logging.fine("Created groups file.");
            groupsConfigFile.createNewFile();
            migrateGroups = true;
        }
        // Load the configuration file into memory
        groupsConfig = new CommentedConfiguration(groupsConfigFile.toPath());
        groupsConfig.load();

        if (migrateGroups) {
            migrateGroups(config.getConfig());
        }

        groupsConfig.addComment("groups", groupSectionComments);
        if (groupsConfig.get("groups") == null) {
            this.getConfig().createSection("groups");
        }

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
        return this.groupsConfig;
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
        WorldGroup profile = new WorldGroup(this, profileContainerStoreProvider, name);
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

    public void updateGroup(final WorldGroup worldGroup) {
        getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
        updateWorldGroup(worldGroup);
        save();
    }

    public boolean removeGroup(WorldGroup worldGroup) {
        if (getGroupNames().remove(worldGroup.getName().toLowerCase()) != null) {
            removeWorldGroup(worldGroup);
            save();
            return true;
        }
        return false;
    }

    /**
     * <p>Retrieves the world group associated with the given name.</p>
     *
     * These groups represent the groups that define a set of worlds and what they share.
     *
     * @param groupName Name of world group to retrieve. Casing is ignored.
     * @return The world group by the name given or null if one doesn't exist by that name.
     */
    public WorldGroup getGroup(String groupName) {
        return groupNamesMap.get(groupName.toLowerCase());
    }

    /**
     * <p>Returns a list of all the world groups defined in Multiverse-Inventories's groups configuration.</p>
     *
     * This list is unmodifiable.
     *
     * @return An unmodifiable list of all world groups.
     */
    public List<WorldGroup> getGroups() {
        return Collections.unmodifiableList(new ArrayList<WorldGroup>(getGroupNames().values()));
    }

    /**
     * Retrieves all of the world groups associated with the given world.
     *
     * @param worldName Name of the world to get groups for.
     * @return List of World Groups associated with the world or null if none.
     */
    public List<WorldGroup> getGroupsForWorld(String worldName) {
        worldName = worldName.toLowerCase();
        List<WorldGroup> worldGroups = new ArrayList<>();
        for (WorldGroup worldGroup : getGroupNames().values()) {
            if (worldGroup.containsWorld(worldName)) {
                worldGroups.add(worldGroup);
            }
        }
        // Only use the default group for worlds managed by MV-Core
        if (worldGroups.isEmpty() && config.isDefaultingUngroupedWorlds() &&
                this.worldManager.isWorld(worldName)) {
            Logging.finer("Returning default group for world: " + worldName);
            worldGroups.add(getDefaultGroup());
        }
        return worldGroups;
    }

    /**
     * Check if the given world has any configured groups.
     *
     * @param worldName Name of the world to check.
     * @return true if this world has one or more groups.
     */
    public boolean hasGroup(String worldName) {
        return !getGroupsForWorld(worldName).isEmpty();
    }

    /**
     * Retrieves all of the World Groups mapped to their names.
     *
     * @return Map of Group Name -> World Group
     */
    private Map<String, WorldGroup> getGroupNames() {
        return groupNamesMap;
    }

    /**
     * Adds a World Group to the collection in memory, also writing it to the groups configuration.
     *
     * @param worldGroup World group to add. Casing is ignored.
     * @param persist    This parameter is unused due to deprecation of the method.
     * @deprecated
     */
    @Deprecated
    public void addGroup(final WorldGroup worldGroup, final boolean persist) {
        updateGroup(worldGroup);
    }

    private void persistGroup(final WorldGroup worldGroup) {
    }

    /**
     * <p>Creates a new empty world group.</p>
     *
     * Please note if you do not add worlds to this group it will not persist very well.
     * This does not automatically persist the new group. It must bed added via {@link #updateGroup(WorldGroup)}.
     *
     * @param name A name for the new group.
     * @return The newly created world group.
     */
    public WorldGroup newEmptyGroup(String name) {
        if (getGroup(name) != null) {
            return null;
        }
        return new WorldGroup(this, profileContainerStoreProvider, name);
    }

    /**
     * Sets up the World Groups in memory.
     *
     * @param worldGroups List of World Groups to store in memory.
     * @deprecated This feature is now completely unused.
     */
    @Deprecated
    public void setGroups(List<WorldGroup> worldGroups) {
    }

    /**
     * Creates a default world group including all of the loaded MV worlds sharing everything.
     */
    public void createDefaultGroup() {
        if (getGroup(DEFAULT_GROUP_NAME) != null) {
            return;
        }
        World defaultWorld = Bukkit.getWorlds().get(0);
        World defaultNether = Bukkit.getWorld(defaultWorld.getName() + "_nether");
        World defaultEnd = Bukkit.getWorld(defaultWorld.getName() + "_the_end");
        WorldGroup worldGroup = new WorldGroup(this, profileContainerStoreProvider, DEFAULT_GROUP_NAME);
        worldGroup.getShares().mergeShares(Sharables.allOf());
        worldGroup.addWorld(defaultWorld);
        StringBuilder worlds = new StringBuilder().append(defaultWorld.getName());
        if (defaultNether != null) {
            worldGroup.addWorld(defaultNether);
            worlds.append(", ").append(defaultNether.getName());
        }
        if (defaultEnd != null) {
            worldGroup.addWorld(defaultEnd);
            worlds.append(", ").append(defaultEnd.getName());
        }
        updateGroup(worldGroup);
        config.save();
        Logging.info("Created a default group for you containing all of your default worlds: " + worlds.toString());
    }

    /**
     * @return The default world group which may be empty.
     */
    public WorldGroup getDefaultGroup() {
        WorldGroup group = getGroupNames().get(DEFAULT_GROUP_NAME);
        if (group == null) {
            group = newEmptyGroup(DEFAULT_GROUP_NAME);
            group.getShares().setSharing(Sharables.allOf(), true);
            updateGroup(group);
        }
        return group;
    }

    /**
     * Checks all the world groups to see if there are any potential issues.
     *
     * @return A list of all the potential conflicts.
     */
    public List<GroupingConflict> checkGroups() {
        List<GroupingConflict> conflicts = new ArrayList<GroupingConflict>();
        Map<WorldGroup, WorldGroup> previousConflicts = new HashMap<>();
        for (WorldGroup checkingGroup : getGroupNames().values()) {
            for (String worldName : checkingGroup.getWorlds()) {
                for (WorldGroup worldGroup : getGroupsForWorld(worldName)) {
                    if (checkingGroup.equals(worldGroup)) {
                        continue;
                    }
                    if (previousConflicts.containsKey(checkingGroup)) {
                        if (previousConflicts.get(checkingGroup).equals(worldGroup)) {
                            continue;
                        }
                    }
                    if (previousConflicts.containsKey(worldGroup)) {
                        if (previousConflicts.get(worldGroup).equals(checkingGroup)) {
                            continue;
                        }
                    }
                    previousConflicts.put(checkingGroup, worldGroup);
                    Shares conflictingShares = worldGroup.getShares()
                            .compare(checkingGroup.getShares());
                    if (!conflictingShares.isEmpty()) {
                        if (checkingGroup.getWorlds().containsAll(worldGroup.getWorlds())
                                || worldGroup.getWorlds().containsAll(checkingGroup.getWorlds())) {
                            continue;
                        }
                        conflicts.add(new GroupingConflict(checkingGroup, worldGroup,
                                Sharables.fromShares(conflictingShares)));
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * Runs a check for conflicts between groups and displays them to console and sender if not null.
     *
     * @param sender The sender to relay information to. If null, info only displayed in console.
     */
    public void checkForConflicts(CommandSender sender) {
        String message = inventories.getMessager().getMessage(Message.CONFLICT_CHECKING);
        if (sender != null) {
            inventories.getMessager().sendMessage(sender, message);
        }
        Logging.fine(message);
        List<GroupingConflict> conflicts = this.checkGroups();
        for (GroupingConflict conflict : conflicts) {
            message = inventories.getMessager().getMessage(Message.CONFLICT_RESULTS,
                    conflict.getFirstGroup().getName(), conflict.getSecondGroup().getName(),
                    conflict.getConflictingShares().toString(), conflict.getWorldsString());
            if (sender != null) {
                inventories.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        }
        if (!conflicts.isEmpty()) {
            message = inventories.getMessager().getMessage(Message.CONFLICT_FOUND);
            if (sender != null) {
                inventories.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        } else {
            message = inventories.getMessager().getMessage(Message.CONFLICT_NOT_FOUND);
            if (sender != null) {
                inventories.getMessager().sendMessage(sender, message);
            }
            Logging.fine(message);
        }
    }
}
