package com.onarandombox.multiverseinventories.profile;

import java.util.List;

/**
 * Manager class for manipulating the profiles of this plugin that are contained in memory.
 */
public interface ProfileManager {

    /**
     * Adds a world profile to memory.
     *
     * @param worldProfile Profile to add.
     */
    void addWorldProfile(WorldProfile worldProfile);

    /**
     * @param worldName Name of world to get profile for.
     * @return the WorldProfile for specified world.
     */
    WorldProfile getWorldProfile(String worldName);

    /**
     * Replaces all of the profiles in memory.  Usually only when loading from config.
     *
     * @param worldProfiles List of profiles to manage.
     */
    void setWorldProfiles(List<WorldProfile> worldProfiles);
}

