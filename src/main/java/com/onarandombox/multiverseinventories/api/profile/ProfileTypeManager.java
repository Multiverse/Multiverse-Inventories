package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;

import java.util.Collection;

public interface ProfileTypeManager {

    void registerProfileType(String name, Shares shares);

    ProfileType lookupType(String name);

    Collection<ProfileType> getProfileTypes();
}
