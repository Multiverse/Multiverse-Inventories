package com.onarandombox.multiverseinventories.profile.container;

import com.onarandombox.multiverseinventories.MultiverseInventories;

import java.util.Map;
import java.util.WeakHashMap;

public final class WeakProfileContainerStore implements ProfileContainerStore {

    private final Map<String, ProfileContainer> containers = new WeakHashMap<>();

    private final MultiverseInventories inventories;
    private final ContainerType containerType;

    public WeakProfileContainerStore(MultiverseInventories inventories, ContainerType containerType) {
        this.inventories = inventories;
        this.containerType = containerType;
    }

    private MultiverseInventories getInventories() {
        return this.inventories;
    }

    @Override
    public void addContainer(ProfileContainer container) {
        this.containers.put(container.getContainerName().toLowerCase(), container);
    }

    @Override
    public ProfileContainer getContainer(String containerName) {
        ProfileContainer container = this.containers.get(containerName.toLowerCase());
        if (container == null) {
            container = new WeakProfileContainer(this.getInventories(), containerName, containerType);
            addContainer(container);
        }
        return container;
    }
}

