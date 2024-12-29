package org.mvplugins.multiverse.inventories.profile.container;

/**
 * A utility for storing and retrieving profile containers.
 */
public interface ProfileContainerStore {

    /**
     * Adds a profile container to the store.
     *
     * @param container profile container to add.
     */
    void addContainer(ProfileContainer container);

    /**
     * Returns the profile container for the given name.
     *
     * @param containerName Name of the profile container to retrieve.
     * @return the profile container for given name.
     */
    ProfileContainer getContainer(String containerName);
}

