package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;

import java.util.Collection;

/**
 * This manages the different types of profiles.  You may use it to register new profile types or to lookup existing
 * types.
 */
public interface ProfileTypeManager {

    /**
     * Registers a new profile type.
     *
     * @param name The name of the profile type.
     * @param shares The shares this profile type will use. TODO Make shares do something as they currently do nothing for profile types.
     */
    void registerProfileType(String name, Shares shares);

    /**
     * Looks up a profile type by name.
     * @param name The name to retrieve.
     * @return The profile type by the given name or null if non-existent.
     */
    ProfileType lookupType(String name);

    /**
     * @return An unmodifiable collection of all registered profile types.
     */
    Collection<ProfileType> getProfileTypes();
}
