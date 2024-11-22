package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CoreDebugListener implements Listener {

    CoreDebugListener(MultiverseInventories plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDebugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
