package org.mvplugins.multiverse.inventories.profile.group;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.event.world.MVWorldLoadedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldUnloadedEvent;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.Tuple;
import org.mvplugins.multiverse.external.vavr.Tuple2;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

/**
 * Abstract implementation of GroupManager with no persistence of groups.
 */
@Contract
abstract sealed class AbstractWorldGroupManager implements WorldGroupManager permits YamlWorldGroupManager {

    static final String DEFAULT_GROUP_NAME = "default";
    protected final Map<String, WorldGroup> groupNamesMap = new LinkedHashMap<>();
    protected final MultiverseInventories plugin;
    protected final MVCommandManager commandManager;
    protected final InventoriesConfig inventoriesConfig;
    protected final ProfileContainerStoreProvider profileContainerStoreProvider;
    protected final WorldManager worldManager;

    AbstractWorldGroupManager(
            @NotNull MultiverseInventories plugin,
            @NotNull MVCommandManager commandManager,
            @NotNull InventoriesConfig config,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
        this.inventoriesConfig = config;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.worldManager = worldManager;

        Bukkit.getPluginManager().registerEvents(new WorldChangeListener(), plugin);
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
        return List.copyOf(getGroupNames().values());
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
        if (worldGroups.isEmpty() && inventoriesConfig.getDefaultUngroupedWorlds() &&
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
                .anyMatch(worldGroup -> worldGroup.containsWorld(worldName));
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
        worldGroup.recalculateApplicableWorlds();
        worldGroup.recalculateApplicableShares();
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
        return new WorldGroup(this, profileContainerStoreProvider, inventoriesConfig, name);
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
        WorldGroup worldGroup = new WorldGroup(this, profileContainerStoreProvider, inventoriesConfig, DEFAULT_GROUP_NAME);
        worldGroup.getShares().mergeShares(Sharables.allOf());
        worldGroup.addWorld(defaultWorld);
        if (defaultNether != null) {
            worldGroup.addWorld(defaultNether);
        }
        if (defaultEnd != null) {
            worldGroup.addWorld(defaultEnd);
        }
        updateGroup(worldGroup);
        Logging.info("Created a default group for you containing all of your default worlds: "
                + String.join(", ", worldGroup.getConfigWorlds()));
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
    public GroupingConflictResult checkForConflicts() {
        List<GroupingConflict> conflicts = new ArrayList<>();
        Set<Tuple2<WorldGroup, WorldGroup>> checkedPairs = new HashSet<>();
        for (WorldGroup checkingGroup : getGroupNames().values()) {
            for (String worldName : checkingGroup.getApplicableWorlds()) {
                for (WorldGroup worldGroup : getGroupsForWorld(worldName)) {
                    checkConflict(checkingGroup, worldGroup, checkedPairs, conflicts);
                }
            }
        }
        return new GroupingConflictResult(conflicts);
    }

    private void checkConflict(WorldGroup checkingGroup,
                               WorldGroup worldGroup,
                               Set<Tuple2<WorldGroup, WorldGroup>> checkedPairs,
                               List<GroupingConflict> conflicts) {
        if (checkingGroup.equals(worldGroup)) {
            // Don't check against itself.
            return;
        }
        if (checkedPairs.contains(new Tuple2<>(checkingGroup, worldGroup))) {
            // Already checked this pair.
            return;
        }
        if (checkedPairs.contains(new Tuple2<>(worldGroup, checkingGroup))) {
            // Already checked this pair in the opposite order.
            return;
        }
        Logging.finer("Checking conflict between %s and %s", checkingGroup.getName(), worldGroup.getName());
        checkedPairs.add(new Tuple2<>(checkingGroup, worldGroup));
        Shares conflictingShares = worldGroup.getApplicableShares().compare(checkingGroup.getApplicableShares());
        if (conflictingShares.isEmpty()) {
            // No overlapping shares.
            return;
        }
        Logging.finer("Conflict found for %s and %s", checkingGroup.getName(), worldGroup.getName());
        conflicts.add(new GroupingConflict(checkingGroup, worldGroup, Sharables.fromShares(conflictingShares)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupingConflict> checkGroups() {
        return checkForConflicts().getConflicts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForConflicts(MVCommandIssuer issuer) {
        if (issuer == null) {
            issuer = commandManager.getCommandIssuer(Bukkit.getConsoleSender());
        }
        issuer.sendInfo(MVInvi18n.CONFLICT_CHECKING);
        GroupingConflictResult groupingConflictResult = checkForConflicts();
        if (groupingConflictResult.hasConflict()) {
            groupingConflictResult.sendConflictIssue(issuer);
        } else {
            issuer.sendInfo(MVInvi18n.CONFLICT_NOTFOUND);
        }
    }

    @Override
    public void recalculateApplicableShares() {
        getGroupNames().values().forEach(WorldGroup::recalculateApplicableShares);
    }

    @Override
    public void recalculateApplicableWorlds() {
        groupNamesMap.values().forEach(WorldGroup::recalculateApplicableWorlds);
    }

    private class WorldChangeListener implements Listener {
        @EventHandler
        void onMVWorldLoad(MVWorldLoadedEvent event) {
            groupNamesMap.values().forEach(group -> group.addApplicableWorld(event.getWorld().getName()));
        }

        @EventHandler
        void onMVWorldUnload(MVWorldUnloadedEvent event) {
            groupNamesMap.values().forEach(group -> group.removeApplicableWorld(event.getWorld().getName()));
        }

        @EventHandler
        void onWorldLoad(WorldLoadEvent event) {
            groupNamesMap.values().forEach(group -> group.addApplicableWorld(event.getWorld().getName()));
        }

        @EventHandler
        void onWorldUnload(WorldUnloadEvent event) {
            groupNamesMap.values().forEach(group -> group.removeApplicableWorld(event.getWorld().getName()));
        }
    }
}
