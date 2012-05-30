package com.onarandombox.multiverseinventories.api.profile;

import java.util.Map;

public interface GlobalProfile {

    String getName();

    String getWorld();

    void setWorld(String world);

    Map<String, Object> serialize();
}
