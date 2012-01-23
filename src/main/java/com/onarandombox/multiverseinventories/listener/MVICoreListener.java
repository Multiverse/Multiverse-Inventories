package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.listeners.MultiverseCoreListener;
import com.onarandombox.multiverseinventories.MultiverseInventories;

public class MVICoreListener extends MultiverseCoreListener {

    private MultiverseInventories plugin;

    public MVICoreListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onVersionRequest(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMVConfigReload(MVConfigReloadEvent event) {
        this.plugin.reloadConfig();
        event.addConfig("Multiverse-Inventories - config.yml");
    }
}
