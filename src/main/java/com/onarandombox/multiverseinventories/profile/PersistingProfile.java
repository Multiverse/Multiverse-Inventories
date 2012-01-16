package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.share.Shares;

/**
 * Simple interface for groups that are going to be saved/loaded.
 */
public interface PersistingProfile {

    /**
     * @return The name used in the plugin's data.
     */
    String getDataName();

    /**
     * @return The shares that will be saved/loaded for.
     */
    Shares getShares();

    /**
     * @return The group that will be saved/loaded for.
     */
    PlayerProfile getProfile();
}
