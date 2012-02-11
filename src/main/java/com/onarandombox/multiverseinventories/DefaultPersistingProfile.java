package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.share.Shares;

/**
 * Simple implementation of PersistingProfile.
 */
final class DefaultPersistingProfile implements PersistingProfile {

    private Shares shares;
    private PlayerProfile profile;
    private String dataName;

    public DefaultPersistingProfile(String dataName, Shares shares, PlayerProfile profile) {
        this.shares = shares;
        this.profile = profile;
        this.dataName = dataName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataName() {
        return this.dataName;
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

