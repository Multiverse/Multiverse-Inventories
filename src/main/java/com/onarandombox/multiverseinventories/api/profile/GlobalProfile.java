package com.onarandombox.multiverseinventories.api.profile;

import java.util.Map;

public interface GlobalProfile {

    String getName();

    String getWorld();

    void setWorld(String world);

    ProfileType getProfileType();

    void setProfileType(ProfileType profileType);

    Map<String, Object> serialize();
}
