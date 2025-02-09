package org.mvplugins.multiverse.inventories;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jvnet.hk2.annotations.Service;

/**
 * This is a simple patch that fixes a
 * <a href="https://github.com/Multiverse/Multiverse-Inventories/issues/161">reported item duping glitch</a>
 * , which allowed players to take items from creative to survival by exploiting a server bug.<br>
 * <br>
 * <b>The bug</b><br>
 * When the client sends a <a href="http://wiki.vg/Protocol#Creative_Inventory_Action">creative inventory packet</a>
 * at the right time between switching worlds, the packet is accepted while the player
 * is on a survival world. With proper timing this allowed the player to take items
 * from creative to survival, or with modded clients, gain any item.<br>
 * <br>
 * <b>The fix</b><br>
 * When the player is changing worlds, put the creative inventory packet on a 5-tick cooldown.
 * This is done by cancelling the Bukkit InventoryCreativeEvent.<br>
 * <br>
 * <b>Usage</b><br>
 * Create an instance of this Class in onEnable() of the plugin. In onDisable, disable the
 * patch again. Use {@link #enable(Plugin)} and {@link #disable()} for that.
 *
 * @author Irmo van den Berge (bergerkiller)
 * @version 1.0
 */
final class InventoriesDupingPatch {

    private static final int SLOT_TIMEOUT = 5;

    static InventoriesDupingPatch enableDupingPatch(Plugin plugin) {
        InventoriesDupingPatch patch = new InventoriesDupingPatch();
        patch.enable(plugin);
        return patch;
    }

    private int updateTimeoutsTaskId = -1;
    private final Map<UUID, Integer> timeouts = new HashMap<>();

    private final Listener listener = new Listener() {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            timeouts.remove(event.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            timeouts.remove(event.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
            if (updateTimeoutsTaskId != -1) { // Only when enabled
                timeouts.put(event.getPlayer().getUniqueId(), SLOT_TIMEOUT);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCreativeSlotChange(InventoryCreativeEvent event) {
            if (timeouts.isEmpty()) {
                return; // Saves performance for most common case
            }
            InventoryHolder holder = event.getInventory().getHolder();
            if (holder instanceof Player player && timeouts.containsKey(player.getUniqueId())) {
                event.setResult(Result.DENY);
            }
        }
    };

    /**
     * Enables this patch, registering listeners and tasks using the plugin specified
     *
     * @param plugin to enable with
     */
    public void enable(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this.listener, plugin);
        this.updateTimeoutsTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin, new UpdateTimeoutsTask(), 1, 1);
    }

    /**
     * Disables this patch, cancelling any registered listeners and tasks
     */
    public void disable() {
        if (this.updateTimeoutsTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.updateTimeoutsTaskId);
            this.updateTimeoutsTaskId = -1;
        }
        this.timeouts.clear();
    }

    private final class UpdateTimeoutsTask implements Runnable {
        @Override
        public void run() {
            if (timeouts.isEmpty()) {
                return; // Saves performance for most common case
            }
            Iterator<Map.Entry<UUID, Integer>> iter = timeouts.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<UUID, Integer> e = iter.next();
                int value = e.getValue() - 1;
                if (value > 0) {
                    e.setValue(value);
                } else {
                    iter.remove();
                }
            }
        }
    }
}
