package org.mvplugins.multiverse.inventories;

import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.share.PersistingProfile;
import org.mvplugins.multiverse.inventories.share.Shares;

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

