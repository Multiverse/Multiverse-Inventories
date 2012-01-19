package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.share.Shares;

/**
 * Simple implementation of PersistingProfile.
 */
public class SimplePersistingProfile implements PersistingProfile {

    private Shares shares;
    private PlayerProfile profile;
    private String dataName;

    public SimplePersistingProfile(String dataName, Shares shares, PlayerProfile profile) {
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

