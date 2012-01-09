package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.util.MILog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public class SimpleWorldGroupManager implements WorldGroupManager {

    private HashMap<String, List<WorldGroup>> worldGroups = new HashMap<String, List<WorldGroup>>();
    private HashMap<String, WorldGroup> groupNames = new HashMap<String, WorldGroup>();

    @Override
    public WorldGroup getGroup(String groupName) {
        return this.groupNames.get(groupName.toLowerCase());
    }

    @Override
    public List<WorldGroup> getWorldGroups(String worldName) {
        List<WorldGroup> worldGroups = this.getWorldGroups().get(worldName);
        if (worldGroups == null) {
            worldGroups = new ArrayList<WorldGroup>();
            this.getWorldGroups().put(worldName, worldGroups);
        }
        return worldGroups;
    }

    protected HashMap<String, List<WorldGroup>> getWorldGroups() {
        return this.worldGroups;
    }

    protected HashMap<String, WorldGroup> getGroupNames() {
        return this.groupNames;
    }

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

    public void setWorldGroups(List<WorldGroup> worldGroups) {
        if (worldGroups == null) {
            MILog.info("No world groups have been configured!");
            MILog.info("This will cause all worlds configured for Multiverse to have separate player statistics/inventories.");
            return;
        }

        for (WorldGroup worldGroup : worldGroups) {
            this.addWorldGroup(worldGroup);
        }
    }
}
