package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.share.PersistingProfile;
import com.onarandombox.multiverseinventories.api.share.Shares;

/**
 * Simple implementation of PersistingProfile.
 */
final class DefaultPersistingProfile implements PersistingProfile {

    private Shares shares;
    private PlayerProfile profile;

    public DefaultPersistingProfile(Shares shares, PlayerProfile profile) {
        this.shares = shares;
        this.profile = profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getShares() {
        return this.shares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getProfile() {
        return this.profile;
    }
}

