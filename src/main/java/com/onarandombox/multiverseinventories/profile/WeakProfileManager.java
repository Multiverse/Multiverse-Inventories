package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.MultiverseInventories;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple implementation of ProfileManager.
 */
public class WeakProfileManager implements ProfileManager {

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
            worldProfile = new SimpleWorldProfile(this.getPlugin().getData(), worldName);
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
