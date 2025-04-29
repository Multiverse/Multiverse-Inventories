package org.mvplugins.multiverse.inventories.profile.container;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A utility for storing and retrieving profile containers.
 */
public final class ProfileContainerStore {

    private final Map<String, ProfileContainer> containers = new WeakHashMap<>();

    private final MultiverseInventories inventories;
    private final ContainerType containerType;
    private final ProfileDataSource profileDataSource;

     ProfileContainerStore(MultiverseInventories inventories, ContainerType containerType) {
        this.inventories = inventories;
        this.containerType = containerType;
        this.profileDataSource = inventories.getServiceLocator().getService(ProfileDataSource.class);
     }

    public List<String> listContainerDataNames() {
        return profileDataSource.listContainerDataNames(containerType);
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

    /**
     * Adds a profile container to the store.
     *
     * @param container profile container to add.
     */
    private void addContainer(ProfileContainer container) {
        this.containers.put(container.getContainerName().toLowerCase(), container);
    }
}

