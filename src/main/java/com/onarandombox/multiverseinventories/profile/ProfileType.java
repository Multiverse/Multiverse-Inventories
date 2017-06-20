package com.onarandombox.multiverseinventories.profile;

/**
 * Used to differentiate between profiles in the same world or world group, primarily for game modes.
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
