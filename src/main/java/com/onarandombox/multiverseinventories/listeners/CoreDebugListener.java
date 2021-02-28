package com.onarandombox.multiverseinventories.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.event.MVDebugModeEvent;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CoreDebugListener implements Listener {

    public CoreDebugListener(MultiverseInventories plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDebugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
