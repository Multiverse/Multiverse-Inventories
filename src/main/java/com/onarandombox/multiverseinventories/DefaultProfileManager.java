package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ProfileManager;

class DefaultProfileManager implements ProfileManager {

    private Inventories inventories;

    DefaultProfileManager(Inventories inventories) {
        this.inventories = inventories;
    }
}
