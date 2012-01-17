package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.share.SimpleShareHandler;
import com.onarandombox.multiverseinventories.util.MVIDebug;
import com.onarandombox.multiverseinventories.util.MVILog;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

/**
 * PlayerListener for MultiverseInventories.
 */
public class MVIListener implements Listener {

    private MultiverseInventories plugin;

    public MVIListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player changes worlds.
     *
     * @param event The world change event.
     */
    @EventHandler(event = PlayerChangedWorldEvent.class, priority = org.bukkit.event.EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            MVIDebug.info("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Do nothing if dealing with non-managed worlds
        if (this.plugin.getCore().getMVWorldManager().getMVWorld(toWorld) == null
                || this.plugin.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            MVILog.debug("The from or to world is not managed by Multiverse!");
            return;
        }

        new SimpleShareHandler(this.plugin, player, fromWorld, toWorld).handleSharing();
    }

    /**
     * Called when a plugin is enabled.
     *
     * @param event The plugin enable event.
     */
    @EventHandler(event = PluginEnableEvent.class, priority = org.bukkit.event.EventPriority.NORMAL)
    public void onPluginEnable(PluginEnableEvent event) {
        try {
            if (event.getPlugin() instanceof MultiInv) {
                this.plugin.getImportManager().hookMultiInv((MultiInv) event.getPlugin());
            } else if (event.getPlugin() instanceof WorldInventories) {
                this.plugin.getImportManager().hookWorldInventories((WorldInventories) event.getPlugin());
            }
        } catch (NoClassDefFoundError ignore) { }
    }

    /**
     * Called when a plugin is disabled.
     *
     * @param event The plugin disable event.
     */
    @EventHandler(event = PluginDisableEvent.class, priority = org.bukkit.event.EventPriority.NORMAL)
    public void onPluginDisable(PluginDisableEvent event) {
        try {
            if (event.getPlugin() instanceof MultiInv) {
                this.plugin.getImportManager().unHookMultiInv();
            } else if (event.getPlugin() instanceof WorldInventories) {
                this.plugin.getImportManager().unHookWorldInventories();
            }
        } catch (NoClassDefFoundError ignore) { }
    }

}
