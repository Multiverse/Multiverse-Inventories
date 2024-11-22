package org.mvplugins.multiverse.inventories.share;

import org.mvplugins.multiverse.inventories.profile.PlayerProfile;

/**
 * Simple interface for groups that are going to be saved/loaded. This is used specifically for when a user's world
 * change is being handled.
 */
public interface PersistingProfile {

    /**
     * @return The shares that will be saved/loaded for the profile. This is the set of all Sharables that will be acted
     *         upon when passed through the ShareHandler class, or any of its subclasses.
     */
    Shares getShares();

    /**
     * @return The player profile for the world/group that will be saved/loaded for.
     */
    PlayerProfile getProfile();
}

