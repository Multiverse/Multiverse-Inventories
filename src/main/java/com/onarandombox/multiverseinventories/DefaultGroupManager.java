package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.GroupManager;
import com.onarandombox.multiverseinventories.group.GroupingConflict;
import com.onarandombox.multiverseinventories.group.SimpleGroupingConflict;
import com.onarandombox.multiverseinventories.group.SimpleWorldGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.MVILog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of WorldGroupManager.
 */
class DefaultGroupManager implements GroupManager {

    private HashMap<String, List<WorldGroup>> worldGroupsMap = new HashMap<String, List<WorldGroup>>();
    private HashMap<String, WorldGroup> groupNamesMap = new HashMap<String, WorldGroup>();
    private MultiverseInventories plugin;

    public DefaultGroupManager(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup getGroup(String groupName) {
        return this.groupNamesMap.get(groupName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroup> getGroups() {
        List<WorldGroup> groups = new ArrayList<WorldGroup>();
        groups.addAll(this.getGroupNames().values());
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroup> getGroupsForWorld(String worldName) {
        List<WorldGroup> worldGroups = this.getWorldGroups().get(worldName);
        if (worldGroups == null) {
            worldGroups = new ArrayList<WorldGroup>();
            this.getWorldGroups().put(worldName, worldGroups);
        }
        return worldGroups;
    }

    /**
     * Retrieves all of the World Groups mapped to each world.
     *
     * @return Map of World -> World Groups
     */
    protected HashMap<String, List<WorldGroup>> getWorldGroups() {
        return this.worldGroupsMap;
    }

    /**
     * Retrieves all of the World Groups mapped to their names.
     *
     * @return Map of Group Name -> World Group
     */
    protected HashMap<String, WorldGroup> getGroupNames() {
        return this.groupNamesMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGroup(WorldGroup worldGroup, boolean persist) {
        this.getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
        for (String worldName : worldGroup.getWorlds()) {
            List<WorldGroup> worldGroupsForWorld = this.getWorldGroups().get(worldName);
            if (worldGroupsForWorld == null) {
                worldGroupsForWorld = new ArrayList<WorldGroup>();
                this.getWorldGroups().put(worldName, worldGroupsForWorld);
            }
            worldGroupsForWorld.add(worldGroup);
        }
        this.plugin.getSettings().updateWorldGroup(worldGroup);
        if (persist) {
            this.plugin.getSettings().save();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGroup(WorldGroup worldGroup) {
        this.getGroupNames().remove(worldGroup.getName().toLowerCase());
        for (String worldName : worldGroup.getWorlds()) {
            List<WorldGroup> worldGroupsForWorld = this.getWorldGroups().get(worldName);
            if (worldGroupsForWorld != null) {
                worldGroupsForWorld.remove(worldGroup);
            }
        }
        this.plugin.getSettings().removeWorldGroup(worldGroup);
        this.plugin.getSettings().save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroups(List<WorldGroup> worldGroups) {
        if (worldGroups == null) {
            MVILog.info("No world groups have been configured!");
            MVILog.info("This will cause all worlds configured for Multiverse to have separate player statistics/inventories.");
            return;
        }

        for (WorldGroup worldGroup : worldGroups) {
            this.addGroup(worldGroup, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDefaultGroup() {
        Collection<MultiverseWorld> mvWorlds = this.plugin.getCore().getMVWorldManager().getMVWorlds();
        if (!mvWorlds.isEmpty()) {
            WorldGroup worldGroup = new SimpleWorldGroup(this.plugin, "default");
            worldGroup.setShares(new SimpleShares(true, true,
                    true, true, true));
            for (MultiverseWorld mvWorld : mvWorlds) {
                worldGroup.addWorld(mvWorld.getName());
            }
            this.addGroup(worldGroup, false);
            this.plugin.getSettings().setFirstRun(false);
            this.plugin.getSettings().save();
            MVILog.info("Created a default group for you containing all of your MV Worlds!");
        } else {
            MVILog.info("Could not configure a starter group due to no worlds being loaded into Multiverse-Core.");
            MVILog.info("Will attempt again at next start up.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroup getDefaultGroup() {
        return this.getGroupNames().get("default");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupingConflict> checkGroups() {
        List<GroupingConflict> conflicts = new ArrayList<GroupingConflict>();
        Map<WorldGroup, WorldGroup> previousConflicts = new HashMap<WorldGroup, WorldGroup>();
        for (WorldGroup checkingGroup : this.getGroupNames().values()) {
            for (String worldName : checkingGroup.getWorlds()) {
                for (WorldGroup worldGroup : this.getGroupsForWorld(worldName)) {
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
                    EnumSet<Sharable> conflictingShares = worldGroup.getShares()
                            .isSharingAnyOf(checkingGroup.getShares().getSharables());
                    if (!conflictingShares.isEmpty()) {
                        conflicts.add(new SimpleGroupingConflict(checkingGroup, worldGroup,
                                new SimpleShares(conflictingShares)));
                    }
                }
            }
        }

        return conflicts;
    }
}

