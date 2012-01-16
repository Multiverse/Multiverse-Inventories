package com.onarandombox.multiverseinventories.share;

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
     * @param sharable The Sharable you want to check for.
     * @return True if it is sharing the sharable.
     */
    boolean isSharing(Sharable sharable);

    /**
     * @param sharable The Sharable you wish to set sharing for.
     * @param sharing Whether to share or not.
     */
    void setSharing(Sharable sharable, boolean sharing);

    /**
     * @return These shares as a string list.
     */
    List<String> toStringList();
}
