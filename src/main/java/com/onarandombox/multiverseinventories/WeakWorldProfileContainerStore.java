package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.container.WorldProfileContainer;
import com.onarandombox.multiverseinventories.profile.WorldProfileContainerStore;

import java.util.Map;
import java.util.WeakHashMap;

final class WeakWorldProfileContainerStore implements WorldProfileContainerStore {

    private Map<String, WorldProfileContainer> worldProfiles = new WeakHashMap<String, WorldProfileContainer>();
    private MultiverseInventories inventories;

    WeakWorldProfileContainerStore(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    private MultiverseInventories getInventories() {
        return this.inventories;
    }

    @Override
    public void addWorldProfileContainer(WorldProfileContainer worldProfileContainer) {
        this.worldProfiles.put(worldProfileContainer.getWorld().toLowerCase(), worldProfileContainer);
    }

    @Override
    public WorldProfileContainer getWorldProfileContainer(String worldName) {
        WorldProfileContainer container = this.worldProfiles.get(worldName.toLowerCase());
        if (container == null) {
            container = new DefaultWorldProfileContainer(this.getInventories(), worldName);
            this.addWorldProfileContainer(container);
        }
        return container;
    }
}

