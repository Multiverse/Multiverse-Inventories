package com.onarandombox.multiverseinventories.share;

import java.util.List;

/**
 * Interface for getting what is shared in a world profile.
 */
public interface Shares {

    /**
     * Merges what is shared with another share.  Only the {@link Sharing#NOT_SET} items should be merged.
     *
     * @param newShares The set of shares to merge into this set of shares.
     */
    void mergeShares(Shares newShares);

    /**
     * @return The {@link Sharing} result
     */
    Sharing getSharingInventory();

    /**
     * @param sharingInventory Whether to share inventory or not.
     */
    void setSharingInventory(Sharing sharingInventory);

    /**
     * @return The {@link Sharing} result
     */
    Sharing getSharingHealth();

    /**
     * @param sharingHealth Whether to share health or not.
     */
    void setSharingHealth(Sharing sharingHealth);

    /**
     * @return The {@link Sharing} result
     */
    Sharing getSharingHunger();

    /**
     * @param sharingHunger Whether to share hunger or not.
     */
    void setSharingHunger(Sharing sharingHunger);

    /**
     * @return The {@link Sharing} result
     */
    Sharing getSharingExp();

    /**
     * @param sharingExp Whether to share exp or not.
     */
    void setSharingExp(Sharing sharingExp);

    /**
     * @return The {@link Sharing} result
     */
    Sharing getSharingEffects();

    /**
     * @param sharingEffects Whether to share effects or not.
     */
    void setSharingEffects(Sharing sharingEffects);

    /**
     * @return These shares as a string list.
     */
    List<String> toStringList();
}
