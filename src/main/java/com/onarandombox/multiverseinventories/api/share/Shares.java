package com.onarandombox.multiverseinventories.api.share;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for getting what is shared in a world player.
 */
public interface Shares extends Cloneable, Iterable<Sharable>, Collection<Sharable>, Set<Sharable> {

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
     * @param shares Shares to compare with.
     * @return True if it is sharing the same sharables.
     */
    boolean isSharing(Shares shares);

    /**
     * Checks to see if any of the sharables passed in are shared by this Shares.
     *
     * @param shares Shares to check for.
     * @return A Set containing all of the Sharables both sets contain.
     */
    Shares compare(Shares shares);

    /**
     * @param sharable The Sharable you wish to set sharing for.
     * @param sharing  Whether to share or not.
     */
    void setSharing(Sharable sharable, boolean sharing);

    /**
     * @param sharables a Set of Sharables you wish to set sharing for.
     * @param sharing   Whether to share or not.
     */
    void setSharing(Shares sharables, boolean sharing);

    /**
     * @return These shares as a string list.
     */
    List<String> toStringList();
}

