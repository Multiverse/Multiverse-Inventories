package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.api.WorldGroup;
import com.onarandombox.multiverseinventories.share.Shares;

import java.util.List;

/**
 * A data class to hold information about any conflicts between world groups.
 */
public interface GroupingConflict {

    /**
     * @return The first group in the conflict.
     */
    WorldGroup getFirstGroup();

    /**
     * @return The second group in the conflict.
     */
    WorldGroup getSecondGroup();

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

