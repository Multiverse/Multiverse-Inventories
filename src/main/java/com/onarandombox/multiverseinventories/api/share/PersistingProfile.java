package com.onarandombox.multiverseinventories.api.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;

/**
 * Simple interface for groups that are going to be saved/loaded.  This is used specifically for when a user's world
 * change is being handled.
 */
public interface PersistingProfile {

    /**
     * @return The shares that will be saved/loaded for.  This set are all the sharable things
     *         that will be acted upon when passed through
     *         {@link com.onarandombox.multiverseinventories.ShareHandler}
     */
    Shares getShares();

    /**
     * @return The player profile for the world/group that will be saved/loaded for.
     */
    PlayerProfile getProfile();
}

