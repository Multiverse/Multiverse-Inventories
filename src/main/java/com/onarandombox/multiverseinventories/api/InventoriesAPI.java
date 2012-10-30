package com.onarandombox.multiverseinventories.api;

import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;

/**
 * A class for simple and direct access to the most common functions and features of Multiverse-Inventories.
 *
 * @version 2.5-beta
 * @since 2012-10-30
 */
public interface InventoriesAPI {

    /**
     * Retrieves the world group associated with the given name.
     *
     * These groups represent the groups that define a set of worlds and what they share.
     *
     * @param groupName Name of world group to retrieve. Casing is ignored.
     * @return The world group by the name given or null if one doesn't exist by that name.
     * @see GroupManager#getGroup(String)
     */
    WorldGroupProfile getGroup(String groupName);
}
