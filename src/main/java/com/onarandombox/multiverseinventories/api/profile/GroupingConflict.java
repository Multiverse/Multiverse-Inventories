package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;

import java.util.List;

/**
 * A data class to hold information about any conflicts between world groups.
 */
public interface GroupingConflict {

    /**
     * @return The first group in the conflict.
     */
    WorldGroupProfile getFirstGroup();

    /**
     * @return The second group in the conflict.
     */
    WorldGroupProfile getSecondGroup();

    /**
     * @return The shares that are causing a conflict.
     */
    Shares getConflictingShares();

    /**
     * @return The worlds the two groups share.
     */
    List<String> getConflictingWorlds();

    /**
     * @return The worlds the two groups share as a single string.
     */
    String getWorldsString();
}

