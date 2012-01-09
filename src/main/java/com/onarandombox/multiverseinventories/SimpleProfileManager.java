package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.world.SimpleWorldProfile;
import com.onarandombox.multiverseinventories.world.WorldProfile;

import java.util.HashMap;
import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public class SimpleProfileManager implements ProfileManager {

    private HashMap<String, WorldProfile> worldProfiles = new HashMap<String, WorldProfile>();

    public void addWorldProfile(WorldProfile worldProfile) {
        this.worldProfiles.put(worldProfile.getWorld(), worldProfile);
    }

    public WorldProfile getWorldProfile(String worldName) {
        WorldProfile worldProfile = this.worldProfiles.get(worldName);
        if (worldProfile == null) {
            worldProfile = new SimpleWorldProfile(worldName);
            this.worldProfiles.put(worldName, worldProfile);
        }
        return worldProfile;
    }

    public void setWorldProfiles(List<WorldProfile> worldProfiles) {
        for (WorldProfile worldProfile : worldProfiles) {
            this.addWorldProfile(worldProfile);
        }
    }
}
