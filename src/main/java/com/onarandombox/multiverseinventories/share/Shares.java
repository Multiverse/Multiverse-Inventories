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
     * @return True if sharing.
     */
    boolean isSharingInventory();

    /**
     * @param sharingInventory Whether to share inventory or not.
     */
    void setSharingInventory(boolean sharingInventory);

    /**
     * @return True if sharing.
     */
    boolean isSharingHealth();

    /**
     * @param sharingHealth Whether to share health or not.
     */
    void setSharingHealth(boolean sharingHealth);

    /**
     * @return True if sharing.
     */
    boolean isSharingHunger();

    /**
     * @param sharingHunger Whether to share hunger or not.
     */
    void setSharingHunger(boolean sharingHunger);

    /**
     * @return True if sharing.
     */
    boolean isSharingExp();

    /**
     * @param sharingExp Whether to share exp or not.
     */
    void setSharingExp(boolean sharingExp);

    /**
     * @return True if sharing.
     */
    boolean isSharingEffects();

    /**
     * @param sharingEffects Whether to share effects or not.
     */
    void setSharingEffects(boolean sharingEffects);

    /**
     * @return These shares as a string list.
     */
    List<String> toStringList();
}
