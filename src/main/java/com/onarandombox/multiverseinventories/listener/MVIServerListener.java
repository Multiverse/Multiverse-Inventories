package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

/**
 * Temporary server listener class to be soon replaced by a single Listener class using the new bukkit
 * event system.
 */
public class MVIServerListener implements Listener {

    private MultiverseInventories plugin;

    public MVIServerListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a plugin is enabled.
     *
     * @param event The plugin enable event.
     */
    @EventHandler
    public void pluginEnable(PluginEnableEvent event) {
        try {
            if (event.getPlugin() instanceof MultiInv) {
                this.plugin.getImportManager().hookMultiInv((MultiInv) event.getPlugin());
            } else if (event.getPlugin() instanceof WorldInventories) {
                this.plugin.getImportManager().hookWorldInventories((WorldInventories) event.getPlugin());
            }
        } catch (NoClassDefFoundError ignore) {
        }
    }

    /**
     * Called when a plugin is disabled.
     *
     * @param event The plugin disable event.
     */
    @EventHandler
    public void pluginDisable(PluginDisableEvent event) {
        try {
            if (event.getPlugin() instanceof MultiInv) {
                this.plugin.getImportManager().unHookMultiInv();
            } else if (event.getPlugin() instanceof WorldInventories) {
                this.plugin.getImportManager().unHookWorldInventories();
            }
        } catch (NoClassDefFoundError ignore) {
        }
    }
}

