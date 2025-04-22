package org.mvplugins.multiverse.inventories.handleshare;

import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.share.Shares;

/**
 * Simple class for groups that are going to be saved/loaded. This is used specifically for when a user's world
 * change is being handled.
 */
public final class PersistingProfile {
    private final Shares shares;
    private final ProfileKey profileKey;

    public PersistingProfile(Shares shares, PlayerProfile profile) {
        this(shares, ProfileKey.fromPlayerProfile(profile));
    }

    public PersistingProfile(Shares shares, ProfileKey profile) {
        this.shares = shares;
        this.profileKey = profile;
    }

    /**
     * Gets the shares that will be saved/loaded for the profile.
     *
     * @return The shares that will be saved/loaded for the profile. This is the set of all Sharables that will be acted
     * upon when passed through the ShareHandler class, or any of its subclasses.
     */
    public Shares getShares() {
        return this.shares;
    }

    /**
     * Gets the player profile for the world/group that will be saved/loaded for.
     *
     * @return The player profile for the world/group that will be saved/loaded for.
     */
    public ProfileKey getProfileKey() {
        return this.profileKey;
    }
}

