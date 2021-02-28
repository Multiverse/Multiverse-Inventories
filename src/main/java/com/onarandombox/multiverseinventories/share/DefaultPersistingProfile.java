package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.share.PersistingProfile;
import com.onarandombox.multiverseinventories.share.Shares;

/**
 * Simple implementation of PersistingProfile.
 */
public final class DefaultPersistingProfile implements PersistingProfile {

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

