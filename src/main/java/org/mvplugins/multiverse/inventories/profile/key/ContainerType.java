package org.mvplugins.multiverse.inventories.profile.key;

import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;

/**
 * Used to describe whether a {@link ProfileContainer} represents a single world or a group of worlds.
 */
public enum ContainerType {

    /**
     * Indicates World type profiles.
     */
    WORLD,
    /**
     * Indicates Group type profiles.
     */
    GROUP;
}

