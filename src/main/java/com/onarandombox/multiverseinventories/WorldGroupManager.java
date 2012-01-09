package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.world.WorldGroup;

import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public interface WorldGroupManager {

    public WorldGroup getGroup(String groupName);

    public List<WorldGroup> getWorldGroups(String worldName);

    public void setWorldGroups(List<WorldGroup> worldGroups);

    public void addWorldGroup(WorldGroup worldGroup);
}
