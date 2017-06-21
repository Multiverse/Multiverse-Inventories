package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.GroupProfileManager;
import com.onarandombox.multiverseinventories.profile.GroupingConflict;
import com.onarandombox.multiverseinventories.profile.container.GroupProfileContainer;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of GroupManager with no persistence of groups.
 */
abstract class AbstractGroupProfileManager implements GroupProfileManager {

    static final String DEFAULT_GROUP_NAME = "default";
    protected final Map<String, GroupProfileContainer> groupNamesMap = new LinkedHashMap<>();
    protected final MultiverseInventories plugin;

    public AbstractGroupProfileManager(final MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupProfileContainer getGroup(String groupName) {
        return this.groupNamesMap.get(groupName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupProfileContainer> getGroups() {
        return Collections.unmodifiableList(new ArrayList<GroupProfileContainer>(this.getGroupNames().values()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupProfileContainer> getGroupsForWorld(String worldName) {
        worldName = worldName.toLowerCase();
        List<GroupProfileContainer> worldGroups = new ArrayList<GroupProfileContainer>();
        for (GroupProfileContainer worldGroup : this.getGroupNames().values()) {
            if (worldGroup.containsWorld(worldName)) {
                worldGroups.add(worldGroup);
            }
        }
        // Only use the default group for worlds managed by MV-Core
        if (worldGroups.isEmpty() && this.plugin.getMVIConfig().isDefaultingUngroupedWorlds() && 
                this.plugin.getCore().getMVWorldManager().isMVWorld(worldName)) {
            Logging.finer("Returning default group for world: " + worldName);
            worldGroups.add(getDefaultGroup());
        }
        return worldGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasGroup(String worldName) {
        return !getGroupsForWorld(worldName).isEmpty();
    }

    /**
     * Retrieves all of the World Groups mapped to their names.
     *
     * @return Map of Group Name -> World Group
     */
    protected Map<String, GroupProfileContainer> getGroupNames() {
        return this.groupNamesMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void addGroup(final GroupProfileContainer worldGroup, final boolean persist) {
        updateGroup(worldGroup);
    }

    @Override
    public void updateGroup(final GroupProfileContainer worldGroup) {
        getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
    }

    protected void persistGroup(final GroupProfileContainer worldGroup) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeGroup(final GroupProfileContainer worldGroup) {
        return getGroupNames().remove(worldGroup.getName().toLowerCase()) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupProfileContainer newEmptyGroup(String name) {
        if (getGroup(name) != null) {
            return null;
        }
        return new DefaultGroupProfileContainer(this.plugin, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void setGroups(List<GroupProfileContainer> worldGroups) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDefaultGroup() {
        if (this.getGroup(DEFAULT_GROUP_NAME) != null) {
            return;
        }
        World defaultWorld = Bukkit.getWorlds().get(0);
        World defaultNether = Bukkit.getWorld(defaultWorld.getName() + "_nether");
        World defaultEnd = Bukkit.getWorld(defaultWorld.getName() + "_the_end");
        GroupProfileContainer worldGroup = new DefaultGroupProfileContainer(this.plugin, DEFAULT_GROUP_NAME);
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
        this.updateGroup(worldGroup);
        this.plugin.getMVIConfig().setFirstRun(false);
        this.plugin.getMVIConfig().save();
        Logging.info("Created a default group for you containing all of your default worlds: " + worlds.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupProfileContainer getDefaultGroup() {
        GroupProfileContainer group = this.getGroupNames().get(DEFAULT_GROUP_NAME);
        if (group == null) {
            group = newEmptyGroup(DEFAULT_GROUP_NAME);
            group.getShares().setSharing(Sharables.allOf(), true);
            this.updateGroup(group);
        }
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupingConflict> checkGroups() {
        List<GroupingConflict> conflicts = new ArrayList<GroupingConflict>();
        Map<GroupProfileContainer, GroupProfileContainer> previousConflicts = new HashMap<GroupProfileContainer, GroupProfileContainer>();
        for (GroupProfileContainer checkingGroup : this.getGroupNames().values()) {
            for (String worldName : checkingGroup.getWorlds()) {
                for (GroupProfileContainer worldGroup : this.getGroupsForWorld(worldName)) {
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
        String message = this.plugin.getMessager().getMessage(Message.CONFLICT_CHECKING);
        if (sender != null) {
            this.plugin.getMessager().sendMessage(sender, message);
        }
        Logging.fine(message);
        List<GroupingConflict> conflicts = this.plugin.getGroupManager().checkGroups();
        for (GroupingConflict conflict : conflicts) {
            message = this.plugin.getMessager().getMessage(Message.CONFLICT_RESULTS,
                    conflict.getFirstGroup().getName(), conflict.getSecondGroup().getName(),
                    conflict.getConflictingShares().toString(), conflict.getWorldsString());
            if (sender != null) {
                this.plugin.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        }
        if (!conflicts.isEmpty()) {
            message = this.plugin.getMessager().getMessage(Message.CONFLICT_FOUND);
            if (sender != null) {
                this.plugin.getMessager().sendMessage(sender, message);
            }
            Logging.info(message);
        } else {
            message = this.plugin.getMessager().getMessage(Message.CONFLICT_NOT_FOUND);
            if (sender != null) {
                this.plugin.getMessager().sendMessage(sender, message);
            }
            Logging.fine(message);
        }
    }
}

