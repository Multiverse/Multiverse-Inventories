package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.profile.container.WorldProfile;

/**
 * Manager class for manipulating the profiles of this plugin that are contained in memory.
 */
public interface WorldProfileManager {

    /**
     * Adds a world player to memory.
     *
     * @param worldProfile Profile to add.
     */
    void addWorldProfile(WorldProfile worldProfile);

    /**
     * @param worldName Name of world to get player for.
     * @return the WorldProfile for specified world.
     */
    WorldProfile getWorldProfile(String worldName);
}

