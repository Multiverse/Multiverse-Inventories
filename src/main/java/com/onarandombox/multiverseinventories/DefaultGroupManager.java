package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.GroupingConflict;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of WorldGroupManager.
 */
final class DefaultGroupManager implements GroupManager {

    private HashMap<String, List<WorldGroupProfile>> worldGroupsMap = new HashMap<String, List<WorldGroupProfile>>();
    private HashMap<String, WorldGroupProfile> groupNamesMap = new HashMap<String, WorldGroupProfile>();
    private Inventories inventories;

    public DefaultGroupManager(Inventories inventories) {
        this.inventories = inventories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroupProfile getGroup(String groupName) {
        return this.groupNamesMap.get(groupName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroupProfile> getGroups() {
        List<WorldGroupProfile> groups = new ArrayList<WorldGroupProfile>();
        groups.addAll(this.getGroupNames().values());
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorldGroupProfile> getGroupsForWorld(String worldName) {
        List<WorldGroupProfile> worldGroups = this.getWorldGroups().get(worldName);
        if (worldGroups == null) {
            worldGroups = new ArrayList<WorldGroupProfile>();
            this.getWorldGroups().put(worldName, worldGroups);
        }
        return worldGroups;
    }

    /**
     * Retrieves all of the World Groups mapped to each world.
     *
     * @return Map of World -> World Groups
     */
    protected HashMap<String, List<WorldGroupProfile>> getWorldGroups() {
        return this.worldGroupsMap;
    }

    /**
     * Retrieves all of the World Groups mapped to their names.
     *
     * @return Map of Group Name -> World Group
     */
    protected HashMap<String, WorldGroupProfile> getGroupNames() {
        return this.groupNamesMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGroup(WorldGroupProfile worldGroup, boolean persist) {
        this.getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
        for (String worldName : worldGroup.getWorlds()) {
            List<WorldGroupProfile> worldGroupsForWorld = this.getWorldGroups().get(worldName);
            if (worldGroupsForWorld == null) {
                worldGroupsForWorld = new ArrayList<WorldGroupProfile>();
                this.getWorldGroups().put(worldName, worldGroupsForWorld);
            }
            worldGroupsForWorld.add(worldGroup);
        }
        this.inventories.getMVIConfig().updateWorldGroup(worldGroup);
        if (persist) {
            this.inventories.getMVIConfig().save();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGroup(WorldGroupProfile worldGroup) {
        this.getGroupNames().remove(worldGroup.getName().toLowerCase());
        for (String worldName : worldGroup.getWorlds()) {
            List<WorldGroupProfile> worldGroupsForWorld = this.getWorldGroups().get(worldName);
            if (worldGroupsForWorld != null) {
                worldGroupsForWorld.remove(worldGroup);
            }
        }
        this.inventories.getMVIConfig().removeWorldGroup(worldGroup);
        this.inventories.getMVIConfig().save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroupProfile newEmptyGroup(String name) {
        return new DefaultWorldGroupProfile(this.inventories, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroupProfile newGroupFromMap(String name, Map<String, Object> dataMap) throws DeserializationException {
        return new DefaultWorldGroupProfile(this.inventories, name, dataMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroups(List<WorldGroupProfile> worldGroups) {
        if (worldGroups == null) {
            Logging.info("No world groups have been configured!");
            Logging.info("This will cause all worlds configured for Multiverse to have separate player statistics/inventories.");
            return;
        }

        for (WorldGroupProfile worldGroup : worldGroups) {
            this.addGroup(worldGroup, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDefaultGroup() {
        Collection<MultiverseWorld> mvWorlds = this.inventories.getCore().getMVWorldManager().getMVWorlds();
        if (!mvWorlds.isEmpty()) {
            WorldGroupProfile worldGroup = new DefaultWorldGroupProfile(this.inventories, "default");
            worldGroup.setShares(Sharables.allOf());
            for (MultiverseWorld mvWorld : mvWorlds) {
                worldGroup.addWorld(mvWorld.getName());
            }
            this.addGroup(worldGroup, false);
            this.inventories.getMVIConfig().setFirstRun(false);
            this.inventories.getMVIConfig().save();
            Logging.info("Created a default group for you containing all of your MV Worlds!");
        } else {
            Logging.info("Could not configure a starter group due to no worlds being loaded into Multiverse-Core.");
            Logging.info("Will attempt again at next start up.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldGroupProfile getDefaultGroup() {
        return this.getGroupNames().get("default");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupingConflict> checkGroups() {
        List<GroupingConflict> conflicts = new ArrayList<GroupingConflict>();
        Map<WorldGroupProfile, WorldGroupProfile> previousConflicts = new HashMap<WorldGroupProfile, WorldGroupProfile>();
        for (WorldGroupProfile checkingGroup : this.getGroupNames().values()) {
            for (String worldName : checkingGroup.getWorlds()) {
                for (WorldGroupProfile worldGroup : this.getGroupsForWorld(worldName)) {
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
                        conflicts.add(new DefaultGroupingConflict(checkingGroup, worldGroup,
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
        String message = this.inventories.getMessager().getMessage(Message.CONFLICT_CHECKING);
        if (sender != null) {
            this.inventories.getMessager().sendMessage(sender, message);
        }
        Logging.info(message);
        List<GroupingConflict> conflicts = this.inventories.getGroupManager().checkGroups();
        for (GroupingConflict conflict : conflicts) {
            message = this.inventories.getMessager().getMessage(Message.CONFLICT_RESULTS,
                    conflict.getFirstGroup().getName(), conflict.getSecondGroup().getName(),
                    conflict.getConflictingShares().toString(), conflict.getWorldsString());
            if (sender != null) {
                this.inventories.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        }
        if (!conflicts.isEmpty()) {
            message = this.inventories.getMessager().getMessage(Message.CONFLICT_FOUND);
            if (sender != null) {
                this.inventories.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        } else {
            message = this.inventories.getMessager().getMessage(Message.CONFLICT_NOT_FOUND);
            if (sender != null) {
                this.inventories.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        }
    }
}

