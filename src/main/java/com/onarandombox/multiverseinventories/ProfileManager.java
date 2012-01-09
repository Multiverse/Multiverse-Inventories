package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.world.WorldProfile;

import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public interface ProfileManager {

    public void addWorldProfile(WorldProfile worldProfile);

    public WorldProfile getWorldProfile(String worldName);

    public void setWorldProfiles(List<WorldProfile> worldProfiles);
}
