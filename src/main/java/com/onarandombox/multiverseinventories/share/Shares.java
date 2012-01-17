package com.onarandombox.multiverseinventories.share;

import java.util.EnumSet;
import java.util.List;

/**
 * Interface for getting what is shared in a world profile.
 */
public interface Shares {

    /**
     * Merges what is shared with another share.  Only the false items should be merged.
     *
     * @param newShares The set of shares to merge into this set of shares.
     */
    void mergeShares(Shares newShares);

    /**
     * @return An EnumSet of all Sharables.
     */
    EnumSet<Sharable> getSharables();

    /**
     * @param sharable The Sharable you want to check for.
     * @return True if it is sharing the sharable.
     */
    boolean isSharing(Sharable sharable);

    /**
     * @param sharables Set of sharables to compare with.
     * @return True if it is sharing the same sharables.
     */
    boolean isSharing(EnumSet<Sharable> sharables);

    /**
     * Checks to see if any of the sharables passed in are shared by this Shares.
     *
     * @param sharables Sharables to check for.
     * @return A Set containing all of the Sharables both sets contain.
     */
    EnumSet<Sharable> isSharingAnyOf(EnumSet<Sharable> sharables);

    /**
     * @param sharable The Sharable you wish to set sharing for.
     * @param sharing  Whether to share or not.
     */
    void setSharing(Sharable sharable, boolean sharing);

    /**
     * @return These shares as a string list.
     */
    List<String> toStringList();
}
