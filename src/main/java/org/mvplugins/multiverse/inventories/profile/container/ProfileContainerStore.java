package org.mvplugins.multiverse.inventories.profile.container;

import org.mvplugins.multiverse.inventories.MultiverseInventories;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A utility for storing and retrieving profile containers.
 */
public final class ProfileContainerStore {

    private final Map<String, ProfileContainer> containers = new WeakHashMap<>();

    private final MultiverseInventories inventories;
    private final ContainerType containerType;

     ProfileContainerStore(MultiverseInventories inventories, ContainerType containerType) {
        this.inventories = inventories;
        this.containerType = containerType;
    }

    /**
     * Adds a profile container to the store.
     *
     * @param container profile container to add.
     */
    public void addContainer(ProfileContainer container) {
        this.containers.put(container.getContainerName().toLowerCase(), container);
    }

    /**
     * Returns the profile container for the given name.
     *
     * @param containerName Name of the profile container to retrieve.
     * @return the profile container for given name.
     */
    public ProfileContainer getContainer(String containerName) {
        ProfileContainer container = this.containers.get(containerName.toLowerCase());
        if (container == null) {
            container = new ProfileContainer(inventories, containerName, containerType);
            addContainer(container);
        }
        return container;
    }
}

