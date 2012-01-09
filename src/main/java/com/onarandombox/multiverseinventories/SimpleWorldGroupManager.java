package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.world.WorldGroup;

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
        return this.groupNames.get(groupName);
    }

    @Override
    public List<WorldGroup> getWorldGroups(String worldName) {
        return this.worldGroups.get(worldName);
    }

    public void setWorldGroups(List<WorldGroup> worldGroups) {

    }
}
