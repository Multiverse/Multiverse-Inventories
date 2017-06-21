package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.container.WorldProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfileManager;

import java.util.Map;
import java.util.WeakHashMap;

final class WeakWorldProfileManager implements WorldProfileManager {

    private Map<String, WorldProfile> worldProfiles = new WeakHashMap<String, WorldProfile>();
    private MultiverseInventories inventories;

    WeakWorldProfileManager(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    private MultiverseInventories getInventories() {
        return this.inventories;
    }

    @Override
    public void addWorldProfile(WorldProfile worldProfile) {
        this.worldProfiles.put(worldProfile.getWorld().toLowerCase(), worldProfile);
    }

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

