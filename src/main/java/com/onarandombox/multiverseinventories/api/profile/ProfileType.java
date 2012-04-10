package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;

/**
 * This class represents the configuration for a type of profile used with multiple profiles per world/group.
 */
public interface ProfileType {

    /**
     * @return The name of the profile.
     */
    String getName();

    /**
     * @return The title for the section within a player profile file.
     */
    String getProfilePath();

    /**
     * @return The {@link com.onarandombox.multiverseinventories.api.share.Sharable}s this Profile will handle.
     * When not set up in the config, this should return {@link com.onarandombox.multiverseinventories.api.share.Sharables#allOf()}
     */
    Shares getShares();
}
