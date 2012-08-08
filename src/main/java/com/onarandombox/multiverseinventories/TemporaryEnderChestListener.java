package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
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
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TemporaryEnderChestListener implements Listener {

    private Inventories plugin;

    TemporaryEnderChestListener(Inventories plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryOpen(InventoryOpenEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        PlayerProfile playerProfile = null;
        List<WorldGroupProfile> groupsForWorld = plugin.getGroupManager().getGroupsForWorld(player.getWorld().getName());
        for (WorldGroupProfile worldGroupProfile : groupsForWorld) {
            if (worldGroupProfile.isSharing(Sharables.ENDER_CHEST)) {
                playerProfile = worldGroupProfile.getPlayerData(player);
                break;
            }
        }
        if (playerProfile == null) {
            WorldProfile worldProfile = plugin.getWorldManager().getWorldProfile(player.getWorld().getName());
            playerProfile = worldProfile.getPlayerData(player);
        }
        ItemStack[] contents = playerProfile.get(Sharables.ENDER_CHEST);
        if (contents == null) {
            contents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.ENDER_CHEST_SIZE]);
            playerProfile.set(Sharables.ENDER_CHEST, contents);
            plugin.getData().updatePlayerData(playerProfile);
            Logging.finest("Creating blank inventory for ender chest");
        }
        event.getInventory().setContents(contents);
        Logging.finest("Loaded ender chest from '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        PlayerProfile playerProfile = null;
        List<WorldGroupProfile> groupsForWorld = plugin.getGroupManager().getGroupsForWorld(player.getWorld().getName());
        for (WorldGroupProfile worldGroupProfile : groupsForWorld) {
            if (worldGroupProfile.isSharing(Sharables.ENDER_CHEST)) {
                playerProfile = worldGroupProfile.getPlayerData(player);
                playerProfile.set(Sharables.ENDER_CHEST, event.getInventory().getContents());
                plugin.getData().updatePlayerData(playerProfile);
                Logging.finest("Saved ender chest for '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
            }
        }
        WorldProfile worldProfile = plugin.getWorldManager().getWorldProfile(player.getWorld().getName());
        playerProfile = worldProfile.getPlayerData(player);
        playerProfile.set(Sharables.ENDER_CHEST, event.getInventory().getContents());
        plugin.getData().updatePlayerData(playerProfile);
        Logging.finest("Saved ender chest for '" + playerProfile.getContainerType() + ":" + playerProfile.getContainerName() + "' for player '" + player.getName() + "' and gamemode profile '" + playerProfile.getProfileType() + "'");
    }
}
