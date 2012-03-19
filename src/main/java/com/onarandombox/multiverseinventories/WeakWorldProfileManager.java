package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfileManager;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple implementation of WorldProfileManager.
 */
final class WeakWorldProfileManager implements WorldProfileManager {

    private Map<String, WorldProfile> worldProfiles = new WeakHashMap<String, WorldProfile>();
    private Inventories inventories;

    public WeakWorldProfileManager(Inventories inventories) {
        this.inventories = inventories;
    }

    private Inventories getInventories() {
        return this.inventories;
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
            worldProfile = new DefaultWorldProfile(this.getInventories(), worldName);
            this.addWorldProfile(worldProfile);
        }
        return worldProfile;
    }
}

