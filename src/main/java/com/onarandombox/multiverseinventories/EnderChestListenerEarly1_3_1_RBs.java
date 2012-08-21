package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnderChestListenerEarly1_3_1_RBs implements Listener {

    private final Inventories plugin;

    private boolean hasBeenWarned = false;

    private final Map<String, Inventory> openInventories = new HashMap<String, Inventory>();

    EnderChestListenerEarly1_3_1_RBs(final Inventories plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryOpen(final InventoryOpenEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getPlayer();
        final String world = player.getWorld().getName();
        PlayerProfile playerProfile = null;
        boolean multiple = false;
        final List<WorldGroupProfile> groupsForWorld = plugin.getGroupManager().getGroupsForWorld(world);
        for (WorldGroupProfile worldGroupProfile : groupsForWorld) {
            if (worldGroupProfile.isSharing(Sharables.ENDER_CHEST)) {
                if (playerProfile == null) {
                    playerProfile = worldGroupProfile.getPlayerData(player);
                } else {
                    multiple = true;
                    break;
                }
            }
        }
        if (multiple && !hasBeenWarned) {
            Logging.warning("There was a conflict when attempting to load an Ender chest inventory due to world '" + world + "' sharing ender_chest in multiple groups. One inventory was picked from multiple.  It is not likely anything very bad will occur due to this but item loss is possible.  This is a temporary issue and will not occur once Bukkit adds a way to properly work with Ender chests.  This may continue to happen but this is the only warning you will receive.");
            hasBeenWarned = true;
        }
        if (playerProfile == null) {
            playerProfile = plugin.getWorldManager().getWorldProfile(world).getPlayerData(player);
        }
        ItemStack[] contents = playerProfile.get(Sharables.ENDER_CHEST);
        if (contents == null) {
            contents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.ENDER_CHEST_SIZE]);
            playerProfile.set(Sharables.ENDER_CHEST, contents);
            plugin.getData().updatePlayerData(playerProfile);
            Logging.finest("Creating blank inventory for ender chest");
        }
        event.getInventory().setContents(contents);
        openInventories.put(player.getName(), event.getInventory());
        Logging.finest("Loaded ender chest from '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryClose(final InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getPlayer();
        final String world = player.getWorld().getName();
        PlayerProfile playerProfile = null;
        final List<WorldGroupProfile> groupsForWorld = plugin.getGroupManager().getGroupsForWorld(world);
        for (WorldGroupProfile worldGroupProfile : groupsForWorld) {
            if (worldGroupProfile.isSharing(Sharables.ENDER_CHEST)) {
                playerProfile = worldGroupProfile.getPlayerData(player);
                playerProfile.set(Sharables.ENDER_CHEST, event.getInventory().getContents());
                plugin.getData().updatePlayerData(playerProfile);
                Logging.finest("Saved ender chest for '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
            }
        }
        playerProfile = plugin.getWorldManager().getWorldProfile(world).getPlayerData(player);
        playerProfile.set(Sharables.ENDER_CHEST, event.getInventory().getContents());
        plugin.getData().updatePlayerData(playerProfile);
        openInventories.remove(player.getName());
        Logging.finest("Saved ender chest for '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (openInventories.containsKey(player.getName())) {
            final Inventory inventory = openInventories.get(player.getName());
            final String world = player.getWorld().getName();
            PlayerProfile playerProfile = null;
            final List<WorldGroupProfile> groupsForWorld = plugin.getGroupManager().getGroupsForWorld(world);
            for (WorldGroupProfile worldGroupProfile : groupsForWorld) {
                if (worldGroupProfile.isSharing(Sharables.ENDER_CHEST)) {
                    playerProfile = worldGroupProfile.getPlayerData(player);
                    playerProfile.set(Sharables.ENDER_CHEST, inventory.getContents());
                    plugin.getData().updatePlayerData(playerProfile);
                    Logging.finest("Saved ender chest for '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
                }
            }
            playerProfile = plugin.getWorldManager().getWorldProfile(world).getPlayerData(player);
            playerProfile.set(Sharables.ENDER_CHEST, inventory.getContents());
            plugin.getData().updatePlayerData(playerProfile);
            openInventories.remove(player.getName());
            Logging.finest("Saved ender chest for '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
        }
    }
}
