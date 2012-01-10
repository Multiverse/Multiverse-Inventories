package com.onarandombox.multiverseinventories.profile;

import java.util.HashMap;
import java.util.List;

/**
 * Simple implementation of ProfileManager.
 */
public class SimpleProfileManager implements ProfileManager {

    private HashMap<String, WorldProfile> worldProfiles = new HashMap<String, WorldProfile>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorldProfile(WorldProfile worldProfile) {
        this.worldProfiles.put(worldProfile.getWorld().toLowerCase(), worldProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldProfile getWorldProfile(String worldName) {
        WorldProfile worldProfile = this.worldProfiles.get(worldName.toLowerCase());
        if (worldProfile == null) {
            worldProfile = new SimpleWorldProfile(worldName);
            this.addWorldProfile(worldProfile);
        }
        return worldProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorldProfiles(List<WorldProfile> worldProfiles) {
        for (WorldProfile worldProfile : worldProfiles) {
            this.addWorldProfile(worldProfile);
        }
    }
}
