package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfileManager;

import java.util.Map;
import java.util.WeakHashMap;

class DefaultGlobalProfileManager implements GlobalProfileManager {

    private Inventories inventories;

    private final Map<String, GlobalProfile> globalProfileMap = new WeakHashMap<String, GlobalProfile>();

    DefaultGlobalProfileManager(Inventories inventories) {
        this.inventories = inventories;
    }

    @Override
    public GlobalProfile getGlobalProfile(String playerName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
