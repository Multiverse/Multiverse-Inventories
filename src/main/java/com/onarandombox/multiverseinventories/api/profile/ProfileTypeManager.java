package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;

public interface ProfileTypeManager {

    public void registerProfileType(String name, Shares shares);

    public ProfileType lookupType(String name);
}
