package com.onarandombox.multiverseinventories.api.profile;

/**
 * This class represents the configuration for a type of profile used with multiple profiles per world/group.
 */
public interface ProfileType {

    /**
     * @return The name of the profile.  The default profile type will return a blank string.
     */
    String getName();

    /**
     * @return The {@link com.onarandombox.multiverseinventories.api.share.Sharable}s this Profile will handle.
     * When not set up in the config, this should return {@link com.onarandombox.multiverseinventories.api.share.Sharables#allOf()}
     */
    //Shares getShares();
}
