package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.api.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dumptruckman
 */
public enum MVIPerms {
    /**
     * Permission for /mvinv info.
     */
    COMMAND_INFO(new Permission("multiverse.inventories.info", "Displays information about a world or group.", PermissionDefault.OP)),
    /**
     * Permission for /mvinv list.
     */
    COMMAND_LIST(new Permission("multiverse.inventories.list", "Displays a list of groups.", PermissionDefault.OP)),
    /**
     * Permission for /mvinv reload.
     */
    COMMAND_RELOAD(new Permission("multiverse.inventories.reload", "Reloads config file.", PermissionDefault.OP)),
    /**
     * Permission for /mvinv import.
     */
    COMMAND_IMPORT(new Permission("multiverse.inventories.import", "Imports data from MultiInv/WorldInventories", PermissionDefault.OP)),
    /**
     * Permission for /mvinv addworld.
     */
    COMMAND_ADDWORLD(new Permission("multiverse.inventories.addworld", "Adds a world to a world group",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv remvoveworld.
     */
    COMMAND_RMWORLD(new Permission("multiverse.inventories.removeworld", "Removes a world from a world group",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv addshare.
     */
    COMMAND_ADDSHARES(new Permission("multiverse.inventories.addshares", "Adds share(s) to a world group",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv addshare.
     */
    COMMAND_RMSHARES(new Permission("multiverse.inventories.removeshares", "Removes share(s) from a world group",
            PermissionDefault.OP)),
    /**
     * Permissions for /mvinv spawn.
     */
    COMMAND_SPAWN(new Permission("multiverse.inventories.spawn.self", "teleport yourself to group spawn",
            PermissionDefault.OP)),
    /**
     * Permissions for /mvinv spawn.
     */
    COMMAND_SPAWN_OTHER(new Permission("multiverse.inventories.spawn.other", "teleport other to group spawn",
            PermissionDefault.OP)),
    /**
     * Permission for bypassing all groups.
     */
    BYPASS_GROUP_ALL(new Permission("mvinv.bypass.group.*", "", PermissionDefault.FALSE)),
    /**
     * Permission prefix for bypassing groups.
     */
    BYPASS_GROUP("mvinv.bypass.group.") {
        private String getBypassMessage(Player player, String name) {
            return "Player: " + player.getName() + " has bypass perms for group: " + name;
        }
    },
    /**
     * Permission for bypassing all worlds.
     */
    BYPASS_WORLD_ALL(new Permission("mvinv.bypass.world.*", "", PermissionDefault.FALSE)),
    /**
     * Permission prefix for bypassing worlds.
     */
    BYPASS_WORLD("mvinv.bypass.world.") {
        private String getBypassMessage(Player player, String name) {
            return "Player: " + player.getName() + " has bypass perms for world: " + name;
        }
    };

    private Permission perm = null;
    private String permNode = "";

    MVIPerms(Permission perm) {
        this.perm = perm;
    }

    MVIPerms(String permNode) {
        this.permNode = permNode;
    }

    /**
     * @return the Permission.
     */
    public Permission getPerm() {
        return this.perm;
    }

    /**
     * @return the Permission node string.
     */
    public String getPermNode() {
        return this.permNode;
    }

    /**
     * @param finalNode    String to add to the bypass prefix.
     * @param groupManager The GroupManager for Multiverse-Inventories.
     * @return The full permission node for bypass.
     */
    public String getBypassNode(String finalNode, GroupManager groupManager) {
        String bypassNode = this.getPermNode() + finalNode;
        if (Bukkit.getPluginManager().getPermission(bypassNode) == null) {
            if (Bukkit.getWorld(finalNode) != null
                    || (groupManager != null && groupManager.getGroup(finalNode) != null)) {
                Bukkit.getPluginManager().addPermission(new Permission(bypassNode, PermissionDefault.FALSE));
            }
        }
        return bypassNode;
    }

    /**
     * Checks if a player has permission to bypass something which requires a name of an object to be bypassed.
     * A World name for example.
     *
     * @param player       Player to check permission for.
     * @param name         Name of object to bypass.
     * @param groupManager The GroupManager for Multiverse-Inventories.
     * @return True if player is allowed to bypass.
     */
    public boolean hasBypass(Player player, String name, GroupManager groupManager) {
        boolean hasBypass = player.hasPermission(this.getBypassNode(name, groupManager))
                || player.hasPermission(this.getBypassNode("*", groupManager));
        if (hasBypass) {
            MVILog.debug(this.getBypassMessage(player, name));
        }
        return hasBypass;
    }

    private String getBypassMessage(Player player, String name) {
        return "";
    }

    /**
     * Checks if the sender has the node in question.
     *
     * @param sender CommandSender to check permission for.
     * @return True if sender has the permission.
     */
    public boolean has(CommandSender sender) {
        return sender.hasPermission(perm);
    }

    /**
     * Registers all Permission to the plugin.
     *
     * @param plugin Plugin to register permissions to.
     */
    public static void register(JavaPlugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        for (MVIPerms perm : MVIPerms.values()) {
            if (perm.getPerm() != null) {
                pm.addPermission(perm.getPerm());
            }
        }
    }
}
