package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for custom events from Multiverse-Core.
 */
public class MVICoreListener implements Listener {

    private MultiverseInventories plugin;

    public MVICoreListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void versionRequest(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }

    /**
     * {@inheritDoc}
     */
    @EventHandler
    public void configReload(MVConfigReloadEvent event) {
        this.plugin.reloadConfig();
        event.addConfig("Multiverse-Inventories - config.yml");
    }
}
