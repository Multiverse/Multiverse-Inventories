package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.ProfileManager;
import com.onarandombox.multiverseinventories.profile.SimpleWorldProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfile;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple implementation of ProfileManager.
 */
class WeakProfileManager implements ProfileManager {

    private Map<String, WorldProfile> worldProfiles = new WeakHashMap<String, WorldProfile>();
    private MultiverseInventories plugin;

    public WeakProfileManager(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    private MultiverseInventories getPlugin() {
        return this.plugin;
    }

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
            worldProfile = new SimpleWorldProfile(this.getPlugin(), worldName);
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

