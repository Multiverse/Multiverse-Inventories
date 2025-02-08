package org.mvplugins.multiverse.inventories.profile.group;

import com.dumptruckman.minecraft.util.Logging;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract implementation of GroupManager with no persistence of groups.
 */
@Contract
abstract sealed class AbstractWorldGroupManager implements WorldGroupManager permits YamlWorldGroupManager {

    static final String DEFAULT_GROUP_NAME = "default";
    protected final Map<String, WorldGroup> groupNamesMap = new LinkedHashMap<>();
    protected final MultiverseInventories plugin;
    protected final InventoriesConfig inventoriesConfig;
    protected final ProfileContainerStoreProvider profileContainerStoreProvider;
    protected final WorldManager worldManager;

    public AbstractWorldGroupManager(
            @NotNull MultiverseInventories plugin,
            @NotNull InventoriesConfig config,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.inventoriesConfig = config;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.worldManager = worldManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup getGroup(String groupName) {
        return groupNamesMap.get(groupName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroup> getGroups() {
        return Collections.unmodifiableList(new ArrayList<WorldGroup>(getGroupNames().values()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroup> getGroupsForWorld(String worldName) {
        worldName = worldName.toLowerCase();
        List<WorldGroup> worldGroups = new ArrayList<>();
        for (WorldGroup worldGroup : getGroupNames().values()) {
            if (worldGroup.containsWorld(worldName)) {
                worldGroups.add(worldGroup);
            }
        }
        // Only use the default group for worlds managed by MV-Core
        if (worldGroups.isEmpty() && inventoriesConfig.isDefaultingUngroupedWorlds() &&
                this.worldManager.isWorld(worldName)) {
            Logging.finer("Returning default group for world: " + worldName);
            worldGroups.add(getDefaultGroup());
        }
        return worldGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasConfiguredGroup(String worldName) {
        return groupNamesMap.values().stream()
                .anyMatch(worldGroup -> worldGroup.getWorlds().contains(worldName));
    }

    /**
     * Retrieves all of the World Groups mapped to their names.
     *
     * @return Map of Group Name -> World Group
     */
    protected Map<String, WorldGroup> getGroupNames() {
        return groupNamesMap;
    }

    @Override
    public void updateGroup(final WorldGroup worldGroup) {
        getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeGroup(final WorldGroup worldGroup) {
        return getGroupNames().remove(worldGroup.getName().toLowerCase()) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup newEmptyGroup(String name) {
        if (getGroup(name) != null) {
            return null;
        }
        return new WorldGroup(this, profileContainerStoreProvider, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDefaultGroup() {
        if (getGroup(DEFAULT_GROUP_NAME) != null) {
            return;
        }
        World defaultWorld = worldManager.getDefaultWorld()
                .flatMap(LoadedMultiverseWorld::getBukkitWorld)
                .fold(() -> Bukkit.getWorlds().get(0), world -> world);
        World defaultNether = Bukkit.getWorld(defaultWorld.getName() + "_nether");
        World defaultEnd = Bukkit.getWorld(defaultWorld.getName() + "_the_end");
        WorldGroup worldGroup = new WorldGroup(this, profileContainerStoreProvider, DEFAULT_GROUP_NAME);
        worldGroup.getShares().mergeShares(Sharables.allOf());
        worldGroup.addWorld(defaultWorld);
        if (defaultNether != null) {
            worldGroup.addWorld(defaultNether);
        }
        if (defaultEnd != null) {
            worldGroup.addWorld(defaultEnd);
        }
        updateGroup(worldGroup);
        inventoriesConfig.save();
        Logging.info("Created a default group for you containing all of your default worlds: "
                + String.join(", ", worldGroup.getWorlds()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public void checkForConflicts(CommandSender sender) {
        String message = plugin.getMessager().getMessage(Message.CONFLICT_CHECKING);
        if (sender != null) {
            plugin.getMessager().sendMessage(sender, message);
        }
        Logging.fine(message);
        List<GroupingConflict> conflicts = checkGroups();
        for (GroupingConflict conflict : conflicts) {
            message = plugin.getMessager().getMessage(Message.CONFLICT_RESULTS,
                    conflict.getFirstGroup().getName(), conflict.getSecondGroup().getName(),
                    conflict.getConflictingShares().toString(), conflict.getWorldsString());
            if (sender != null) {
                plugin.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        }
        if (!conflicts.isEmpty()) {
            message = plugin.getMessager().getMessage(Message.CONFLICT_FOUND);
            if (sender != null) {
                plugin.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        } else {
            message = plugin.getMessager().getMessage(Message.CONFLICT_NOT_FOUND);
            if (sender != null) {
                plugin.getMessager().sendMessage(sender, message);
            }
            Logging.fine(message);
        }
    }
}

