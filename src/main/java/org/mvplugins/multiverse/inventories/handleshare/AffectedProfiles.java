package org.mvplugins.multiverse.inventories.handleshare;

import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.LinkedList;
import java.util.List;

import static org.mvplugins.multiverse.inventories.share.Sharables.allOf;

public final class AffectedProfiles {

    private PersistingProfile alwaysWriteProfile;
    private final List<PersistingProfile> writeProfiles = new LinkedList<>();
    private final List<PersistingProfile> readProfiles = new LinkedList<>();

    AffectedProfiles() {
    }

    void setAlwaysWriteProfile(PlayerProfile profile) {
        alwaysWriteProfile = new PersistingProfile(allOf(), profile);
    }

    /**
     * @param profile The player profile that will need data saved to.
     * @param shares  What from this group needs to be saved.
     */
    void addWriteProfile(PlayerProfile profile, Shares shares) {
        writeProfiles.add(new PersistingProfile(shares, profile));
    }

    /**
     * @param profile The player profile that will need data loaded from.
     * @param shares  What from this group needs to be loaded.
     */
    void addReadProfile(PlayerProfile profile, Shares shares) {
        readProfiles.add(new PersistingProfile(shares, profile));
    }

    public PersistingProfile getAlwaysWriteProfile() {
        return alwaysWriteProfile;
    }

    public List<PersistingProfile> getWriteProfiles() {
        return writeProfiles;
    }

    public List<PersistingProfile> getReadProfiles() {
        return readProfiles;
    }
}
