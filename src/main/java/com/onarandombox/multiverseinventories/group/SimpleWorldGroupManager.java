package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.util.MVILog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of WorldGroupManager.
 */
public class SimpleWorldGroupManager implements WorldGroupManager {

    private HashMap<String, List<WorldGroup>> worldGroupsMap = new HashMap<String, List<WorldGroup>>();
    private HashMap<String, WorldGroup> groupNamesMap = new HashMap<String, WorldGroup>();

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
    public List<WorldGroup> getWorldGroups(String worldName) {
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
    public void addWorldGroup(WorldGroup worldGroup) {
        this.getGroupNames().put(worldGroup.getName().toLowerCase(), worldGroup);
        for (String worldName : worldGroup.getWorlds()) {
            List<WorldGroup> worldGroupsForWorld = this.getWorldGroups().get(worldName);
            if (worldGroupsForWorld == null) {
                worldGroupsForWorld = new ArrayList<WorldGroup>();
                this.getWorldGroups().put(worldName, worldGroupsForWorld);
            }
            worldGroupsForWorld.add(worldGroup);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorldGroups(List<WorldGroup> worldGroups) {
        if (worldGroups == null) {
            MVILog.info("No world groups have been configured!");
            MVILog.info("This will cause all worlds configured for Multiverse to have separate player statistics/inventories.");
            return;
        }

        for (WorldGroup worldGroup : worldGroups) {
            this.addWorldGroup(worldGroup);
        }
    }
}
