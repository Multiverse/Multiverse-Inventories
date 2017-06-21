package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.profile.container.WorldProfileContainer;

/**
 * Manager class for manipulating the  of this plugin that are contained in memory.
 */
public interface WorldProfileContainerStore {

    /**
     * Adds a world player to memory.
     *
     * @param worldProfileContainer profile container to add.
     */
    void addWorldProfileContainer(WorldProfileContainer worldProfileContainer);

    /**
     * @param worldName Name of world to get player for.
     * @return the profile container for specified world.
     */
    WorldProfileContainer getWorldProfileContainer(String worldName);
}

