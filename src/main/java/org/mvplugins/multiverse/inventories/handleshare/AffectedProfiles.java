package org.mvplugins.multiverse.inventories.handleshare;

import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.LinkedList;
import java.util.List;

public final class AffectedProfiles {

    private final List<PersistingProfile> writeProfiles = new LinkedList<>();
    private final List<PersistingProfile> readProfiles = new LinkedList<>();
    private final Shares sharesToRead = Sharables.noneOf();

    AffectedProfiles() {
    }

    /**
     * @param profileKey The profile key that will need data saved to.
     * @param shares  What from this group needs to be saved.
     */
    void addWriteProfile(ProfileKey profileKey, Shares shares) {
        writeProfiles.add(new PersistingProfile(shares, profileKey));
    }

    /**
     * @param profileKey The profile key that will need data loaded from.
     * @param shares  What from this group needs to be loaded.
     */
    void addReadProfile(ProfileKey profileKey, Shares shares) {
        readProfiles.add(new PersistingProfile(shares, profileKey));
        sharesToRead.addAll(shares);
    }

    public List<PersistingProfile> getWriteProfiles() {
        return writeProfiles;
    }

    public List<PersistingProfile> getReadProfiles() {
        return readProfiles;
    }

    boolean isShareToRead(Sharable<?> sharable) {
        return sharesToRead.contains(sharable);
    }
}
